package com.example.bm_mobile.data.api.dto

data class NfcLookupDto(
    val produktId: Int,
    val nazwa: String,
    val magazynNazwa: String?,
)

data class NfcAssignRequest(
    val tagId: String,
    val tagType: String?,
)
