package com.example.bm_mobile.data.repository

import com.example.bm_mobile.data.api.ApiClient
import com.example.bm_mobile.data.api.ApiService
import com.example.bm_mobile.data.api.dto.LoginRequest
import com.example.bm_mobile.data.prefs.TokenStore

class AuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {

    suspend fun login(username: String, password: String): Result<String> = runCatching {
        val resp = api.login(LoginRequest(username, password))
        tokenStore.token = resp.token
        ApiClient.setToken(resp.token)
        resp.username
    }

    fun isLoggedIn(): Boolean = tokenStore.token != null

    fun logout() {
        tokenStore.clear()
        ApiClient.setToken("")
    }
}
