package com.example.bm_mobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produkty")
data class ProduktEntity(
    @PrimaryKey val id: Int,
    val magazynId: Int,
    val nazwa: String,
    val sku: String? = null,
    val jednostkaMiary: String? = null,
    val kategoria: String? = null,
    val stan: Double = 0.0,
    val wartosc: Double = 0.0,
    val statusSprzetu: String? = null,
    val numerSeryjnyWewn: String? = null,
    val producent: String? = null,
    val model: String? = null,
    val pendingSync: Boolean = false
)
