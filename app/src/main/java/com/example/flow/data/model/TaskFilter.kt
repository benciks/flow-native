package com.example.flow.data.model

import java.time.ZonedDateTime

data class TaskFilterModel(
    var status: String? = "pending",
    var project: String? = null,
    var priority: String? = null,
    var due: ZonedDateTime? = null,
    var tags: List<String> = emptyList(),
    var description: String = ""
)