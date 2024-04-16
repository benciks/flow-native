package com.example.flow.domain.network

import com.apollographql.apollo3.api.Optional
import com.example.flow.domain.model.Task

interface TasksClient {
    suspend fun getTasks(): List<Task>
    suspend fun createTask(description: String, due: Optional<String> = Optional.Absent): Task
}