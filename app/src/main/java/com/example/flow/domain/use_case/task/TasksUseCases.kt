package com.example.flow.domain.use_case.task

data class TasksUseCases(
    val getTasks: GetTasksUseCase,
    val createTask: CreateTaskUseCase
)
