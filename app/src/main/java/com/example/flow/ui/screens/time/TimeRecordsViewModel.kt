package com.example.flow.ui.screens.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flow.domain.model.TimeRecord
import com.example.flow.domain.use_case.TimeRecordUseCases
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
            _state.update { it.copy(timeRecords = timeRecords, isLoading = false) }

            restartTimerIfRunning(timeRecords)
        }
    }

    private fun timestampToDateTime(timestamp: String): LocalDateTime {
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")).minusHours(1)
    }

    fun toDisplayDateTime(timestamp: String): String {
        val dateTime = timestampToDateTime(timestamp)

        // If today, display time only
        if (dateTime.toLocalDate() == LocalDateTime.now().toLocalDate()) {
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        // If yesterday, display "Yesterday"
        if (dateTime.toLocalDate() == LocalDateTime.now().toLocalDate().minusDays(1)) {
            return "Yesterday " + dateTime.format(DateTimeFormatter.ofPattern(" HH:mm"))
        }

        // Otherwise, display date and time
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }

    private fun restartTimerIfRunning(timeRecords: List<TimeRecord>) {
        if (timeRecords.any { it.end.isBlank() }) {
            val runningRecord = timeRecords.find { it.end.isBlank() }!!
            val start = timestampToDateTime(runningRecord.start)
            val seconds = calculateSecondsElapsed(start)

            _state.update {
                it.copy(
                    isTracking = true,
                    startedAt = start.format(DateTimeFormatter.ofPattern("HH:mm")),
                    currentTimeSeconds = seconds
                )
            }

            startTimer()
        }
    }

    private fun calculateSecondsElapsed(start: LocalDateTime, end: LocalDateTime = LocalDateTime.now().minusHours(2)): Int {
        val startEpochSeconds = start.toEpochSecond(ZoneOffset.UTC)
        val nowEpochSeconds = end.toEpochSecond(ZoneOffset.UTC)
        return (nowEpochSeconds - startEpochSeconds).toInt()
    }

    fun displayDifference(start: String, end: String): String {
        val startDateTime = timestampToDateTime(start)
        val endDateTime = timestampToDateTime(end)

        val seconds = calculateSecondsElapsed(startDateTime, endDateTime)
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
}
