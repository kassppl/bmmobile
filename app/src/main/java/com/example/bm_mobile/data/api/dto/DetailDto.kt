package com.example.bm_mobile.data.api.dto

// ── Dokument szczegóły ────────────────────────────────────────────────────────

data class DokumentRuchDto(
    val id: Int,
    val produktId: Int?,
    val produktNazwa: String?,
    val produktSku: String?,
    val ilosc: Double,
    val cenaJednostkowa: Double?,
    val data: String?,
    val opisOperacji: String?,
    val pracownik: String?,
)

data class DokumentDetailDto(
    val id: Int,
    val typ: String,
    val numer: String,
    val dataWystawienia: String?,
    val status: String,
    val opis: String?,
    val magazynNazwa: String?,
    val magazynDocelowy: String?,
    val kontrahent: String?,
    val pracownik: String?,
    val ruchy: List<DokumentRuchDto>,
)

// ── Produkt karta ─────────────────────────────────────────────────────────────

data class ProduktRuchDto(
    val id: Int,
    val ilosc: Double,
    val data: String?,
    val dokNumer: String?,
    val dokTyp: String?,
    val opisOperacji: String?,
)

data class ProduktSerwisSkrotDto(
    val id: Int,
    val status: String,
    val statusLabel: String,
    val opisUsterki: String?,
    val priorytet: String,
    val dataZgl: String?,
)

data class ProduktPrzegladSkrotDto(
    val id: Int,
    val typLabel: String,
    val dataPrzegl: String?,
    val nastepny: String?,
    val wazny: Boolean,
    val wykonawca: String?,
)

data class ProduktCertyfikatDto(
    val id: Int,
    val typ: String?,
    val numer: String?,
    val wydawca: String?,
    val dataWydania: String?,
    val dataWaznosci: String?,
    val wazny: Boolean,
)

data class ProduktDetailDto(
    val id: Int,
    val nazwa: String,
    val sku: String?,
    val kategoria: String?,
    val statusSprzetu: String?,
    val model: String?,
    val producent: String?,
    val numerSeryjnyWewn: String?,
    val jednostkaMiary: String?,
    val opis: String?,
    val wartoscZakupu: Double?,
    val dataZakupu: String?,
    val terminNastepnegoPrzegl: String?,
    val lokalizacja: String?,
    val magazynNazwa: String?,
    val stan: Double,
    val tagNfcId: String?,
    val tagNfcType: String?,
    val ruchy: List<ProduktRuchDto>,
    val serwisy: List<ProduktSerwisSkrotDto>,
    val przeglady: List<ProduktPrzegladSkrotDto>,
    val certyfikaty: List<ProduktCertyfikatDto>,
)

// ── Serwis szczegóły ──────────────────────────────────────────────────────────

data class SerwisHistoriaDto(
    val id: Int,
    val akcja: String,
    val akcjaLabel: String,
    val statusPrzed: String?,
    val statusPo: String?,
    val tresc: String?,
    val data: String,
    val uzytkownik: String?,
)

data class SerwisDetailDto(
    val id: Int,
    val typ: String?,
    val typLabel: String?,
    val priorytet: String,
    val status: String,
    val statusLabel: String,
    val opisUsterki: String?,
    val diagnoza: String?,
    val opisPracWykonanych: String?,
    val kosztorysRobocizna: Double?,
    val kosztorysCzesci: Double?,
    val kosztorysLacznie: Double,
    val roboczogodziny: Double?,
    val dataZgloszenia: String?,
    val dataZakonczenia: String?,
    val terminUmowny: String?,
    val firmaSerwisowa: String?,
    val nrListuPrzewozowego: String?,
    val dataWyslania: String?,
    val dataOdbioru: String?,
    val kosztFaktury: Double?,
    val gwarancjaSerwisowaDo: String?,
    val produktId: Int?,
    val produktNazwa: String?,
    val magazynNazwa: String?,
    val pojazdId: Int?,
    val pojazdNazwa: String?,
    val zglaszajacy: String?,
    val serwisant: String?,
    val historia: List<SerwisHistoriaDto>,
)

// ── Inwentaryzacje ────────────────────────────────────────────────────────────

data class InwentaryzacjaDto(
    val id: Int,
    val numer: String,
    val status: String,
    val dataRozpoczecia: String,
    val dataZamkniecia: String?,
    val opis: String?,
    val liczbaPozycji: Int,
    val uzytkownik: String?,
)

data class InwentaryzacjaPozycjaDto(
    val id: Int,
    val produktId: Int?,
    val produktNazwa: String?,
    val stanSystemowy: Double,
    val stanRealny: Double?,
    val roznica: Double?,
    val uwagi: String?,
)

data class InwentaryzacjaDetailDto(
    val id: Int,
    val numer: String,
    val status: String,
    val dataRozpoczecia: String,
    val dataZamkniecia: String?,
    val opis: String?,
    val magazynNazwa: String?,
    val uzytkownik: String?,
    val pozycje: List<InwentaryzacjaPozycjaDto>,
)

// ── Zamówienia ────────────────────────────────────────────────────────────────

data class ZamowienieDto(
    val id: Int,
    val numer: String?,
    val status: String,
    val statusLabel: String,
    val dataUtworzenia: String?,
    val dostawca: String?,
    val sumaWartosc: Double,
    val liczbaElementow: Int,
)

data class ZamowienieElementDto(
    val id: Int,
    val nazwa: String?,
    val ilosc: Double,
    val jednostka: String?,
    val cenaSzacunk: Double?,
    val wartosc: Double?,
    val linkUrl: String?,
    val uwagi: String?,
)

data class ZamowienieDetailDto(
    val id: Int,
    val numer: String?,
    val status: String,
    val statusLabel: String,
    val dataUtworzenia: String?,
    val dataZatwierdzenia: String?,
    val dataWyslania: String?,
    val dataRealizacji: String?,
    val dostawca: String?,
    val uwagi: String?,
    val tworzacy: String?,
    val zatwierdzajacy: String?,
    val serwisZrodlowy: Int?,
    val sumaWartosc: Double,
    val elementy: List<ZamowienieElementDto>,
)
