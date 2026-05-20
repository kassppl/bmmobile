package com.example.bm_mobile.data.repository

import com.example.bm_mobile.data.api.ApiService
import com.example.bm_mobile.data.db.AppDatabase

class MagazynRepository(
    private val api: ApiService,
    private val db: AppDatabase
) {
    fun getMagazyny() = db.magazynDao().getAll()
    fun getProdukty(magazynId: Int) = db.produktDao().getByMagazyn(magazynId)
}
