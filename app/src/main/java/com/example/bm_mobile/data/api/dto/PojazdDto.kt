package com.example.bm_mobile.data.api.dto

data class PojazdDto(
    val id: Int,
    val nazwa: String,
    val typ: String?,
    val opis: String?,
    val rejestracja: String?,
    val kierowcaImie: String?,
    val kierowcaNazwisko: String?,
    val ocDo: String?,
    val acDo: String?
)

data class SerwisDto(
    val id: Int,
    val typ: String?,
    val priorytet: String,
    val status: String,
    val statusLabel: String,
    val opisUsterki: String?,
    val diagnoza: String?,
    val dataZgloszenia: String?,
    val dataZakonczenia: String?,
    val terminUmowny: String?,
    val kosztorys: Double
)

data class PrzegladDto(
    val id: Int,
    val typ: String,
    val typLabel: String,
    val nazwa: String?,
    val dataPrzegladu: String?,
    val dataNastepnegoPrzegladu: String?,
    val dataWaznosci: String?,
    val wykonawca: String?,
    val koszt: Double?,
    val wynik: String?,
    val uwagi: String?,
    val wazny: Boolean
)
