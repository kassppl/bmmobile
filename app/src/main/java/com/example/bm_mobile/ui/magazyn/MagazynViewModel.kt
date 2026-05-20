package com.example.bm_mobile.ui.magazyn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bm_mobile.data.api.ApiService
import com.example.bm_mobile.data.api.dto.DokumentDto
import com.example.bm_mobile.data.api.dto.MagazynDto
import com.example.bm_mobile.data.api.dto.ProduktDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MagazynUiState {
    object Loading : MagazynUiState()
    data class Success<T>(val data: T) : MagazynUiState()
    data class Error(val message: String) : MagazynUiState()
}

class MagazynViewModel(private val api: ApiService) : ViewModel() {

    private val _magazyny = MutableStateFlow<MagazynUiState>(MagazynUiState.Loading)
    val magazyny: StateFlow<MagazynUiState> = _magazyny.asStateFlow()

    private val _produkty = MutableStateFlow<MagazynUiState>(MagazynUiState.Loading)
    val produkty: StateFlow<MagazynUiState> = _produkty.asStateFlow()

    private val _dokumenty = MutableStateFlow<MagazynUiState>(MagazynUiState.Loading)
    val dokumenty: StateFlow<MagazynUiState> = _dokumenty.asStateFlow()

    fun loadMagazyny() {
        _magazyny.value = MagazynUiState.Loading
        viewModelScope.launch {
            runCatching { api.getMagazyny() }.fold(
                onSuccess = { _magazyny.value = MagazynUiState.Success(it) },
                onFailure = { _magazyny.value = MagazynUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadProdukty(magazynId: Int) {
        _produkty.value = MagazynUiState.Loading
        viewModelScope.launch {
            runCatching { api.getProdukty(magazynId) }.fold(
                onSuccess = { _produkty.value = MagazynUiState.Success(it) },
                onFailure = { _produkty.value = MagazynUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadDokumenty(magazynId: Int) {
        _dokumenty.value = MagazynUiState.Loading
        viewModelScope.launch {
            runCatching { api.getDokumenty(magazynId) }.fold(
                onSuccess = { _dokumenty.value = MagazynUiState.Success(it) },
                onFailure = { _dokumenty.value = MagazynUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    companion object {
        fun factory(api: ApiService) = viewModelFactory {
            initializer { MagazynViewModel(api) }
        }
    }
}
