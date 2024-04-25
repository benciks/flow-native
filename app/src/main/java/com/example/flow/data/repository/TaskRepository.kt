package com.example.flow.data.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.flow.CreateTaskMutation
import com.example.flow.MarkTaskDoneMutation
import com.example.flow.TasksQuery
import com.example.flow.data.model.Task
import com.example.flow.data.mapper.toTask
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun getTasks(): List<Task> {
        return apolloClient
            .query(TasksQuery())
            .execute()
            .data?.tasks?.map { it.toTask() } ?: emptyList()
    }

    suspend fun createTask(
        description: String,
        due: Optional<String>,
        project: Optional<String>,
        priority: Optional<String>
    ): Task {
        return apolloClient
            .mutation(
                CreateTaskMutation(
                    description,
                    due = due,
                    project = project,
                    priority = priority
                )
            )
            .execute()
            .data?.createTask?.toTask() ?: throw IllegalStateException("Task not created")
    }

    suspend fun markDone(taskId: String): Task {
        return apolloClient
            .mutation(
                MarkTaskDoneMutation(taskId)
            )
            .execute()
            .data?.markTaskDone?.toTask() ?: throw IllegalStateException("Task not marked as done")
    }


}
