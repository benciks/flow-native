package com.example.flow.domain.model

import java.time.LocalDateTime

data class TimeRecord(
    val id: String,
    val start: String,
    val end: String,
    val startDateTime: LocalDateTime?,
    val endDateTime: LocalDateTime?,
    val tags: List<String>,
)