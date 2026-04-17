package com.sailguard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sailguard.app.data.database.AppDatabase
import com.sailguard.app.data.model.TripHistoryEntity
import com.sailguard.app.data.repository.TripHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TripHistoryRepository(
        AppDatabase.getDatabase(app).tripHistoryDao()
    )

    val trips: StateFlow<List<TripHistoryEntity>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
