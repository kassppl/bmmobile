package com.example.bm_mobile.ui.nfc

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bm_mobile.data.api.ApiService
import com.example.bm_mobile.data.api.dto.NfcAssignRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class NfcTagProtection {
    NONE,       // tylko zapis danych NDEF
    PASSWORD,   // ochrona hasłem (tylko zapis chroniony, odczyt swobodny)
    READONLY,   // trwała blokada (tylko odczyt na zawsze)
}

sealed class NfcMode {
    object Idle : NfcMode()
    data class WaitingForAssign(
        val produktId: Int,
        val produktNazwa: String,
        val firmaNazwa: String,
        val protection: NfcTagProtection,
    ) : NfcMode()
}

sealed class NfcEvent {

    data class NavigateToProduct(val produktId: Int) : NfcEvent()
    data class TagAssigned(val produktId: Int, val ndefOk: Boolean, val protectionOk: Boolean?) : NfcEvent()
    data class TagRemoved(val produktId: Int) : NfcEvent()
    data class Error(val message: String) : NfcEvent()
    data class Conflict(val message: String, val otherProductId: Int, val otherNazwa: String) : NfcEvent()
    object TagNotFound : NfcEvent()
}

class NfcViewModel(private val api: ApiService) : ViewModel() {

    private val _mode = MutableStateFlow<NfcMode>(NfcMode.Idle)
    val mode: StateFlow<NfcMode> = _mode.asStateFlow()

    private val _events = MutableSharedFlow<NfcEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<NfcEvent> = _events.asSharedFlow()

    fun startAssignMode(
        produktId: Int,
        produktNazwa: String,
        firmaNazwa: String,
        protection: NfcTagProtection,
    ) {
        _mode.value = NfcMode.WaitingForAssign(produktId, produktNazwa, firmaNazwa, protection)
    }

    fun cancelAssignMode() {
        _mode.value = NfcMode.Idle
    }

    fun handleTag(tag: Tag, tagId: String, tagType: String?) {
        when (val m = _mode.value) {
            is NfcMode.Idle             -> lookupTag(tagId)
            is NfcMode.WaitingForAssign -> assignTag(tag, tagId, tagType, m)
        }
    }

    private fun lookupTag(tagId: String) {
        viewModelScope.launch {
            runCatching { api.nfcLookup(tagId) }.fold(
                onSuccess = { _events.tryEmit(NfcEvent.NavigateToProduct(it.produktId)) },
                onFailure = { _events.tryEmit(NfcEvent.TagNotFound) }
            )
        }
    }

    private fun assignTag(tag: Tag, tagId: String, tagType: String?, mode: NfcMode.WaitingForAssign) {
        _mode.value = NfcMode.Idle
        viewModelScope.launch {
            val ndefText = NfcTagWriter.buildNdefText(mode.firmaNazwa, mode.produktNazwa)

            // NDEF i API równolegle — tag musi być w zasięgu przez obie operacje
            val ndefJob = async(Dispatchers.IO) { NfcTagWriter.writeNdef(tag, ndefText) }
            val apiJob  = async(Dispatchers.IO) {
                runCatching { api.nfcAssign(mode.produktId, NfcAssignRequest(tagId, tagType)) }
            }

            val ndefError = ndefJob.await()
            val apiResult = apiJob.await()

            if (apiResult.isFailure) {
                _events.tryEmit(NfcEvent.Error(apiResult.exceptionOrNull()?.message ?: "Błąd przypisania tagu"))
                return@launch
            }

            // Weryfikacja zapisu — odczyt z tagu potwierdza że dane są na miejscu
            val ndefOk = ndefError == null && withContext(Dispatchers.IO) {
                NfcTagWriter.verifyNdef(tag, ndefText)
            }

            // Ochrona tagu tylko po pomyślnej weryfikacji zapisu NDEF
            val protectionResult: Boolean? = if (!ndefOk || mode.protection == NfcTagProtection.NONE) {
                null
            } else {
                withContext(Dispatchers.IO) {
                    when (mode.protection) {
                        NfcTagProtection.PASSWORD -> NfcTagWriter.setPasswordProtection(tag, tagId) == null
                        NfcTagProtection.READONLY -> NfcTagWriter.makeReadOnly(tag) == null
                        NfcTagProtection.NONE     -> null
                    }
                }
            }

            _events.tryEmit(NfcEvent.TagAssigned(mode.produktId, ndefOk, protectionResult))
        }
    }

    fun removeTag(produktId: Int) {
        viewModelScope.launch {
            runCatching { api.nfcRemove(produktId) }.fold(
                onSuccess = { _events.tryEmit(NfcEvent.TagRemoved(produktId)) },
                onFailure = { _events.tryEmit(NfcEvent.Error(it.message ?: "Błąd usunięcia tagu")) }
            )
        }
    }

    companion object {
        fun factory(api: ApiService) = viewModelFactory {
            initializer { NfcViewModel(api) }
        }
    }
}
