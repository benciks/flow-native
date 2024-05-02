package com.example.flow.data.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.example.flow.CreateTaskMutation
import com.example.flow.DeleteTaskMutation
import com.example.flow.EditTaskMutation
import com.example.flow.MarkTaskDoneMutation
import com.example.flow.RecentTaskProjectsQuery
import com.example.flow.RecentTaskTagsQuery
import com.example.flow.StartTaskMutation
import com.example.flow.StopTaskMutation
import com.example.flow.TasksQuery
import com.example.flow.data.model.Task
import com.example.flow.data.mapper.toTask
import com.example.flow.type.TaskFilter
import javax.inject.Inject

sealed class TaskRepositoryError(message: String) : Exception(message) {
    object TaskNotCreated : TaskRepositoryError("Task not created")
    object TaskNotMarkedAsDone : TaskRepositoryError("Task not marked as done")
    object TaskNotEdited : TaskRepositoryError("Task not edited")
    object TaskNotDeleted : TaskRepositoryError("Task not deleted")

    object TaskNotStarted : TaskRepositoryError("Task not started")
    object TaskNotStopped : TaskRepositoryError("Task not stopped")
    class ApolloError(message: String) : TaskRepositoryError(message)
}

class TaskRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun getTasks(filter: Optional<TaskFilter?>): List<Task> {
        return try {
            apolloClient
                .query(TasksQuery(filter))
                .execute()
                .data?.tasks?.map { it.toTask() } ?: emptyList()
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun getRecentTags(): List<String> {
        return try {
            apolloClient
                .query(RecentTaskTagsQuery())
                .execute()
                .data?.recentTaskTags ?: emptyList()
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun getRecentProjects(): List<String> {
        return try {
            apolloClient
                .query(RecentTaskProjectsQuery())
                .execute()
                .data?.recentTaskProjects ?: emptyList()
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun createTask(
        description: String,
        due: Optional<String>,
        project: Optional<String>,
        priority: Optional<String>
    ): Task {
        return try {
            apolloClient
                .mutation(
                    CreateTaskMutation(
                        description,
                        due = due,
                        project = project,
                        priority = priority
                    )
                )
                .execute()
                .data?.createTask?.toTask() ?: throw TaskRepositoryError.TaskNotCreated
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun markDone(taskId: String): Task {
        return try {
            apolloClient
                .mutation(
                    MarkTaskDoneMutation(taskId)
                )
                .execute()
                .data?.markTaskDone?.toTask()
                ?: throw TaskRepositoryError.TaskNotMarkedAsDone
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun editTask(
        taskId: String,
        description: Optional<String> = Optional.Absent,
        due: Optional<String> = Optional.Absent,
        project: Optional<String> = Optional.Absent,
        priority: Optional<String> = Optional.Absent,
        tags: Optional.Present<List<String>> = Optional.Present(emptyList()),
        depends: Optional.Present<List<String>> = Optional.Present(emptyList()),
        recurring: Optional<String> = Optional.Absent,
        until: Optional<String> = Optional.Absent
    ): Task {
        return try {
            apolloClient
                .mutation(
                    EditTaskMutation(
                        taskId,
                        description,
                        due = due,
                        project = project,
                        priority = priority,
                        tags = tags,
                        depends = depends,
                        recurring = recurring,
                        until = until
                    )
                )
                .execute()
                .data?.editTask?.toTask() ?: throw TaskRepositoryError.TaskNotEdited
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun deleteTask(taskId: String): Task {
        return try {
            apolloClient
                .mutation(
                    DeleteTaskMutation(taskId)
                )
                .execute()
                .data?.deleteTask?.toTask() ?: throw TaskRepositoryError.TaskNotDeleted
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun startTask(taskId: String): Task {
        return try {
            apolloClient
                .mutation(
                    StartTaskMutation(taskId)
                )
                .execute()
                .data?.startTask?.toTask()
                ?: throw TaskRepositoryError.TaskNotStarted
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }

    suspend fun stopTask(taskId: String): Task {
        return try {
            apolloClient
                .mutation(
                    StopTaskMutation(taskId)
                )
                .execute()
                .data?.stopTask?.toTask()
                ?: throw TaskRepositoryError.TaskNotStopped
        } catch (e: ApolloException) {
            throw TaskRepositoryError.ApolloError(e.message ?: "Network error")
        }
    }
}
