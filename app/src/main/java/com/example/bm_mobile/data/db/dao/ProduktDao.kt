package com.example.bm_mobile.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.bm_mobile.data.db.entity.ProduktEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProduktDao {

    @Query("SELECT * FROM produkty WHERE magazynId = :magazynId ORDER BY nazwa")
    fun getByMagazyn(magazynId: Int): Flow<List<ProduktEntity>>

    @Query("SELECT * FROM produkty WHERE id = :id")
    suspend fun getById(id: Int): ProduktEntity?

    @Query("SELECT * FROM produkty WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<ProduktEntity>

    @Upsert
    suspend fun upsertAll(produkty: List<ProduktEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(produkt: ProduktEntity): Long

    @Update
    suspend fun update(produkt: ProduktEntity)

    @Delete
    suspend fun delete(produkt: ProduktEntity)
}
