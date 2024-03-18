package com.example.flow.domain.use_case

import com.example.flow.domain.model.TimeRecord
import com.example.flow.domain.network.TimeRecordsClient

class StopTimerUseCase(
    private val timeRecordsClient: TimeRecordsClient
) {
    suspend fun execute(): TimeRecord {
        return timeRecordsClient.stopTimer()
    }
}