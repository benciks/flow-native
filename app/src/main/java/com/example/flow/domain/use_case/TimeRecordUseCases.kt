package com.example.flow.domain.use_case

data class TimeRecordUseCases(
    val getTimeRecords: GetTimeRecordUseCase,
    val startTimer: StartTimerUseCase,
    val stopTimer: StopTimerUseCase
)