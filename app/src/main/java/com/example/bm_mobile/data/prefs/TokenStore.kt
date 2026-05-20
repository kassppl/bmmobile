package com.example.bm_mobile.data.prefs

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("bm_prefs", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("api_token", null)
        set(value) { prefs.edit().putString("api_token", value).apply() }

    var baseUrl: String
        get() = prefs.getString("base_url", DEFAULT_URL) ?: DEFAULT_URL
        set(value) { prefs.edit().putString("base_url", value).apply() }

    fun clear() = prefs.edit().clear().apply()

    companion object {
        const val DEFAULT_URL = "https://bm.kassp.pl/"
    }
}
