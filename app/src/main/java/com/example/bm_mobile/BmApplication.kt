package com.example.bm_mobile

import android.app.Application
import com.example.bm_mobile.data.api.ApiClient
import com.example.bm_mobile.data.prefs.TokenStore

class BmApplication : Application() {

    val tokenStore by lazy { TokenStore(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        tokenStore.token?.let { ApiClient.setToken(it) }
    }

    companion object {
        lateinit var instance: BmApplication
            private set
    }
}
