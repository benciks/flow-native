package com.example.flow.ui.screens.time.detail

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flow.TimeNavGraph
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.components.TimePickerDialog
import com.example.flow.ui.screens.destinations.TimeTagsScreenDestination
import com.example.flow.ui.screens.time.TimeRecordsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@TimeNavGraph
@Destination
@Composable
fun TimeDetailScreen(
    navController: NavController,
    viewModel: TimeRecordsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var editingTime by remember { mutableStateOf("start") }
    val startTimePickerState = rememberTimePickerState(
        initialHour = state.selectedRecord?.startDateTime?.hour ?: 0,
        initialMinute = state.selectedRecord?.startDateTime?.minute ?: 0
    )

    Log.i(
        "TimeDetailScreen",
        "state.selectedRecord?.startDateTime: ${state.selectedRecord?.startDateTime}"
    )
    val endTimePickerState: TimePickerState = if (state.selectedRecord?.endDateTime != null) {
        rememberTimePickerState(
            initialHour = state.selectedRecord?.endDateTime?.hour!!,
            initialMinute = state.selectedRecord?.endDateTime?.minute!!
        )
    } else {
        rememberTimePickerState()
    }

    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.selectedRecord?.startDateTime?.toInstant()
            ?.toEpochMilli()!!
    )

    val endDatePickerState = if (state.selectedRecord?.endDateTime != null) {
        rememberDatePickerState(
            initialSelectedDateMillis = state.selectedRecord?.endDateTime?.toInstant()
                ?.toEpochMilli()!!
        )
    } else {
        rememberDatePickerState()
    }

    if (showTimePicker) {
        TimePickerDialog(
            onCancel = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, startTimePickerState.hour)
                cal.set(Calendar.MINUTE, startTimePickerState.minute)
                cal.isLenient = false

                val currentDate =
                    if (editingTime == "start") state.selectedRecord?.startDateTime else state.selectedRecord?.endDateTime
                val dateTime = if (editingTime == "start") {
                    currentDate?.withHour(startTimePickerState.hour)
                        ?.withMinute(startTimePickerState.minute)
                } else {
                    currentDate?.withHour(endTimePickerState.hour)
                        ?.withMinute(endTimePickerState.minute)
                }

                Log.i("TimeDetailScreen", "dateTime: $dateTime")

                // If end time is before start time, show error
                if (editingTime == "end" && dateTime?.isBefore(state.selectedRecord?.startDateTime) == true) {
                    showTimePicker = false
                    snackScope.launch {
                        snackState.showSnackbar("End time cannot be before start time")
                    }
                    return@TimePickerDialog
                }

                if (editingTime == "start" && state.selectedRecord?.endDateTime != null && dateTime != null && dateTime.isAfter(
                        state.selectedRecord?.endDateTime
                    )
                ) {
                    showTimePicker = false
                    snackScope.launch {
                        snackState.showSnackbar("Start time cannot be after end time")
                    }
                    return@TimePickerDialog
                }


                // Set the time
                if (editingTime == "start") {
                    viewModel.modifySelectedRecordDate(dateTime, null)
                } else {
                    viewModel.modifySelectedRecordDate(null, dateTime)
                }

                showTimePicker = false
            },
        ) {
            TimePicker(state = if (editingTime == "start") startTimePickerState else endTimePickerState)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                Button(onClick = {
                    if (editingTime == "start") {
                        val newDate = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(startDatePickerState.selectedDateMillis!!),
                            ZoneOffset.UTC
                        )
                        // Set time to start time
                        val time = state.selectedRecord?.startDateTime?.toLocalTime()
                        val newDateTime = newDate.withHour(time?.hour!!).withMinute(time.minute)

                        viewModel.modifySelectedRecordDate(newDateTime, null)
                    } else {
                        val newDate = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(endDatePickerState.selectedDateMillis!!),
                            ZoneOffset.UTC
                        )
                        // Set time to end time
                        val time = state.selectedRecord?.endDateTime?.toLocalTime()
                        val newDateTime = newDate.withHour(time?.hour!!).withMinute(time.minute)

                        viewModel.modifySelectedRecordDate(null, newDateTime)
                    }

                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
        ) {
            DatePicker(
                state = if (editingTime == "start") startDatePickerState else endDatePickerState
            )
        }
    }


    val openAlertDialog = remember { mutableStateOf(false) }
    if (openAlertDialog.value) {
        Dialog(onDismissRequest = { openAlertDialog.value = false }) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)

                ) {
                    Text("Are you sure you want to delete this entry?")
                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = {
                            openAlertDialog.value = false
                        }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            viewModel.deleteSelectedRecord()
                            navController.popBackStack()
                            openAlertDialog.value = false
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }


    val context = LocalContext.current
    LaunchedEffect(viewModel, context) {
        viewModel.errorFlow.collect { result ->
            snackState.showSnackbar(result)
        }
    }


    Scaffold(
        bottomBar = {
            BottomNav(navController = navController)
        },
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit Entry") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                            viewModel.clearSelectedRecord()
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            openAlertDialog.value = true
                        },
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackState)
        },
    ) { padding ->
        LazyColumn(
            contentPadding = padding, modifier = Modifier.padding(
                top = 8.dp,
                bottom = 8.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                Column {
                    Text("Total time")
                    Text(
                        viewModel.displayDifference(
                            state.selectedRecord?.startDateTime,
                            state.selectedRecord?.endDateTime
                        ),
                        fontSize = MaterialTheme.typography.displayLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Start", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = {
                                editingTime = "start"
                                showTimePicker = true
                            }) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = "Select time",
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(16.dp)
                                )
                                Text(
                                    state.selectedRecord?.startDateTime?.toLocalTime()!!.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    ).toString(),
                                )
                            }
                            FilledTonalButton(onClick = {
                                editingTime = "start"
                                showDatePicker = true
                            }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Select date",
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(16.dp)
                                )
                                Text(
                                    state.selectedRecord?.startDateTime?.toLocalDate()!!.format(
                                        DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                    ).toString()
                                )
                            }
                        }

                    }

                    if (state.selectedRecord?.endDateTime != null) {
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("End", fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(onClick = {
                                    editingTime = "end"
                                    showTimePicker = true
                                }) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = "Select time",
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(16.dp)
                                    )
                                    Text(
                                        state.selectedRecord?.endDateTime?.toLocalTime()!!.format(
                                            DateTimeFormatter.ofPattern("HH:mm")
                                        ).toString()
                                    )
                                }
                                FilledTonalButton(onClick = {
                                    editingTime = "end"
                                    showDatePicker = true
                                }) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = "Select date",
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(16.dp)
                                    )
                                    Text(
                                        state.selectedRecord?.endDateTime?.toLocalDate()!!.format(
                                            DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                        ).toString()
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 40.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tags", fontWeight = FontWeight.Bold)
                        Button(onClick = {
                            navController.navigate(TimeTagsScreenDestination.route)
                        }) {
                            Icon(
                                Icons.Default.Tag, contentDescription = "Edit tags",
                                Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text("Edit tags")
                        }
                    }

                    FlowRow {
                        if (state.selectedRecord?.tags?.size!! > 0) {
                            for (tag in state.selectedRecord?.tags!!) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(MaterialTheme.shapes.large)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                                ) {
                                    Text(
                                        tag,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(8.dp, 2.dp)
                                    )
                                }
                            }
                        } else {
                            Text("No tags yet, add some!", modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
