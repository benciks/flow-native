package com.example.flow.data.model

import java.time.ZonedDateTime

data class TimeRecord(
    val id: String,
    val start: String,
    val end: String,
    val startDateTime: ZonedDateTime?,
    val endDateTime: ZonedDateTime?,
    val tags: List<String>,
)