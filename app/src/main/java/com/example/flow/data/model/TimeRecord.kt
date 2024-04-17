package com.example.flow.data.model

import java.time.LocalDateTime

data class TimeRecord(
    val id: String,
    val start: String,
    val end: String,
    val startDateTime: LocalDateTime?,
    val endDateTime: LocalDateTime?,
    val tags: List<String>,
)