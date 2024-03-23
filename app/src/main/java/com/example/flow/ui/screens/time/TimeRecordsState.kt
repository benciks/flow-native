package com.example.flow.ui.screens.time

import com.example.flow.domain.model.TimeRecord
import kotlinx.coroutines.flow.Flow

data class TimeRecordsState(
    val timeRecords: List<TimeRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val selectedRecord: TimeRecord? = null,
    val currentTimeSeconds: Int = 0,
    val startedAt: String? = null,
)