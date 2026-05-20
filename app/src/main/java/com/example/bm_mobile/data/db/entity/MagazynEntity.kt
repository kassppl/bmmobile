package com.example.bm_mobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "magazyny")
data class MagazynEntity(
    @PrimaryKey val id: Int,
    val nazwa: String,
    val typ: String? = null,
    val aktywny: Boolean = true,
    val pracownikId: Int? = null,
    val pracownikImie: String? = null,
    val pracownikNazwisko: String? = null
)
