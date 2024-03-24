package com.example.flow.ui.screens.time

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.example.flow.domain.model.TimeRecord
import com.example.flow.domain.use_case.time.TimeRecordUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TimeRecordsViewModel @Inject constructor(
    private val useCases: TimeRecordUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(TimeRecordsState())
    val state = _state.asStateFlow()

    init {
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val timeRecords = useCases.getTimeRecords.execute()
            val recentTags = timeRecords.flatMap { it.tags }.distinct()
            _state.update {
                it.copy(
                    timeRecords = timeRecords,
                    isLoading = false,
                    recentTags = recentTags
                ) }

            restartTimerIfRunning(timeRecords)
        }
    }

    fun toDisplayDateTime(date: LocalDateTime?): String {
        if (date == null) {
            return "-"
        }

        Log.i("TimeRecordsViewModel", "toDisplayDateTime: ${date.toLocalDate()} ${LocalDateTime.now().toLocalDate()}")


        // If today, display time only
        if (date.toLocalDate() == LocalDateTime.now().toLocalDate()) {
            return date.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        // If yesterday, display "Yesterday"
        if (date.toLocalDate() == LocalDateTime.now().toLocalDate().minusDays(1)) {
            return "Yesterday " + date.format(DateTimeFormatter.ofPattern(" HH:mm"))
        }

        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }

    private fun restartTimerIfRunning(timeRecords: List<TimeRecord>) {
        if (timeRecords.any { it.end.isBlank() }) {
            val runningRecord = timeRecords.find { it.end.isBlank() }!!
            val seconds = calculateSecondsElapsed(runningRecord.startDateTime)

            _state.update {
                it.copy(
                    isTracking = true,
                    startedAt = runningRecord.startDateTime?.format(DateTimeFormatter.ofPattern("HH:mm")),
                    currentTimeSeconds = seconds
                )
            }

            startTimer()
        }
    }

    private fun calculateSecondsElapsed(start: LocalDateTime?, end: LocalDateTime? = LocalDateTime.now()): Int {
        if (start == null || end == null) {
            return 0
        }
        val startEpochSeconds = start.toEpochSecond(ZoneOffset.UTC)
        val nowEpochSeconds = end.toEpochSecond(ZoneOffset.UTC)
        return (nowEpochSeconds - startEpochSeconds).toInt()
    }

    fun displayDifference(start: LocalDateTime?, end: LocalDateTime?): String {
        if (start == null || end == null) {
            return ""
        }

        val seconds = calculateSecondsElapsed(start, end)
        return secondsToTime(seconds)
    }

    fun startTimer() {
        viewModelScope.launch {
            useCases.startTimer.execute()

            _state.update {
                it.copy(
                    isTracking = true,
                    timeRecords = useCases.getTimeRecords.execute(),
                    startedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                )
            }

            updateTimer()
        }
    }

    private fun updateTimer() {
        viewModelScope.launch {
            while (state.value.isTracking) {
                _state.update {
                    it.copy(currentTimeSeconds = it.currentTimeSeconds.plus(1))
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun stopTimer() {
        viewModelScope.launch {
            useCases.stopTimer.execute()
            _state.update {
                it.copy(
                    timeRecords = useCases.getTimeRecords.execute(),
                    isTracking = false,
                    currentTimeSeconds = 0
                )
            }
        }
    }

    fun secondsToTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return if (hours == 0) {
            String.format("%02d:%02d", minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        }
    }

    fun onSelectRecord(record: TimeRecord) {
        _state.update { it.copy(selectedRecord = record) }
    }

    fun clearSelectedRecord() {
        _state.update { it.copy(selectedRecord = null) }
    }

    fun deleteSelectedRecord() {
        viewModelScope.launch {
            val selectedRecord = state.value.selectedRecord ?: return@launch
            useCases.deleteTimeRecord.execute(selectedRecord.id)
            _state.update {
                it.copy(
                    timeRecords = useCases.getTimeRecords.execute(),
                    selectedRecord = null
                )
            }
        }
    }

    fun tagSelectedRecord(tag: String) {
        viewModelScope.launch {
            val selectedRecord = state.value.selectedRecord ?: return@launch
            val timeRecord = useCases.tagTimeRecord.execute(selectedRecord.id, tag)
            _state.update {
                it.copy(
                    timeRecords = useCases.getTimeRecords.execute(),
                    selectedRecord = timeRecord
                )
            }
        }
    }

    fun untagSelectedRecord(tag: String) {
        viewModelScope.launch {
            val selectedRecord = state.value.selectedRecord ?: return@launch
            val timeRecord = useCases.untagTimeRecord.execute(selectedRecord.id, tag)
            _state.update {
                it.copy(
                    timeRecords = useCases.getTimeRecords.execute(),
                    selectedRecord = timeRecord
                )
            }
        }
    }

    fun modifySelectedRecordDate(start: LocalDateTime?, end: LocalDateTime?) {
        viewModelScope.launch {
            var startTimestamp: Optional<String?> = Optional.Absent
            var endTimestamp: Optional<String?> = Optional.Absent

            if (start != null) {
                startTimestamp = Optional.Present(start.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")))
            }
            if (end != null) {
                endTimestamp = Optional.Present(end.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")))
            }

            val selectedRecord = state.value.selectedRecord ?: return@launch
            val timeRecord = useCases.modifyTimeRecordDate.execute(selectedRecord.id,startTimestamp,endTimestamp)
            Log.i("TimeRecordsViewModel", "modifySelectedRecordDate: $timeRecord")
            _state.update {
                it.copy(
                    timeRecords = useCases.getTimeRecords.execute(),
                    selectedRecord = timeRecord
                )
            }
        }
    }
}
