package com.example.flow.domain.network

import com.example.flow.domain.model.TimeRecord

interface TimeRecordsClient {
    suspend fun getTimeRecords(): List<TimeRecord>
    suspend fun startTimer(): TimeRecord
    suspend fun stopTimer(): TimeRecord
}