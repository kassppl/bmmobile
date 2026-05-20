package com.example.bm_mobile.ui.pojazd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bm_mobile.data.api.ApiService
import com.example.bm_mobile.data.api.dto.PojazdDto
import com.example.bm_mobile.data.api.dto.PrzegladDto
import com.example.bm_mobile.data.api.dto.SerwisDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PojazdUiState {
    object Loading : PojazdUiState()
    data class Success<T>(val data: T) : PojazdUiState()
    data class Error(val message: String) : PojazdUiState()
}

class PojazdViewModel(private val api: ApiService) : ViewModel() {

    private val _pojazdy = MutableStateFlow<PojazdUiState>(PojazdUiState.Loading)
    val pojazdy: StateFlow<PojazdUiState> = _pojazdy.asStateFlow()

    private val _serwisy = MutableStateFlow<PojazdUiState>(PojazdUiState.Loading)
    val serwisy: StateFlow<PojazdUiState> = _serwisy.asStateFlow()

    private val _przeglady = MutableStateFlow<PojazdUiState>(PojazdUiState.Loading)
    val przeglady: StateFlow<PojazdUiState> = _przeglady.asStateFlow()

    fun loadPojazdy() {
        _pojazdy.value = PojazdUiState.Loading
        viewModelScope.launch {
            runCatching { api.getPojazdy() }.fold(
                onSuccess = { _pojazdy.value = PojazdUiState.Success(it) },
                onFailure = { _pojazdy.value = PojazdUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadSerwisy(pojazdId: Int) {
        _serwisy.value = PojazdUiState.Loading
        viewModelScope.launch {
            runCatching { api.getPojazdSerwisy(pojazdId) }.fold(
                onSuccess = { _serwisy.value = PojazdUiState.Success(it) },
                onFailure = { _serwisy.value = PojazdUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadPrzeglady(pojazdId: Int) {
        _przeglady.value = PojazdUiState.Loading
        viewModelScope.launch {
            runCatching { api.getPrzeglady(pojazdId) }.fold(
                onSuccess = { _przeglady.value = PojazdUiState.Success(it) },
                onFailure = { _przeglady.value = PojazdUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    companion object {
        fun factory(api: ApiService) = viewModelFactory {
            initializer { PojazdViewModel(api) }
        }
    }
}
