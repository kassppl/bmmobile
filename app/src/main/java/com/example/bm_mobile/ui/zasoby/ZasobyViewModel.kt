package com.example.bm_mobile.ui.zasoby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bm_mobile.data.api.ApiService
import com.example.bm_mobile.data.api.dto.DokumentDetailDto
import com.example.bm_mobile.data.api.dto.HarmonogramResponse
import com.example.bm_mobile.data.api.dto.InwentaryzacjaDetailDto
import com.example.bm_mobile.data.api.dto.InwentaryzacjaDto
import com.example.bm_mobile.data.api.dto.NaprawyDto
import com.example.bm_mobile.data.api.dto.ProduktDetailDto
import com.example.bm_mobile.data.api.dto.SerwisDetailDto
import com.example.bm_mobile.data.api.dto.SerwisyResponse
import com.example.bm_mobile.data.api.dto.WyjazdDto
import com.example.bm_mobile.data.api.dto.WyjazdSzczegolyDto
import com.example.bm_mobile.data.api.dto.ZamowienieDetailDto
import com.example.bm_mobile.data.api.dto.ZamowienieDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ZasobyUiState<out T> {
    object Loading : ZasobyUiState<Nothing>()
    data class Success<T>(val data: T) : ZasobyUiState<T>()
    data class Error(val message: String) : ZasobyUiState<Nothing>()
}

class ZasobyViewModel(private val api: ApiService) : ViewModel() {

    private val _serwisy = MutableStateFlow<ZasobyUiState<SerwisyResponse>>(ZasobyUiState.Loading)
    val serwisy: StateFlow<ZasobyUiState<SerwisyResponse>> = _serwisy.asStateFlow()

    private val _harmonogram = MutableStateFlow<ZasobyUiState<HarmonogramResponse>>(ZasobyUiState.Loading)
    val harmonogram: StateFlow<ZasobyUiState<HarmonogramResponse>> = _harmonogram.asStateFlow()

    private val _wyjazdy = MutableStateFlow<ZasobyUiState<List<WyjazdDto>>>(ZasobyUiState.Loading)
    val wyjazdy: StateFlow<ZasobyUiState<List<WyjazdDto>>> = _wyjazdy.asStateFlow()

    private val _wyjazdSzczegoly = MutableStateFlow<ZasobyUiState<WyjazdSzczegolyDto>>(ZasobyUiState.Loading)
    val wyjazdSzczegoly: StateFlow<ZasobyUiState<WyjazdSzczegolyDto>> = _wyjazdSzczegoly.asStateFlow()

    private val _naprawy = MutableStateFlow<ZasobyUiState<List<NaprawyDto>>>(ZasobyUiState.Loading)
    val naprawy: StateFlow<ZasobyUiState<List<NaprawyDto>>> = _naprawy.asStateFlow()

    private val _dokument = MutableStateFlow<ZasobyUiState<DokumentDetailDto>>(ZasobyUiState.Loading)
    val dokument: StateFlow<ZasobyUiState<DokumentDetailDto>> = _dokument.asStateFlow()

    private val _produkt = MutableStateFlow<ZasobyUiState<ProduktDetailDto>>(ZasobyUiState.Loading)
    val produkt: StateFlow<ZasobyUiState<ProduktDetailDto>> = _produkt.asStateFlow()

    private val _serwisDetail = MutableStateFlow<ZasobyUiState<SerwisDetailDto>>(ZasobyUiState.Loading)
    val serwisDetail: StateFlow<ZasobyUiState<SerwisDetailDto>> = _serwisDetail.asStateFlow()

    private val _inwentaryzacje = MutableStateFlow<ZasobyUiState<List<InwentaryzacjaDto>>>(ZasobyUiState.Loading)
    val inwentaryzacje: StateFlow<ZasobyUiState<List<InwentaryzacjaDto>>> = _inwentaryzacje.asStateFlow()

