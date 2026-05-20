package com.example.bm_mobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bm_mobile.data.db.dao.MagazynDao
import com.example.bm_mobile.data.db.dao.ProduktDao
import com.example.bm_mobile.data.db.entity.MagazynEntity
import com.example.bm_mobile.data.db.entity.ProduktEntity

@Database(
    entities = [MagazynEntity::class, ProduktEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun magazynDao(): MagazynDao
    abstract fun produktDao(): ProduktDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, "bm.db")
                .build()
                .also { INSTANCE = it }
        }
    }
}
