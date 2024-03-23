package com.example.flow.domain.use_case.time

import com.example.flow.domain.model.TimeRecord
import com.example.flow.domain.network.TimeRecordsClient

class UntagTimeRecordUseCase(
    private val timeRecordsClient: TimeRecordsClient
) {
    suspend fun execute(id: String, tag: String): TimeRecord {
        return timeRecordsClient.untagTimeRecord(id, tag)
    }
}