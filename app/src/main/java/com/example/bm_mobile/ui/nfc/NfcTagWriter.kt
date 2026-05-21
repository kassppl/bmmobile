package com.example.bm_mobile.ui.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NfcTagWriter {

    fun buildNdefText(firmaNazwa: String, produktNazwa: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "$firmaNazwa | $produktNazwa | $date"
    }

    /** Zapisuje rekord NDEF na tagu. Zwraca null przy sukcesie, komunikat błędu przy niepowodzeniu. */
    fun writeNdef(tag: Tag, text: String): String? {
        return try {
            val ndefMsg = NdefMessage(NdefRecord.createTextRecord("pl", text))
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    ndef.close()
                    return "Tag jest zablokowany — nie można zapisać danych"
                }
                ndef.writeNdefMessage(ndefMsg)
                ndef.close()
                null
            } else {
                val fmt = NdefFormatable.get(tag)
                if (fmt != null) {
                    fmt.connect()
                    fmt.format(ndefMsg)
                    fmt.close()
                    null
                } else "Tag nie obsługuje NDEF"
            }
        } catch (e: Exception) {
            e.message ?: "Błąd zapisu danych na tagu"
        }
    }

    /** Blokuje tag na stałe (tylko odczyt). Nieodwracalne. Zwraca null przy sukcesie. */
    fun makeReadOnly(tag: Tag): String? {
        return try {
            val ndef = Ndef.get(tag) ?: return "Tag nie obsługuje NDEF"
            ndef.connect()
            if (!ndef.canMakeReadOnly()) {
                ndef.close()
                return "Ten typ tagu nie obsługuje trwałej blokady"
            }
            val ok = ndef.makeReadOnly()
            ndef.close()
            if (ok) null else "Nie udało się zablokować tagu"
        } catch (e: Exception) {
            e.message ?: "Błąd blokowania tagu"
        }
    }

    /**
     * Ustawia ochronę hasłem na NTAG213/215/216.
     * Hasło jest wyprowadzane deterministycznie z UID tagu — aplikacja zawsze je zna.
     * Zwraca null przy sukcesie, komunikat błędu przy niepowodzeniu.
     */
    fun setPasswordProtection(tag: Tag, tagId: String): String? {
        val mfu = MifareUltralight.get(tag) ?: return "Ten typ tagu nie obsługuje ochrony hasłem (tylko NTAG21x)"

        // Faza 1: wykryj typ tagu przez próby odczytu stron konfiguracyjnych
        val config = try {
            mfu.connect()
            val c = detectNtagConfig(mfu)
            mfu.close()
            c
        } catch (e: Exception) {
            try { mfu.close() } catch (_: Exception) {}
            null
        } ?: return "Ochrona hasłem nie jest obsługiwana przez ten tag (wymaga NTAG213/215/216)"

        // Faza 2: zapisz hasło i aktywuj ochronę
        return try {
            mfu.connect()

            val pwd  = derivePassword(tagId)
            val pack = derivePack(tagId)

            // Kolejność ważna: najpierw PWD/PACK/ACCESS, AUTH0 aktywuje ochronę jako ostatni
            mfu.writePage(config.pwdPage,    pwd)
            mfu.writePage(config.packPage,   pack + byteArrayOf(0x00, 0x00))
            // ACCESS: bit 7 = PROT=0 → ochrona tylko zapisu (odczyt bez hasła)
            mfu.writePage(config.accessPage, byteArrayOf(0x00, 0x00, 0x00, 0x00))
            // AUTH0: chroń od strony 4 (start pamięci użytkownika) — aktywuje ochronę
            val cfg0 = mfu.readPages(config.auth0Page).copyOf(4)
            cfg0[3]  = 0x04
            mfu.writePage(config.auth0Page, cfg0)

            mfu.close()
            null
        } catch (e: Exception) {
            try { mfu.close() } catch (_: Exception) {}
            e.message ?: "Błąd ustawiania hasła na tagu"
        }
    }

    /**
     * Odczytuje tag i porównuje z oczekiwanym tekstem NDEF.
     * Zwraca true jeśli dane na tagu zgadzają się z tym co zapisano.
     */
    fun verifyNdef(tag: Tag, expectedText: String): Boolean {
        return try {
            val ndef = Ndef.get(tag) ?: return false
            ndef.connect()
            val msg = ndef.ndefMessage
            ndef.close()
            if (msg == null) return false
            msg.records.any { record ->
                if (record.tnf != NdefRecord.TNF_WELL_KNOWN) return@any false
                if (!record.type.contentEquals(NdefRecord.RTD_TEXT)) return@any false
                // Format rekordu tekstowego: [status byte][język bytes][tekst UTF-8]
                val payload   = record.payload
                val langLen   = payload[0].toInt() and 0x3F
                val text      = String(payload, langLen + 1, payload.size - langLen - 1, Charsets.UTF_8)
                text == expectedText
            }
        } catch (_: Exception) { false }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private data class NtagConfig(
        val auth0Page: Int,
        val accessPage: Int,
        val pwdPage: Int,
        val packPage: Int,
    )

    /**
     * Wykrywa typ NTAG21x przez próby odczytu stron poza zasięgiem mniejszych tagów.
     * NTAG213: 45 stron, NTAG215: 135 stron, NTAG216: 231 stron.
     * Zwraca null jeśli tag nie jest NTAG21x.
     */
    private fun detectNtagConfig(mfu: MifareUltralight): NtagConfig? {
        return try {
            mfu.readPages(40)  // zawiedzie dla zwykłego UL (16 stron)
            try {
                mfu.readPages(130)  // zawiedzie dla NTAG213 (45 stron)
                try {
                    mfu.readPages(226)  // zawiedzie dla NTAG215 (135 stron)
                    NtagConfig(auth0Page = 227, accessPage = 228, pwdPage = 229, packPage = 230) // NTAG216
                } catch (_: Exception) {
                    NtagConfig(auth0Page = 131, accessPage = 132, pwdPage = 133, packPage = 134) // NTAG215
                }
            } catch (_: Exception) {
                NtagConfig(auth0Page = 41, accessPage = 42, pwdPage = 43, packPage = 44) // NTAG213
            }
        } catch (_: Exception) {
            null  // zwykły UL lub inny nieobsługiwany typ
        }
    }

    /** 4-bajtowe hasło wyprowadzone z UID tagu — deterministyczne, znane tylko aplikacji. */
    private fun derivePassword(tagId: String): ByteArray {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest("$tagId:bmmobile_nfc_pwd_v1".toByteArray())
        return hash.copyOfRange(0, 4)
    }

    /** 2-bajtowy PACK (potwierdzenie hasła). */
    private fun derivePack(tagId: String): ByteArray {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest("$tagId:bmmobile_nfc_pack_v1".toByteArray())
        return hash.copyOfRange(0, 2)
    }
}
