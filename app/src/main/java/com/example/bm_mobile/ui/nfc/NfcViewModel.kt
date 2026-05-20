package com.example.bm_mobile.ui.nfc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bm_mobile.data.api.ApiService
import com.example.bm_mobile.data.api.dto.NfcAssignRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NfcMode {
    object Idle : NfcMode()
    data class WaitingForAssign(val produktId: Int) : NfcMode()
}

sealed class NfcEvent {
    data class NavigateToProduct(val produktId: Int) : NfcEvent()
    data class TagAssigned(val produktId: Int) : NfcEvent()
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

    fun startAssignMode(produktId: Int) {
        _mode.value = NfcMode.WaitingForAssign(produktId)
    }

    fun cancelAssignMode() {
        _mode.value = NfcMode.Idle
    }

    fun handleTag(tagId: String, tagType: String?) {
        when (val m = _mode.value) {
            is NfcMode.Idle               -> lookupTag(tagId)
            is NfcMode.WaitingForAssign   -> assignTag(m.produktId, tagId, tagType)
        }
    }

    private fun lookupTag(tagId: String) {
        viewModelScope.launch {
            runCatching { api.nfcLookup(tagId) }.fold(
                onSuccess  = { _events.tryEmit(NfcEvent.NavigateToProduct(it.produktId)) },
                onFailure  = { _events.tryEmit(NfcEvent.TagNotFound) }
            )
        }
    }

    private fun assignTag(produktId: Int, tagId: String, tagType: String?) {
        _mode.value = NfcMode.Idle
        viewModelScope.launch {
            runCatching { api.nfcAssign(produktId, NfcAssignRequest(tagId, tagType)) }.fold(
                onSuccess = { _events.tryEmit(NfcEvent.TagAssigned(produktId)) },
                onFailure = { e ->
                    // HTTP 409 = tag należy do innego produktu
                    val msg = e.message ?: "Błąd przypisania tagu"
                    _events.tryEmit(NfcEvent.Error(msg))
                }
            )
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
