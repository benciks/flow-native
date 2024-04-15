package com.example.flow.domain.use_case.task

import com.example.flow.domain.model.Task
import com.example.flow.domain.network.TasksClient

class GetTasksUseCase(
    private val tasksClient: TasksClient
) {
    suspend fun execute(): List<Task> {
        return tasksClient.getTasks()
    }
}
