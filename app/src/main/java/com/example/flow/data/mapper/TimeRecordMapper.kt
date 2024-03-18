package com.example.flow.data.mapper

import com.example.flow.TimeRecordsQuery
import com.example.flow.TimeStartMutation
import com.example.flow.TimeStopMutation
import com.example.flow.domain.model.TimeRecord

// TODO: Compute displayable time records from the GraphQL time records
fun TimeRecordsQuery.TimeRecord.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        tags = tags
    )
}

fun TimeStartMutation.TimeStart.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        tags = tags
    )
}

fun TimeStopMutation.TimeStop.toTimeRecord(): TimeRecord {
    return TimeRecord(
        id = id,
        start = start,
        end = end,
        tags = tags
    )
}