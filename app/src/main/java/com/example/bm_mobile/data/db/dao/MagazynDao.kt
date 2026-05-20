package com.example.bm_mobile.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.bm_mobile.data.db.entity.MagazynEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MagazynDao {

    @Query("SELECT * FROM magazyny WHERE aktywny = 1 ORDER BY nazwa")
    fun getAll(): Flow<List<MagazynEntity>>

    @Query("SELECT * FROM magazyny WHERE id = :id")
    suspend fun getById(id: Int): MagazynEntity?

    @Upsert
    suspend fun upsertAll(magazyny: List<MagazynEntity>)
}
