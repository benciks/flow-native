package com.example.flow.data.network

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.watch
import com.example.flow.DeleteTimeRecordMutation
import com.example.flow.ModifyTimeRecordDateMutation
import com.example.flow.TagTimeRecordMutation
import com.example.flow.TimeRecordsQuery
import com.example.flow.TimeStartMutation
import com.example.flow.TimeStopMutation
import com.example.flow.UntagTimeRecordMutation
import com.example.flow.data.mapper.toTimeRecord
import com.example.flow.domain.model.TimeRecord
import com.example.flow.domain.network.TimeRecordsClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime

class ApolloTimeRecordClient(
    private val apolloClient: ApolloClient
): TimeRecordsClient {
    override suspend fun getTimeRecords(): List<TimeRecord> {
        return apolloClient
            .query(TimeRecordsQuery())
            .execute()
            .data?.timeRecords?.map { it.toTimeRecord() } ?: emptyList()
    }

    override suspend fun startTimer(): TimeRecord {
        return apolloClient
            .mutation(TimeStartMutation())
            .execute()
            .data?.timeStart?.toTimeRecord() ?: TimeRecord("", "", "", LocalDateTime.now(), null, emptyList())
    }

    override suspend fun stopTimer(): TimeRecord {
        return apolloClient
            .mutation(TimeStopMutation())
            .execute()
            .data?.timeStop?.toTimeRecord() ?: TimeRecord("", "", "", LocalDateTime.now(), null, emptyList())
    }

    override suspend fun modifyTimeRecordDate(
        id: String,
        start: Optional<String?>,
        end: Optional<String?>,
    ): TimeRecord {
        return apolloClient
            .mutation(ModifyTimeRecordDateMutation(id, start, end))
            .execute()
            .data?.modifyTimeRecordDate?.toTimeRecord() ?: TimeRecord("", "", "", LocalDateTime.now(), null, emptyList())
    }

    override suspend fun deleteTimeRecord(id: String): TimeRecord {
        return apolloClient
            .mutation(DeleteTimeRecordMutation(id))
            .execute()
            .data?.deleteTimeRecord?.toTimeRecord() ?: TimeRecord("", "", "", LocalDateTime.now(), null, emptyList())
    }

    override suspend fun tagTimeRecord(id: String, tag: String): TimeRecord {
        return apolloClient
            .mutation(TagTimeRecordMutation(id, tag))
            .execute()
            .data?.tagTimeRecord?.toTimeRecord() ?: TimeRecord("", "", "", LocalDateTime.now(), null, emptyList())
    }

    override suspend fun untagTimeRecord(id: String, tag: String): TimeRecord {
        return apolloClient
            .mutation(UntagTimeRecordMutation(id, tag))
            .execute()
            .data?.untagTimeRecord?.toTimeRecord() ?: TimeRecord("", "", "", LocalDateTime.now(), null, emptyList())
    }
}