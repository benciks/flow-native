package com.example.flow.data.network

import com.apollographql.apollo3.ApolloClient
import com.example.flow.CreateTaskMutation
import com.example.flow.TasksQuery
import com.example.flow.domain.model.Task
import com.example.flow.domain.network.TasksClient
import com.example.flow.data.mapper.toTask

class ApolloTasksClient(
    private val apolloClient: ApolloClient
) : TasksClient {
    override suspend fun getTasks(): List<Task> {
        return apolloClient
            .query(TasksQuery())
            .execute()
            .data?.tasks?.map { it.toTask() } ?: emptyList()
    }

    override suspend fun createTask(description: String): Task {
        return apolloClient
            .mutation(CreateTaskMutation(description))
            .execute()
            .data?.createTask?.toTask() ?: throw IllegalStateException("Task not created")
    }

}
