package com.example.flow.ui.screens.tasks

import com.example.flow.data.model.Task
import com.example.flow.data.model.TimeRecord
import kotlinx.coroutines.flow.Flow

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
)