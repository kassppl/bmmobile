package com.example.bm_mobile.data.api.dto

data class SerwisGlobalDto(
    val id: Int,
    val typ: String?,
    val typLabel: String?,
    val priorytet: String,
    val status: String,
    val statusLabel: String,
    val opisUsterki: String?,
    val dataZgloszenia: String?,
    val terminUmowny: String?,
    val kosztorys: Double,
    val produktId: Int?,
    val produktNazwa: String?,
    val magazynNazwa: String?,
    val pojazdId: Int?,
    val pojazdNazwa: String?,
)

data class SerwisyResponse(
    val otwarte: List<SerwisGlobalDto>,
    val oczekujace: List<SerwisGlobalDto>,
)

data class HarmonogramItemDto(
    val id: Int,
    val typ: String,
    val nazwa: String,
    val termin: String?,
    val info: String?,
)

data class HarmonogramResponse(
    val przeterminowane: List<HarmonogramItemDto>,
    val nadchodzace: List<HarmonogramItemDto>,
)

data class WyjazdDto(
    val id: Int,
    val numer: String,
    val projektNazwa: String,
    val lokalizacja: String?,
    val dataWyjazdu: String?,
    val dataPowrotu: String?,
    val status: String,
    val statusLabel: String,
    val magazynNazwa: String?,
    val kierownikImie: String?,
    val kierownikNazwisko: String?,
    val liczbaPozycji: Int,
)

data class WyjazdPozycjaDto(
    val id: Int,
    val produktId: Int?,
    val produktNazwa: String?,
    val ilosc: Double,
    val uwagi: String?,
    val wrocilo: Boolean,
    val dataPowrotu: String?,
)

data class WyjazdSzczegolyDto(
    val id: Int,
    val numer: String,
    val projektNazwa: String,
    val lokalizacja: String?,
    val dataWyjazdu: String?,
    val dataPowrotu: String?,
    val status: String,
    val statusLabel: String,
    val magazynNazwa: String?,
    val kierownikImie: String?,
    val kierownikNazwisko: String?,
    val uwagi: String?,
    val pozycje: List<WyjazdPozycjaDto>,
)

data class NaprawyDto(
    val id: Int,
    val jednostka: String?,
    val imie: String?,
    val nazwisko: String?,
    val osobaOdpowiedzialna: String?,
    val opis: String?,
    val numerInwentarzowy: String?,
    val opisUsterki: String?,
    val adres: String?,
    val nazwa: String?,
    val miejsce: String?,
    val zlecajacy: String?,
    val czaszlecenia: String?,
    val dataWykonania: String?,
    val wykonane: Boolean?,
    val historia: Boolean?,
    val wykonawcaImie: String?,
    val wykonawcaNazwisko: String?,
    val pojazdNazwa: String?,
)
