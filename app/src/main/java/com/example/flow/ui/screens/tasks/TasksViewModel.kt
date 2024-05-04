package com.example.flow.ui.screens.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.example.flow.data.model.Task
import com.example.flow.data.model.TaskFilterModel
import com.example.flow.data.repository.TaskRepository
import com.example.flow.type.TaskFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val recentProjects: List<String> = emptyList(),
    val recentTags: List<String> = emptyList(),
    val view: String = "all",
    var selectedTask: Task? = null,
    val appliedFilter: Optional<TaskFilter> = Optional.Absent,
    val filter: TaskFilterModel = TaskFilterModel(status = "pending"),
    val allTasks: List<Task> = emptyList()
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TaskRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TasksState())
    val state = _state.asStateFlow()

    private val errorChan = Channel<String>()
    val errorFlow = errorChan.receiveAsFlow()


    init {
        initializeViewModel()
    }

    fun applyFilter(filter: TaskFilterModel = state.value.filter) {
        val taskFilter = Optional.Present(
            TaskFilter(
                status = Optional.Present(filter.status),
                project = Optional.Present(filter.project),
                priority = Optional.Present(filter.priority),
                due = Optional.Present(
                    filter.due?.format(
                        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                            .withZone(ZoneId.of("UTC"))
                    )
                ),
                tags = Optional.Present(filter.tags),
                description = Optional.Present(filter.description)
            )
        )

        viewModelScope.launch {
            _state.update {
                it.copy(
                    appliedFilter = taskFilter,
                    filter = filter
                )
            }
        }
    }

    fun fetchTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            var tasks: List<Task> = emptyList()
            var recentProjects: List<String> = emptyList()
            var recentTags: List<String> = emptyList()
            var allTasks: List<Task> = emptyList()

            try {
                tasks = tasksRepository.getTasks(state.value.appliedFilter)
                recentProjects = tasksRepository.getRecentProjects()
                recentTags = tasksRepository.getRecentTags()
                allTasks = tasksRepository.getTasks(Optional.Absent)
            } catch (e: Exception) {
                errorChan.send(e.message ?: "An error occurred")
            }
            // Remove all that don't have id
            allTasks = allTasks.filter { it.id != "0" }


            _state.update {
                it.copy(
                    tasks = tasks,
                    recentProjects = recentProjects,
                    recentTags = recentTags,
                    isEmpty = tasks.isEmpty(),
                    allTasks = allTasks,
                    isLoading = false
                )
            }
        }
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }

            applyFilter()
            fetchTasks()

            _state.update {
                it.copy(isLoading = false)
            }
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

            try {
                tasksRepository.createTask(description, due, projectOptional, priorityOptional)
            } catch (e: Exception) {
                errorChan.send(e.message ?: "An error occurred")
            }
            fetchTasks()
        }
    }

    fun markTaskDone(taskId: String) {
        if (taskId == "0") return
        viewModelScope.launch {
            try {
                tasksRepository.markDone(taskId)
            } catch (e: Exception) {
                errorChan.send(e.message ?: "An error occurred")
            }
            fetchTasks()
        }
    }

    fun selectTask(task: Task) {
        _state.update {
            it.copy(selectedTask = task)
        }
    }

    fun clearSelectedTask() {
        _state.update {
            it.copy(selectedTask = null)
        }
    }

    fun editTask(
        taskId: String,
        description: String? = null,
        dueDate: ZonedDateTime? = null,
        project: String? = null,
        priority: String? = null,
        tags: List<String> = emptyList(),
        depends: List<String> = emptyList(),
        recurring: String? = null,
        until: ZonedDateTime? = null
    ) {
        viewModelScope.launch {
            var desc: Optional<String> = Optional.Absent
            var due: Optional<String> = Optional.Absent
            var projectOptional: Optional<String> = Optional.Absent
            var priorityOptional: Optional<String> = Optional.Absent
            val dependsOptional: Optional.Present<List<String>> = Optional.Present(depends)
            var recurringOptional: Optional<String> = Optional.Absent
            var untilOptional: Optional<String> = Optional.Absent

            if (description != null) {
                desc = Optional.Present(description)
            }

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

            if (recurring != null) {
                recurringOptional = Optional.Present(recurring)
            }

            if (until != null) {
                untilOptional = Optional.Present(
                    until.format(
                        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(
                            ZoneId.of("UTC")
                        )
                    )
                )
            }

            try {
                val task = tasksRepository.editTask(
                    taskId,
                    desc,
                    due,
                    projectOptional,
                    priorityOptional,
                    Optional.Present(tags),
                    dependsOptional,
                    recurringOptional,
                    untilOptional
                )

                _state.update {
                    it.copy(selectedTask = task)
                }
            } catch (e: Exception) {
                errorChan.send(e.message ?: "An error occurred")
            }

            fetchTasks()
        }
    }

    fun startTask(taskId: String) {
        viewModelScope.launch {
            try {
                tasksRepository.startTask(taskId)
            } catch (e: Exception) {
                errorChan.send(e.message ?: "An error occurred")
            }
            fetchTasks()
        }
    }

    fun stopTask(taskId: String) {
        viewModelScope.launch {
            try {
                tasksRepository.stopTask(taskId)
            } catch (e: Exception) {
                errorChan.send(e.message ?: "An error occurred")
            }

            fetchTasks()
        }
    }


    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                tasksRepository.deleteTask(taskId)
            } catch (e: Exception) {
                errorChan.send(e.message ?: "An error occurred")
            }

            fetchTasks()
        }
    }
}