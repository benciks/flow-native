package com.example.flow.ui.screens.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flow.data.model.Task
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Destination
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        CreateTaskDescriptionSheet(
            onDismiss = { showSheet = false },
            onCreate = { description, due ->
                viewModel.createTask(description, due)
            })
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showSheet = true
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create task"
                )
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 8.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Tasks",
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                items(state.tasks, key = { it.uuid }) {
                    Box(modifier = Modifier.animateItemPlacement()) {
                        TaskItem(
                            it,
                            onClick = {
                                showSheet = true
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun TaskItem(
    task: Task = Task(
        uuid = "1",
        id = "1",
        description = "This is extremely long title i wonder what happens with it",
        status = "completed",
        entry = "2021-10-10T10:00:00Z",
        modified = "2021-10-10T10:00:00Z",
        urgency = 0.6,
        priority = "H",
        due = "2021-10-10T10:00:00Z",
        dueDateTime = ZonedDateTime.now(),
        project = "Project",
        tags = emptyList()
    ),
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        onClick = {
            onClick()
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.description,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1F, fill = false)
                        .padding(end = 16.dp)
                )
                Badge(
                    content = { Text(text = task.status) },
                    containerColor = when (task.status) {
                        "pending" -> MaterialTheme.colorScheme.secondaryContainer
                        "completed" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            }
            if (task.due.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Due: ${task.dueDateTime?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}")
            }
            if (task.priority.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Priority: ${task.priority}")
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDescriptionSheet(
    onDismiss: () -> Unit,
    onCreate: (description: String, due: ZonedDateTime?) -> Unit
) {
    val modalState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    )
    {
        var text by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        val keyboardController = LocalSoftwareKeyboardController.current

        val dateSheet = remember { mutableStateOf(false) }
        val timeSheet = remember { mutableStateOf(false) }

        val selectedDueDate = remember { mutableStateOf<ZonedDateTime?>(null) }

        if (dateSheet.value) {
            CreateTaskDateSheet(onDismiss = {
                scope.launch {
                    dateSheet.value = false
                    modalState.show()
                }
            },
                onCreate = {
                    selectedDueDate.value = it
                    scope.launch {
                        dateSheet.value = false
                        timeSheet.value = true
                    }
                })
        }

        if (timeSheet.value) {
            CreateTaskTimeSheet(onDismiss = {
                scope.launch {
                    timeSheet.value = false
                    modalState.show()
                }
            },
                onCreate = {
                    // Set the time of the selectedDueDate
                    selectedDueDate.value =
                        selectedDueDate.value?.withHour(it.hour)?.withMinute(it.minute)
                    scope.launch {
                        timeSheet.value = false
                        modalState.show()
                    }
                })
        }

        Row {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Enter task description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                )

            }
            Button(onClick = {
                scope.launch {
                    onCreate(text, selectedDueDate.value)
                    onDismiss()
                }
            }, content = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            },
                modifier = Modifier
                    .padding(end = 12.dp)
            )
        }

        TextButton(onClick = {
            scope.launch {
                keyboardController?.hide()
                modalState.hide()
                dateSheet.value = true
            }
        },
            content = {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Add due date",
                    modifier = Modifier.padding(end = 4.dp)
                )
                if (selectedDueDate.value != null) {
                    Text(text = selectedDueDate.value!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                } else {
                    Text(text = "Add due date")
                }
            }
        )


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDateSheet(onDismiss: () -> Unit, onCreate: (ZonedDateTime) -> Unit = {}) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    )
    {
        val dateState = rememberDatePickerState()
        val scope = rememberCoroutineScope()

        DatePicker(
            state = dateState
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                scope.launch {
                    onCreate(
                        ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(dateState.selectedDateMillis!!),
                            ZoneId.systemDefault()
                        )
                    )
                }
            }, content = {
                Text(text = "Done")
            })
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskTimeSheet(onDismiss: () -> Unit, onCreate: (ZonedDateTime) -> Unit = {}) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    )
    {
        val timeState = rememberTimePickerState()
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimePicker(
                state = timeState
            )
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                scope.launch {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                    cal.set(Calendar.MINUTE, timeState.minute)

                    onCreate(
                        ZonedDateTime.ofInstant(
                            cal.toInstant(),
                            ZoneId.systemDefault()
                        )
                    )
                }
            }, content = {
                Text(text = "Done")
            })
        }
    }
}