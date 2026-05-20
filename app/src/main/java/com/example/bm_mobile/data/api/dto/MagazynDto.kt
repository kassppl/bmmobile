package com.example.bm_mobile.data.api.dto

data class MagazynDto(
    val id: Int,
    val nazwa: String,
    val typ: String?,
    val pracownikId: Int?,
    val pracownikImie: String?,
    val pracownikNazwisko: String?
)

data class ProduktDto(
    val id: Int,
    val magazynId: Int,
    val nazwa: String,
    val sku: String?,
    val jednostkaMiary: String?,
    val kategoria: String?,
    val statusSprzetu: String?,
    val numerSeryjnyWewn: String?,
    val producent: String?,
    val model: String?,
    val stan: Double,
    val wartoscZakupu: Double?
)

data class DokumentDto(
    val id: Int,
    val typ: String,
    val numer: String,
    val dataWystawienia: String?,
    val status: String?,
    val kontrahent: String?,
    val pracownik: String?
)
