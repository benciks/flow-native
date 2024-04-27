package com.example.flow.ui.screens.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.example.flow.data.model.Task
import com.example.flow.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val recentProjects: List<String> = emptyList(),
    val view: String = "all"
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TaskRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TasksState())
    val state = _state.asStateFlow()

    init {
        initializeViewModel()
    }

    private suspend fun filterTasksByView(view: String) {
        _state.update {
            when (view) {
                "all" -> {
                    it.copy(tasks = tasksRepository.getTasks())
                }

                "ongoing" -> {
                    it.copy(
                        tasks = tasksRepository.getTasks()
                            .filter { task -> task.status == "pending" })
                }

                else -> {
                    it.copy(
                        tasks = tasksRepository.getTasks()
                            .filter { task -> task.status == "completed" })
                }
            }
        }
    }

    suspend fun updateView(view: String) {
        _state.update {
            it.copy(view = view)
        }
        filterTasksByView(view)
    }

    fun fetchTasks() {
        viewModelScope.launch {
            val tasks = tasksRepository.getTasks()
            val recentProjects = tasks.map { it.project }.distinct()

            _state.update {
                it.copy(
                    tasks = tasks,
                    recentProjects = recentProjects
                )
            }
        }
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            fetchTasks()
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun createTask(
        description: String,
        dueDate: ZonedDateTime? = null,
        project: String? = null,
        priority: String? = null
    ) {
        viewModelScope.launch {
            var due: Optional<String> = Optional.Absent
            var projectOptional: Optional<String> = Optional.Absent
            var priorityOptional: Optional<String> = Optional.Absent

            if (dueDate != null) {
                // Since the due date is in ZonedDateTime, convert it to UTC time
                due =
                    Optional.Present(
                        dueDate.format(
                            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(
                                ZoneId.of("UTC")
                            )
                        )
                    )
            }

            if (project != null) {
                projectOptional = Optional.Present(project)
            }

            if (priority != null) {
                priorityOptional = Optional.Present(priority)
            }

            tasksRepository.createTask(description, due, projectOptional, priorityOptional)

            _state.update {
                it.copy(
                    tasks = tasksRepository.getTasks(),
                    isLoading = false
                )
            }
        }
    }

    fun markTaskDone(taskId: String) {
        if (taskId == "0") return
        viewModelScope.launch {
            tasksRepository.markDone(taskId)

            _state.update {
                it.copy(
                    isLoading = false,
                    view = it.view,
                )
            }

            filterTasksByView(state.value.view)
        }
    }
}