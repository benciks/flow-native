package com.example.flow.ui.screens.tasks

import com.example.flow.domain.model.Task
import com.example.flow.domain.model.TimeRecord
import kotlinx.coroutines.flow.Flow

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
)