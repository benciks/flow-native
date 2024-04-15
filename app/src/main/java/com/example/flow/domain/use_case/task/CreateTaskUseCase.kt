package com.example.flow.domain.use_case.task

import com.example.flow.domain.network.TasksClient

class CreateTaskUseCase(
    private val tasksClient: TasksClient
) {
    suspend fun execute(description: String) {
        tasksClient.createTask(description)
    }
}