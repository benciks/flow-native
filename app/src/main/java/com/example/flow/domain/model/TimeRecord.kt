package com.example.flow.domain.model

data class TimeRecord(
    val id: String,
    val start: String,
    val end: String,
    val tags: List<String>,
)