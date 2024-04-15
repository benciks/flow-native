package com.example.flow.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flow.domain.use_case.task.TasksUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val useCases: TasksUseCases
) : ViewModel() {
    private val _state = MutableStateFlow(TasksState())
    val state = _state.asStateFlow()

    init {
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val tasks = useCases.getTasks.execute()
            _state.update {
                it.copy(
                    tasks = tasks,
                    isLoading = false
                )
            }
        }
    }

    fun createTask(description: String) {
        viewModelScope.launch {
            useCases.createTask.execute(description)

            _state.update {
                it.copy(
                    tasks = useCases.getTasks.execute(),
                    isLoading = false
                )
            }
        }
    }
}