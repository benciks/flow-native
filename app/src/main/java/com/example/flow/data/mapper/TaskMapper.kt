package com.example.flow.data.mapper

import com.example.flow.CreateTaskMutation
import com.example.flow.TasksQuery
import com.example.flow.data.model.Task
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun TasksQuery.Task.toTask(): Task {
    return Task(
        id = id,
        uuid = uuid,
        description = description,
        entry = entry,
        modified = modified,
        status = status,
        urgency = urgency,
        due = due,
        priority = priority,
        project = project,
        dueDateTime = parseToLocalDateTime(due),
        tags = tags
    )
}

fun CreateTaskMutation.CreateTask.toTask(): Task {
    return Task(
        id = id,
        uuid = uuid,
        description = description,
        entry = entry,
        modified = modified,
        status = status,
        urgency = urgency,
        due = due,
        priority = priority,
        project = project,
        dueDateTime = parseToLocalDateTime(due),
        tags = tags
    )
}

private fun parseToLocalDateTime(timestamp: String): ZonedDateTime? {
    if (timestamp.isEmpty()) return null
    return ZonedDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX"))
        .withZoneSameInstant(
            ZoneId.systemDefault()
        )
}