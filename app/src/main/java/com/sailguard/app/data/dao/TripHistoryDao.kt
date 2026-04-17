package com.sailguard.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sailguard.app.data.model.TripHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripHistoryDao {

    @Insert
    suspend fun insert(trip: TripHistoryEntity)

    @Query("SELECT * FROM trip_history ORDER BY date DESC")
    fun getAll(): Flow<List<TripHistoryEntity>>

    @Query("SELECT * FROM trip_history WHERE destination = :destination ORDER BY date DESC")
    suspend fun getByDestination(destination: String): List<TripHistoryEntity>
}
