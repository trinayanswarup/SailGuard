package com.sailguard.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_history")
data class TripHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destination:  String,
    val countryFlag:  String,
    val tripDays:     Int,
    val planSizeGb:   Double,
    val actualGbUsed: Double,
    val planWasEnough: Boolean,
    val date:         Long,    // epoch millis
    val totalCost:    Double   // plan price in USD
)
