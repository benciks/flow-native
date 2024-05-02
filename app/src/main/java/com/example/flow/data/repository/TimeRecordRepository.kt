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

sealed class TimeRecordRepositoryError(message: String) : Exception(message) {
    object TimeRecordNotStarted : TimeRecordRepositoryError("Time record not started")
    object TimeRecordNotStopped : TimeRecordRepositoryError("Time record not stopped")
    object TimeRecordNotModified : TimeRecordRepositoryError("Time record not modified")
    object TimeRecordNotDeleted : TimeRecordRepositoryError("Time record not deleted")
    object TimeRecordNotTagged : TimeRecordRepositoryError("Time record not tagged")
    object TimeRecordNotUntagged : TimeRecordRepositoryError("Time record not untagged")
    class ApolloError(message: String) : TimeRecordRepositoryError(message)
}

class TimeRecordRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun getTimeRecords(): List<TimeRecord> {
        return try {
            apolloClient
                .query(TimeRecordsQuery())
                .execute()
                .data?.timeRecords?.map { it.toTimeRecord() } ?: emptyList()
        } catch (e: Exception) {
            throw TimeRecordRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun startTimer(): TimeRecord {
        return try {
            apolloClient
                .mutation(TimeStartMutation())
                .execute()
                .data?.timeStart?.toTimeRecord()
                ?: throw TimeRecordRepositoryError.TimeRecordNotStarted
        } catch (e: Exception) {
            throw TimeRecordRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun stopTimer(): TimeRecord {
        return try {
            apolloClient
                .mutation(TimeStopMutation())
                .execute()
                .data?.timeStop?.toTimeRecord()
                ?: throw TimeRecordRepositoryError.TimeRecordNotStopped
        } catch (e: Exception) {
            throw TimeRecordRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun modifyTimeRecordDate(
        id: String,
        start: Optional<String?>,
        end: Optional<String?>,
    ): TimeRecord {
        return try {
            apolloClient
                .mutation(ModifyTimeRecordDateMutation(id, start, end))
                .execute()
                .data?.modifyTimeRecordDate?.toTimeRecord()
                ?: throw TimeRecordRepositoryError.TimeRecordNotModified
        } catch (e: Exception) {
            throw TimeRecordRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun deleteTimeRecord(id: String): TimeRecord {
        return try {
            apolloClient
                .mutation(DeleteTimeRecordMutation(id))
                .execute()
                .data?.deleteTimeRecord?.toTimeRecord()
                ?: throw TimeRecordRepositoryError.TimeRecordNotDeleted
        } catch (e: Exception) {
            throw TimeRecordRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun tagTimeRecord(id: String, tag: String): TimeRecord {
        return try {
            apolloClient
                .mutation(TagTimeRecordMutation(id, tag))
                .execute()
                .data?.tagTimeRecord?.toTimeRecord()
                ?: throw TimeRecordRepositoryError.TimeRecordNotTagged
        } catch (e: Exception) {
            throw TimeRecordRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun untagTimeRecord(id: String, tag: String): TimeRecord {
        return try {
            apolloClient
                .mutation(UntagTimeRecordMutation(id, tag))
                .execute()
                .data?.untagTimeRecord?.toTimeRecord()
                ?: throw TimeRecordRepositoryError.TimeRecordNotUntagged
        } catch (e: Exception) {
            throw TimeRecordRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }
}