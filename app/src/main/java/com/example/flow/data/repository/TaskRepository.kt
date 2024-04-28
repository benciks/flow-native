package com.example.flow.data.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.flow.CreateTaskMutation
import com.example.flow.DeleteTaskMutation
import com.example.flow.EditTaskMutation
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

    suspend fun editTask(
        taskId: String,
        description: Optional<String>,
        due: Optional<String>,
        project: Optional<String>,
        priority: Optional<String>,
        tags: Optional.Present<List<String>>
    ): Task {
        return apolloClient
            .mutation(
                EditTaskMutation(
                    taskId,
                    description,
                    due = due,
                    project = project,
                    priority = priority,
                    tags = tags
                )
            )
            .execute()
            .data?.editTask?.toTask() ?: throw IllegalStateException("Task not edited")
    }

    suspend fun deleteTask(taskId: String): Task {
        return apolloClient
            .mutation(
                DeleteTaskMutation(taskId)
            )
            .execute()
            .data?.deleteTask?.toTask() ?: throw IllegalStateException("Task not deleted")
    }
}
