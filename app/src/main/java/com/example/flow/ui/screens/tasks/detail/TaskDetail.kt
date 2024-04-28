package com.example.flow.ui.screens.tasks.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flow.TaskNavGraph
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.components.tasks.CreateProjectSheet
import com.example.flow.ui.components.tasks.TaskDateSheet
import com.example.flow.ui.components.tasks.TaskTimeSheet
import com.example.flow.ui.screens.destinations.TaskTagsScreenDestination
import com.example.flow.ui.screens.destinations.TimeTagsScreenDestination
import com.example.flow.ui.screens.tasks.TasksViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@TaskNavGraph
@Destination
@Composable
fun TaskDetail(
    navController: NavController,
    viewModel: TasksViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val dateSheet = remember { mutableStateOf(false) }
    val timeSheet = remember { mutableStateOf(false) }
    val projectSheet = remember { mutableStateOf(false) }
    var projectExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf(state.selectedTask?.description ?: "") }
    val selectedDueDate =
        remember { mutableStateOf<ZonedDateTime?>(state.selectedTask?.dueDateTime) }
    var project by remember { mutableStateOf(state.selectedTask?.project) }
    var priority by remember { mutableStateOf(state.selectedTask?.priority) }

    if (dateSheet.value) {
        TaskDateSheet(
            onDismiss = {
                scope.launch {
                    dateSheet.value = false
                }
            },
            onCreate = {
                // Save current selected time
                val time = selectedDueDate.value?.toLocalTime()
                selectedDueDate.value = it
                selectedDueDate.value = selectedDueDate.value?.withHour(time?.hour ?: 0)
                selectedDueDate.value = selectedDueDate.value?.withMinute(time?.minute ?: 0)
                viewModel.editTask(state.selectedTask!!.id, dueDate = selectedDueDate.value)
                dateSheet.value = false
            },
            selectedDate = selectedDueDate.value
        )
    }

    if (timeSheet.value) {
        TaskTimeSheet(
            onDismiss = {
                scope.launch {
                    timeSheet.value = false
                }
            },
            onCreate = {
                // Set the time of the selectedDueDate
                if (selectedDueDate.value == null) {
                    selectedDueDate.value = ZonedDateTime.now()
                }
                selectedDueDate.value =
                    selectedDueDate.value?.withHour(it.hour)?.withMinute(it.minute)
                scope.launch {
                    timeSheet.value = false
                    viewModel.editTask(state.selectedTask!!.id, dueDate = selectedDueDate.value)
                }
            },
            selectedTime = selectedDueDate.value
        )
    }

    if (projectSheet.value) {
        CreateProjectSheet(onDismiss = {
            scope.launch {
                projectSheet.value = false
            }
        },
            onCreate = {
                project = it
                scope.launch {
                    projectSheet.value = false
                }

                viewModel.editTask(state.selectedTask!!.id, project = it)
            })
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
                    Text("Are you sure you want to delete this task?")
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
                            viewModel.deleteTask(state.selectedTask!!.id)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Task Detail") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
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
        bottomBar = {
            BottomNav(navController = navController)
        }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Description")
                BasicTextField(
                    value = description,
                    textStyle = TextStyle.Default.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    onValueChange = { description = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }),
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                )
                LaunchedEffect(key1 = description) {
                    if (description != state.selectedTask?.description) {
                        delay(2000);
                        viewModel.editTask(state.selectedTask!!.id, description)
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Due", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = {
                            timeSheet.value = true
                        }) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Select time",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(16.dp)
                            )
                            Text(
                                if (state.selectedTask?.dueDateTime != null) {
                                    state.selectedTask?.dueDateTime?.toLocalTime()?.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    ).toString()
                                } else {
                                    "Select time"
                                }
                            )
                        }
                        FilledTonalButton(onClick = {
                            dateSheet.value = true
                        }) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Select date",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(16.dp)
                            )
                            Text(
                                if (state.selectedTask?.dueDateTime != null) {
                                    state.selectedTask?.dueDateTime?.toLocalDate()?.format(
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    ).toString()
                                } else {
                                    "Select date"
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Project", fontWeight = FontWeight.Bold)
                    Box {
                        DropdownMenu(
                            expanded = projectExpanded,
                            onDismissRequest = { projectExpanded = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Add project") },
                                onClick = {
                                    scope.launch {
                                        projectSheet.value = true
                                        projectExpanded = false
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add project"
                                    )
                                })
                            DropdownMenuItem(
                                text = { Text(text = "No project") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "No project"
                                    )
                                },
                                onClick = {
                                    project = null
                                    projectExpanded = false
                                })
                            state.recentProjects.forEach {
                                DropdownMenuItem(text = {
                                    Text(text = it)
                                }, onClick = {
                                    project = it
                                    viewModel.editTask(state.selectedTask!!.id, project = it)
                                    projectExpanded = false
                                })
                            }


                        }
                        FilledTonalButton(onClick = {
                            projectExpanded = true
                        }) {
                            Text(
                                if (project != "") project!! else "Select project",
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Priority", fontWeight = FontWeight.Bold)
                    Box {
                        DropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false },
                        ) {
                            DropdownMenuItem(text = {
                                Text(text = "Low")
                            }, onClick = {
                                priority = "L"
                                viewModel.editTask(state.selectedTask!!.id, priority = "L")
                                priorityExpanded = false
                            })
                            DropdownMenuItem(text = {
                                Text(text = "Medium")
                            }, onClick = {
                                priority = "M"
                                viewModel.editTask(state.selectedTask!!.id, priority = "M")
                                priorityExpanded = false
                            })
                            DropdownMenuItem(text = {
                                Text(text = "High")
                            }, onClick = {
                                priority = "H"
                                viewModel.editTask(state.selectedTask!!.id, priority = "H")
                                priorityExpanded = false
                            })
                            DropdownMenuItem(
                                text = { Text(text = "No priority") },
                                onClick = {
                                    priority = ""
                                    viewModel.editTask(state.selectedTask!!.id, priority = "")
                                    priorityExpanded = false
                                })
                        }
                        FilledTonalButton(onClick = { priorityExpanded = !priorityExpanded },
                            content = {
                                if (priority != null && priority != "") {
                                    when (priority) {
                                        "L" -> {
                                            Text(text = "Low")
                                        }

                                        "M" -> {
                                            Text(text = "Medium")
                                        }

                                        else -> {
                                            Text(text = "High")
                                        }
                                    }
                                } else {
                                    Text(text = "Priority")
                                }
                            })
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
                        navController.navigate(TaskTagsScreenDestination.route)
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
                    if (state.selectedTask?.tags?.size!! > 0) {
                        for (tag in state.selectedTask?.tags!!) {
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
