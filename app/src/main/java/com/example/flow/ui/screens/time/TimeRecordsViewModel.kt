package com.example.flow.ui.screens.time

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.example.flow.data.model.TimeRecord
import com.example.flow.data.repository.TimeRecordRepository
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

data class TimeRecordsState(
    val timeRecords: List<TimeRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val timerRunning: Boolean = false,
    val selectedRecord: TimeRecord? = null,
    val currentTimeSeconds: Int = 0,
    val startedAt: String? = null,
    val recentTags: List<String> = emptyList(),
)

@HiltViewModel
class TimeRecordsViewModel @Inject constructor(
    private val timeRecordsRepository: TimeRecordRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimeRecordsState())
    val state = _state.asStateFlow()

    private val errorChan = Channel<String>()
    val errorFlow = errorChan.receiveAsFlow()

    init {
        initializeViewModel()
    }

    fun fetchTimeRecords() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val timeRecords = timeRecordsRepository.getTimeRecords()
                val recentTags = timeRecords.flatMap { it.tags }.distinct()
                _state.update {
                    it.copy(
                        timeRecords = timeRecords,
                        recentTags = recentTags
                    )
                }

                _state.update { it.copy(isLoading = false) }
                restartTimerIfRunning(timeRecords)
            } catch (e: Exception) {
                errorChan.send(e.message ?: "Error fetching time records")
            }
        }
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            fetchTimeRecords()

            _state.update {
                it.copy(isLoading = false)
            }
        }
    }

    fun toDisplayDateTime(date: ZonedDateTime?): String {
        if (date == null) {
            return "-"
        }

        Log.i(
            "TimeRecordsViewModel",
            "toDisplayDateTime: ${date.toLocalDateTime()} ${ZonedDateTime.now().toLocalTime()}"
        )

        val now = ZonedDateTime.now()

        // If today, display time only
        if (date.toLocalDate() == now.toLocalDate()) {
            return date.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        // If yesterday, display "Yesterday"
        if (date.toLocalDate() == now.toLocalDate().minusDays(1)) {
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

            updateTimer()
        } else {
            _state.update {
                it.copy(
                    isTracking = false,
                    startedAt = null,
                    currentTimeSeconds = 0
                )
            }
        }
    }

    private fun calculateSecondsElapsed(
        start: ZonedDateTime?,
        end: ZonedDateTime? = ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault())
    ): Int {
        if (start == null || end == null) {
            return 0
        }

        val nowEpochSeconds = end.toEpochSecond()
        val startEpochSeconds = start.toEpochSecond()
        return (nowEpochSeconds - startEpochSeconds).toInt()
    }

    fun displayDifference(start: ZonedDateTime?, end: ZonedDateTime?): String {
        if (start == null || end == null) {
            return ""
        }

        val seconds = calculateSecondsElapsed(start, end)
        return secondsToTime(seconds)
    }

    fun startTimer() {
        viewModelScope.launch {
            var timeRecords: List<TimeRecord> = emptyList()
            try {
                timeRecordsRepository.startTimer()
                timeRecords = timeRecordsRepository.getTimeRecords()

                _state.update {
                    it.copy(
                        isTracking = true,
                        timeRecords = timeRecords,
                        startedAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    )
                }

                updateTimer()
            } catch (e: Exception) {
                errorChan.send(e.message ?: "Error starting timer")
            }
        }
    }

    private fun updateTimer() {
        viewModelScope.launch {
            if (state.value.timerRunning) {
                return@launch
            }
            _state.update { it.copy(timerRunning = true) }

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
            try {
                timeRecordsRepository.stopTimer()
                _state.update {
                    it.copy(
                        timeRecords = timeRecordsRepository.getTimeRecords(),
                        isTracking = false,
                        currentTimeSeconds = 0
                    )
                }
            } catch (e: Exception) {
                errorChan.send(e.message ?: "Error stopping timer")
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

    fun selectCurrentRecord() {
        val currentRecord = state.value.timeRecords.find { it.end.isBlank() }
        if (currentRecord != null) {
            onSelectRecord(currentRecord)
        }
    }

    fun clearSelectedRecord() {
        _state.update { it.copy(selectedRecord = null) }
    }

    fun deleteSelectedRecord() {
        viewModelScope.launch {
            val selectedRecord = state.value.selectedRecord ?: return@launch

            try {
                timeRecordsRepository.deleteTimeRecord(selectedRecord.id)
                _state.update {
                    it.copy(
                        timeRecords = timeRecordsRepository.getTimeRecords(),
                        selectedRecord = null
                    )
                }
            } catch (e: Exception) {
                errorChan.send(e.message ?: "Error deleting time record")
            }
        }
    }

    fun tagSelectedRecord(tag: String) {
        viewModelScope.launch {
            val selectedRecord = state.value.selectedRecord ?: return@launch

            try {
                val timeRecord = timeRecordsRepository.tagTimeRecord(selectedRecord.id, tag)
                _state.update {
                    it.copy(
                        timeRecords = timeRecordsRepository.getTimeRecords(),
                        selectedRecord = timeRecord
                    )
                }
            } catch (e: Exception) {
                errorChan.send(e.message ?: "Error tagging time record")
            }
        }
    }

    fun untagSelectedRecord(tag: String) {
        viewModelScope.launch {
            val selectedRecord = state.value.selectedRecord ?: return@launch
            try {
                val timeRecord = timeRecordsRepository.untagTimeRecord(selectedRecord.id, tag)
                _state.update {
                    it.copy(
                        timeRecords = timeRecordsRepository.getTimeRecords(),
                        selectedRecord = timeRecord
                    )
                }
            } catch (e: Exception) {
                errorChan.send(e.message ?: "Error untagging time record")
            }
        }
    }

    fun modifySelectedRecordDate(start: ZonedDateTime?, end: ZonedDateTime?) {
        viewModelScope.launch {
            var startTimestamp: Optional<String?> = Optional.Absent
            var endTimestamp: Optional<String?> = Optional.Absent

            if (start != null) {
                startTimestamp =
                    Optional.Present(
                        start.format(
                            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                                .withZone(ZoneId.of("UTC"))
                        )
                    )
            }
            if (end != null) {
                endTimestamp =
                    Optional.Present(
                        end.format(
                            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                                .withZone(ZoneId.of("UTC"))
                        )
                    )
            }

            val selectedRecord = state.value.selectedRecord ?: return@launch

            try {
                val timeRecord = timeRecordsRepository.modifyTimeRecordDate(
                    selectedRecord.id,
                    startTimestamp,
                    endTimestamp
                )
                _state.update {
                    it.copy(
                        timeRecords = timeRecordsRepository.getTimeRecords(),
                        selectedRecord = timeRecord
                    )
                }
            } catch (e: Exception) {
                errorChan.send(e.message ?: "Error modifying time record date")
            }
        }
    }
}
