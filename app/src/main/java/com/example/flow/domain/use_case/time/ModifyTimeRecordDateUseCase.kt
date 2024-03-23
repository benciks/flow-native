package com.example.flow.domain.use_case.time

import com.apollographql.apollo3.api.Optional
import com.example.flow.domain.model.TimeRecord
import com.example.flow.domain.network.TimeRecordsClient

class ModifyTimeRecordDateUseCase(
    private val timeRecordsClient: TimeRecordsClient
) {
    suspend fun execute(id: String, start: Optional<String?>, end: Optional<String?>): TimeRecord {
        return timeRecordsClient.modifyTimeRecordDate(id, start, end)
    }
}