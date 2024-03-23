package com.example.flow.domain.use_case.time

import com.example.flow.domain.model.TimeRecord
import com.example.flow.domain.network.TimeRecordsClient

class GetTimeRecordUseCase(
    private val timeRecordsClient: TimeRecordsClient
) {
    suspend fun execute(): List<TimeRecord> {
        return timeRecordsClient.getTimeRecords().sortedByDescending { it.start }
    }
}