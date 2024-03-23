package com.example.flow.domain.use_case.time

data class TimeRecordUseCases(
    val getTimeRecords: GetTimeRecordUseCase,
    val startTimer: StartTimerUseCase,
    val stopTimer: StopTimerUseCase,
    val deleteTimeRecord: DeleteTimeRecordUseCase,
    val modifyTimeRecordDate: ModifyTimeRecordDateUseCase,
    val tagTimeRecord: TagTimeRecordUseCase,
    val untagTimeRecord: UntagTimeRecordUseCase,
)