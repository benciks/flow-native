package com.example.flow.domain.network

import com.example.flow.domain.model.Task

interface TasksClient {
    suspend fun getTasks(): List<Task>
    suspend fun createTask(description: String): Task
}