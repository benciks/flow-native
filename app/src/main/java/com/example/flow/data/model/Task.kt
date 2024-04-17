package com.example.flow.data.model

import java.time.LocalDateTime

data class Task(
    val id: String,
    val uuid: String,
    val description: String,
    val entry: String,
    val modified: String,
    val status: String,
    val urgency: Double,
    val priority: String,
    val due: String,
    val project: String,
    val tags: List<String>,

    val dueDateTime: LocalDateTime?
)