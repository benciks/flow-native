package com.example.flow.data.mapper

import com.example.flow.CreateTaskMutation
import com.example.flow.DeleteTaskMutation
import com.example.flow.EditTaskMutation
import com.example.flow.MarkTaskDoneMutation
import com.example.flow.StartTaskMutation
import com.example.flow.StopTaskMutation
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
        tags = tags,
        depends = depends,
        parent = parent,
        recur = recur,
        until = until,
        untilDateTime = parseToLocalDateTime(until),
        start = start
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
        tags = tags,
        depends = depends,
        parent = parent,
        recur = recur,
        until = until,
        untilDateTime = parseToLocalDateTime(until),
        start = start
    )
}

fun MarkTaskDoneMutation.MarkTaskDone.toTask(): Task {
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
        tags = tags,
        depends = depends,
        parent = parent,
        recur = recur,
        until = until,
        untilDateTime = parseToLocalDateTime(until),
        start = start
    )
}

fun EditTaskMutation.EditTask.toTask(): Task {
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
        tags = tags,
        depends = depends,
        parent = parent,
        recur = recur,
        until = until,
        untilDateTime = parseToLocalDateTime(until),
        start = start
    )
}

fun DeleteTaskMutation.DeleteTask.toTask(): Task {
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
        tags = tags,
        depends = depends,
        parent = parent,
        recur = recur,
        until = until,
        untilDateTime = parseToLocalDateTime(until),
        start = start
    )
}

fun StartTaskMutation.StartTask.toTask(): Task {
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
        tags = tags,
        depends = depends,
        parent = parent,
        recur = recur,
        until = until,
        untilDateTime = parseToLocalDateTime(until),
        start = start
    )
}

fun StopTaskMutation.StopTask.toTask(): Task {
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
        tags = tags,
        depends = depends,
        parent = parent,
        recur = recur,
        until = until,
        untilDateTime = parseToLocalDateTime(until),
        start = start
    )
}

private fun parseToLocalDateTime(timestamp: String?): ZonedDateTime? {
    if (timestamp == null) return null
    if (timestamp.isEmpty()) return null
    return ZonedDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX"))
        .withZoneSameInstant(
            ZoneId.systemDefault()
        )
}