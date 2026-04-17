package com.sailguard.app.data.repository

import com.sailguard.app.data.dao.TripHistoryDao
import com.sailguard.app.data.model.TripHistoryEntity
import kotlinx.coroutines.flow.Flow

class TripHistoryRepository(private val dao: TripHistoryDao) {

    val allTrips: Flow<List<TripHistoryEntity>> = dao.getAll()

    suspend fun saveTrip(entity: TripHistoryEntity) = dao.insert(entity)

    suspend fun getByDestination(destination: String): List<TripHistoryEntity> =
        dao.getByDestination(destination)
}
