package com.sailguard.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sailguard.app.data.dao.TripHistoryDao
import com.sailguard.app.data.model.TripHistoryEntity

@Database(entities = [TripHistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripHistoryDao(): TripHistoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sailguard_db"
                ).build().also { INSTANCE = it }
            }
    }
}
