package com.example.flow.ui.screens.time

import com.example.flow.data.model.TimeRecord
import kotlinx.coroutines.flow.Flow

data class TimeRecordsState(
    val timeRecords: List<TimeRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val selectedRecord: TimeRecord? = null,
    val currentTimeSeconds: Int = 0,
    val startedAt: String? = null,
    val recentTags: List<String> = emptyList(),
)