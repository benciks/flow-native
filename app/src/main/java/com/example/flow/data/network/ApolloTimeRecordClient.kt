package com.example.flow.data.network

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.watch
import com.example.flow.TimeRecordsQuery
import com.example.flow.TimeStartMutation
import com.example.flow.TimeStopMutation
import com.example.flow.data.mapper.toTimeRecord
import com.example.flow.domain.model.TimeRecord
import com.example.flow.domain.network.TimeRecordsClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

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
            .data?.timeStart?.toTimeRecord() ?: TimeRecord("-1", "0", "0", emptyList())
    }

    override suspend fun stopTimer(): TimeRecord {
        return apolloClient
            .mutation(TimeStopMutation())
            .execute()
            .data?.timeStop?.toTimeRecord() ?: TimeRecord("-1", "0", "0", emptyList())
    }
}