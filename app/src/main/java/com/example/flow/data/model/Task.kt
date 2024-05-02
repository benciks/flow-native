package com.example.flow.data.model

import java.time.ZonedDateTime

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
    val depends: List<String>,
    val parent: String?,
    val recur: String?,
    val until: String?,
    val start: String?,

    val dueDateTime: ZonedDateTime?,
    val untilDateTime: ZonedDateTime?
)