    private val _inwentaryzacjaDetail = MutableStateFlow<ZasobyUiState<InwentaryzacjaDetailDto>>(ZasobyUiState.Loading)
    val inwentaryzacjaDetail: StateFlow<ZasobyUiState<InwentaryzacjaDetailDto>> = _inwentaryzacjaDetail.asStateFlow()

    private val _zamowienia = MutableStateFlow<ZasobyUiState<List<ZamowienieDto>>>(ZasobyUiState.Loading)
    val zamowienia: StateFlow<ZasobyUiState<List<ZamowienieDto>>> = _zamowienia.asStateFlow()

    private val _zamowienieDetail = MutableStateFlow<ZasobyUiState<ZamowienieDetailDto>>(ZasobyUiState.Loading)
    val zamowienieDetail: StateFlow<ZasobyUiState<ZamowienieDetailDto>> = _zamowienieDetail.asStateFlow()

    fun loadSerwisy() {
        _serwisy.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getSerwisyGlobalne() }.fold(
                onSuccess = { _serwisy.value = ZasobyUiState.Success(it) },
                onFailure = { _serwisy.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadHarmonogram() {
        _harmonogram.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getHarmonogram() }.fold(
                onSuccess = { _harmonogram.value = ZasobyUiState.Success(it) },
                onFailure = { _harmonogram.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadWyjazdy() {
        _wyjazdy.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getWyjazdy() }.fold(
                onSuccess = { _wyjazdy.value = ZasobyUiState.Success(it) },
                onFailure = { _wyjazdy.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadWyjazdSzczegoly(id: Int) {
        _wyjazdSzczegoly.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getWyjazdSzczegoly(id) }.fold(
                onSuccess = { _wyjazdSzczegoly.value = ZasobyUiState.Success(it) },
                onFailure = { _wyjazdSzczegoly.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadNaprawy() {
        _naprawy.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getNaprawy() }.fold(
                onSuccess = { _naprawy.value = ZasobyUiState.Success(it) },
                onFailure = { _naprawy.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadDokument(id: Int) {
        _dokument.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getDokumentDetail(id) }.fold(
                onSuccess = { _dokument.value = ZasobyUiState.Success(it) },
                onFailure = { _dokument.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadProdukt(id: Int) {
        _produkt.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getProduktDetail(id) }.fold(
                onSuccess = { _produkt.value = ZasobyUiState.Success(it) },
                onFailure = { _produkt.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadSerwisDetail(id: Int) {
        _serwisDetail.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getSerwisDetail(id) }.fold(
                onSuccess = { _serwisDetail.value = ZasobyUiState.Success(it) },
                onFailure = { _serwisDetail.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadInwentaryzacje(magazynId: Int) {
        _inwentaryzacje.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getInwentaryzacje(magazynId) }.fold(
                onSuccess = { _inwentaryzacje.value = ZasobyUiState.Success(it) },
                onFailure = { _inwentaryzacje.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadInwentaryzacjaDetail(id: Int) {
        _inwentaryzacjaDetail.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getInwentaryzacjaDetail(id) }.fold(
                onSuccess = { _inwentaryzacjaDetail.value = ZasobyUiState.Success(it) },
                onFailure = { _inwentaryzacjaDetail.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadZamowienia() {
        _zamowienia.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getZamowienia() }.fold(
                onSuccess = { _zamowienia.value = ZasobyUiState.Success(it) },
                onFailure = { _zamowienia.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    fun loadZamowienieDetail(id: Int) {
        _zamowienieDetail.value = ZasobyUiState.Loading
        viewModelScope.launch {
            runCatching { api.getZamowienieDetail(id) }.fold(
                onSuccess = { _zamowienieDetail.value = ZasobyUiState.Success(it) },
                onFailure = { _zamowienieDetail.value = ZasobyUiState.Error(it.message ?: "Błąd") }
            )
        }
    }

    companion object {
        fun factory(api: ApiService) = viewModelFactory {
            initializer { ZasobyViewModel(api) }
        }
    }
}
