package com.example.flow.data.mapper

import com.example.flow.DeleteTimeRecordMutation
import com.example.flow.ModifyTimeRecordDateMutation
import com.example.flow.TagTimeRecordMutation
import com.example.flow.TimeRecordsQuery
import com.example.flow.TimeStartMutation
import com.example.flow.TimeStopMutation
import com.example.flow.UntagTimeRecordMutation
import com.example.flow.domain.model.TimeRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun TimeRecordsQuery.TimeRecord.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        startDateTime = parseToLocalDateTime(start),
        endDateTime = parseToLocalDateTime(end),
        tags = tags
    )
}

fun TimeStartMutation.TimeStart.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        startDateTime = parseToLocalDateTime(start),
        endDateTime = parseToLocalDateTime(end),
        tags = tags
    )
}

fun TimeStopMutation.TimeStop.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        startDateTime = parseToLocalDateTime(start),
        endDateTime = parseToLocalDateTime(end),
        tags = tags
    )
}

fun DeleteTimeRecordMutation.DeleteTimeRecord.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        startDateTime = parseToLocalDateTime(start),
        endDateTime = parseToLocalDateTime(end),
        tags = tags
    )
}

fun ModifyTimeRecordDateMutation.ModifyTimeRecordDate.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        startDateTime = parseToLocalDateTime(start),
        endDateTime = parseToLocalDateTime(end),
        tags = tags
    )
}

fun TagTimeRecordMutation.TagTimeRecord.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        startDateTime = parseToLocalDateTime(start),
        endDateTime = parseToLocalDateTime(end),
        tags = tags
    )
}

fun UntagTimeRecordMutation.UntagTimeRecord.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        startDateTime = parseToLocalDateTime(start),
        endDateTime = parseToLocalDateTime(end),
        tags = tags
    )
}

private fun parseToLocalDateTime(timestamp: String): LocalDateTime? {
    if (timestamp.isEmpty()) return null
    return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))
}