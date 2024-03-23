package com.example.flow.data.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.network.okHttpClient
import com.example.flow.data.network.ApolloTimeRecordClient
import com.example.flow.domain.network.TimeRecordsClient
import com.example.flow.domain.use_case.time.DeleteTimeRecordUseCase
import com.example.flow.domain.use_case.time.GetTimeRecordUseCase
import com.example.flow.domain.use_case.time.ModifyTimeRecordDateUseCase
import com.example.flow.domain.use_case.time.StartTimerUseCase
import com.example.flow.domain.use_case.time.StopTimerUseCase
import com.example.flow.domain.use_case.time.TagTimeRecordUseCase
import com.example.flow.domain.use_case.time.TimeRecordUseCases
import com.example.flow.domain.use_case.time.UntagTimeRecordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("http://10.0.2.2:8080/query")
            .normalizedCache(cacheFactory)
            .fetchPolicy(FetchPolicy.NetworkFirst)
            .okHttpClient(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideTimeRecordsClient(apolloClient: ApolloClient): TimeRecordsClient {
        return ApolloTimeRecordClient(apolloClient)
    }

    @Provides
    @Singleton
    fun provideTimeRecordUseCases(timeRecordsClient: TimeRecordsClient): TimeRecordUseCases {
        return TimeRecordUseCases(
            GetTimeRecordUseCase(timeRecordsClient),
            StartTimerUseCase(timeRecordsClient),
            StopTimerUseCase(timeRecordsClient),
            DeleteTimeRecordUseCase(timeRecordsClient),
            ModifyTimeRecordDateUseCase(timeRecordsClient),
            TagTimeRecordUseCase(timeRecordsClient),
            UntagTimeRecordUseCase(timeRecordsClient)
        )
    }
}