package com.example.flow.domain.network

import com.apollographql.apollo3.api.Optional
import com.example.flow.domain.model.TimeRecord

interface TimeRecordsClient {
    suspend fun getTimeRecords(): List<TimeRecord>
    suspend fun startTimer(): TimeRecord
    suspend fun stopTimer(): TimeRecord

    // TODO: Add error handling
    suspend fun deleteTimeRecord(id: String): TimeRecord

    suspend fun modifyTimeRecordDate(id: String, start: Optional<String?> = Optional.Absent, end: Optional<String?> = Optional.Absent): TimeRecord

    suspend fun tagTimeRecord(id: String, tag: String): TimeRecord

    suspend fun untagTimeRecord(id: String, tag: String): TimeRecord
}