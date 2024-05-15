package com.example.flow.ui.screens.tasks.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flow.TaskNavGraph
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.components.tasks.CreateProjectSheet
import com.example.flow.ui.components.tasks.TaskDateSheet
import com.example.flow.ui.components.tasks.TaskItem
import com.example.flow.ui.components.tasks.TaskTimeSheet
import com.example.flow.ui.screens.destinations.TaskDetailDestination
import com.example.flow.ui.screens.destinations.TaskTagsScreenDestination
import com.example.flow.ui.screens.destinations.TasksScreenDestination
import com.example.flow.ui.screens.tasks.TasksViewModel
import com.ramcosta.composedestinations.annotation.Destination
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
    var untilDateSheet by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf(state.selectedTask?.description ?: "") }
    val selectedDueDate =
        remember { mutableStateOf<ZonedDateTime?>(state.selectedTask?.dueDateTime) }
    var project by remember { mutableStateOf(state.selectedTask?.project) }
    var priority by remember { mutableStateOf(state.selectedTask?.priority) }

    var blockedExpanded by remember { mutableStateOf(false) }
    var selectedBlockedTasks by remember {
        mutableStateOf(state.allTasks.filter {
            // Id in blocked by
            state.selectedTask?.depends?.contains(it.uuid) == true
        })
    }
    var canRecur by remember {
        mutableStateOf(
            (state.selectedTask?.parent == null || state.selectedTask?.status == "recurring") && selectedDueDate.value != null
        )
    }
    var frequency by remember { mutableStateOf(state.selectedTask?.recur ?: "") }
    var selectedUntil by remember { mutableStateOf(state.selectedTask?.untilDateTime) }
    var lazyColumnState = rememberLazyListState()

    if (untilDateSheet) {
        TaskDateSheet(
            onDismiss = {
                scope.launch {
                    untilDateSheet = false
                }
            },
            onCreate = {
                // Save current selected time
                val time = selectedUntil?.toLocalTime()
                selectedUntil = it
                selectedUntil = selectedUntil?.withHour(time?.hour ?: 0)
                selectedUntil = selectedUntil?.withMinute(time?.minute ?: 0)
                viewModel.editTask(state.selectedTask!!.id, until = selectedUntil)
                untilDateSheet = false
            },
            selectedDate = selectedUntil
        )
    }

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
                canRecur =
                    state.selectedTask?.parent == null || state.selectedTask?.status == "recurring"
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
                canRecur =
                    state.selectedTask?.parent == null || state.selectedTask?.status == "recurring"
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
                            viewModel.deleteTask(state.selectedTask!!.uuid)
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

    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel, context) {
        viewModel.errorFlow.collect { result ->
            snackBarHostState.showSnackbar(result)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Task Detail") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Always pop till the TaskList screen
                            navController.popBackStack(TasksScreenDestination.route, false)
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
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyColumnState
            ) {
                item {
                    Text("Description")
                    BasicTextField(
                        value = description,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
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

                }
                item {
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
                                    if (selectedDueDate.value != null) {
                                        selectedDueDate.value?.toLocalTime()?.format(
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
                                    if (selectedDueDate.value != null) {
                                        selectedDueDate.value?.toLocalDate()?.format(
                                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                        ).toString()
                                    } else {
                                        "Select date"
                                    }
                                )
                            }
                        }
                    }
                }

                item {
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
                }

                item {
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
                }

                item {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
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
                }


                item {
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

                item {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var forbiddenStatuses = listOf("completed", "deleted", "recurring")
                        Text("Blocked By", fontWeight = FontWeight.Bold)
                        Box {
                            DropdownMenu(
                                expanded = blockedExpanded,
                                onDismissRequest = { blockedExpanded = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                for (task in state.allTasks) {
                                    if (task.id != state.selectedTask?.id && !forbiddenStatuses.contains(
                                            task.status
                                        )
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                if (selectedBlockedTasks.contains(task)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "check",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = task.description,
                                                        )
                                                    }
                                                } else {
                                                    Text(text = task.description)
                                                }
                                            },
                                            onClick = {
                                                val blockedBy =
                                                    selectedBlockedTasks.map { it.uuid }
                                                        .toMutableList()
                                                if (blockedBy.contains(task.uuid)) {
                                                    blockedBy.remove(task.uuid)
                                                } else {
                                                    blockedBy.add(task.uuid)
                                                }

                                                selectedBlockedTasks =
                                                    blockedBy.mapNotNull { uuid ->
                                                        state.allTasks.find { it.uuid == uuid }
                                                    }

                                                viewModel.editTask(
                                                    state.selectedTask!!.id,
                                                    depends = selectedBlockedTasks.map { it.uuid }
                                                )
                                            }
                                        )
                                    }
                                }

                            }
                            FilledTonalButton(onClick = { blockedExpanded = !blockedExpanded },
                                content = {
                                    Text("Select task")
                                })
                        }
                    }
                }
                if (selectedBlockedTasks.isNotEmpty()) {
                    for (task in selectedBlockedTasks) {
                        item {
                            TaskItem(
                                task,
                                disabled = true,
                                onClick = {
                                    viewModel.selectTask(task)
                                    navController.navigate(TaskDetailDestination.route)
                                }
                            )
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Recurring", fontWeight = FontWeight.Bold)
                        if (state.selectedTask?.parent != null) {
                            Text("This task is a recurring child task. To edit recurrence, edit the parent task.")

                            val parentTask =
                                state.allTasks.find { it.uuid == state.selectedTask?.parent }
                            TaskItem(parentTask!!, disabled = true, onClick = {
                                viewModel.selectTask(parentTask)
                                navController.navigate(TaskDetailDestination.route)
                            })
                        } else {
                            Text(
                                "To set recurrence, due date has to be set. Once the recurrence is set, it cannot be changed.",
                                color = Color.Gray
                            )
                        }
                    }
                }
                if (canRecur) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recurring", fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = frequency,
                                onValueChange = {
                                    frequency = it
                                },
                                label = { Text("Frequency") },
                                singleLine = true,
                                readOnly = state.selectedTask?.recur != null
                            )

                            LaunchedEffect(key1 = frequency) {
                                if (frequency != state.selectedTask?.recur) {
                                    delay(2000);
                                    viewModel.editTask(
                                        state.selectedTask!!.id,
                                        recurring = frequency
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Until", fontWeight = FontWeight.Bold)
                            FilledTonalButton(onClick = { untilDateSheet = true }) {
                                if (selectedUntil != null) {
                                    Text(
                                        selectedUntil?.toLocalDate()?.format(
                                            DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                        ).toString()
                                    )
                                } else {
                                    Text("Select date")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
