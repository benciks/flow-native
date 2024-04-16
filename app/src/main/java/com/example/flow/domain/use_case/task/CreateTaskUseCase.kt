package com.example.flow.domain.use_case.task

import com.apollographql.apollo3.api.Optional
import com.example.flow.domain.network.TasksClient

class CreateTaskUseCase(
    private val tasksClient: TasksClient
) {
    suspend fun execute(description: String, dueDate: Optional<String> = Optional.Absent) {
        tasksClient.createTask(description, dueDate)
    }
}