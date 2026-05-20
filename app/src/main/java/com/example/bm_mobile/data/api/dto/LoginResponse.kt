package com.example.bm_mobile.data.api.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("Authentication") val authentication: String,
    val username: String,
    val roles: List<String>,
    val token: String,
    val firmaId: Int?,
    val firmaNazwa: String?,
    val firmy: List<FirmaDto> = emptyList()
)

data class FirmaDto(val id: Int, val nazwa: String)
