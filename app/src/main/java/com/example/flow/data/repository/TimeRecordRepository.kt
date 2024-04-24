package com.example.flow.data.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.flow.DeleteTimeRecordMutation
import com.example.flow.ModifyTimeRecordDateMutation
import com.example.flow.TagTimeRecordMutation
import com.example.flow.TimeRecordsQuery
import com.example.flow.TimeStartMutation
import com.example.flow.TimeStopMutation
import com.example.flow.UntagTimeRecordMutation
import com.example.flow.data.mapper.toTimeRecord
import com.example.flow.data.model.TimeRecord
import java.time.LocalDateTime
import java.time.ZonedDateTime
import javax.inject.Inject

class TimeRecordRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun getTimeRecords(): List<TimeRecord> {
        return apolloClient
            .query(TimeRecordsQuery())
            .execute()
            .data?.timeRecords?.map { it.toTimeRecord() } ?: emptyList()
    }

    suspend fun startTimer(): TimeRecord {
        return apolloClient
            .mutation(TimeStartMutation())
            .execute()
            .data?.timeStart?.toTimeRecord() ?: TimeRecord(
            "",
            "",
            "",
            ZonedDateTime.now(),
            null,
            emptyList()
        )
    }

    suspend fun stopTimer(): TimeRecord {
        return apolloClient
            .mutation(TimeStopMutation())
            .execute()
            .data?.timeStop?.toTimeRecord() ?: TimeRecord(
            "",
            "",
            "",
            ZonedDateTime.now(),
            null,
            emptyList()
        )
    }

    suspend fun modifyTimeRecordDate(
        id: String,
        start: Optional<String?>,
        end: Optional<String?>,
    ): TimeRecord {
        return apolloClient
            .mutation(ModifyTimeRecordDateMutation(id, start, end))
            .execute()
            .data?.modifyTimeRecordDate?.toTimeRecord() ?: TimeRecord(
            "",
            "",
            "",
            ZonedDateTime.now(),
            null,
            emptyList()
        )
    }

    suspend fun deleteTimeRecord(id: String): TimeRecord {
        return apolloClient
            .mutation(DeleteTimeRecordMutation(id))
            .execute()
            .data?.deleteTimeRecord?.toTimeRecord() ?: TimeRecord(
            "",
            "",
            "",
            ZonedDateTime.now(),
            null,
            emptyList()
        )
    }

    suspend fun tagTimeRecord(id: String, tag: String): TimeRecord {
        return apolloClient
            .mutation(TagTimeRecordMutation(id, tag))
            .execute()
            .data?.tagTimeRecord?.toTimeRecord() ?: TimeRecord(
            "",
            "",
            "",
            ZonedDateTime.now(),
            null,
            emptyList()
        )
    }

    suspend fun untagTimeRecord(id: String, tag: String): TimeRecord {
        return apolloClient
            .mutation(UntagTimeRecordMutation(id, tag))
            .execute()
            .data?.untagTimeRecord?.toTimeRecord() ?: TimeRecord(
            "",
            "",
            "",
            ZonedDateTime.now(),
            null,
            emptyList()
        )
    }
}