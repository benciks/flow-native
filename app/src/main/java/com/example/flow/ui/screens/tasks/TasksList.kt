package com.example.flow.ui.screens.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flow.TaskNavGraph
import com.example.flow.data.model.TaskFilterModel
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.components.tasks.CreateTaskSheet
import com.example.flow.ui.components.tasks.TaskDateSheet
import com.example.flow.ui.components.tasks.TaskItem
import com.example.flow.ui.components.tasks.TaskTimeSheet
import com.example.flow.ui.screens.destinations.TaskDetailDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.DestinationLambda
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@TaskNavGraph(start = true)
@Destination
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TasksViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var filterOpen by remember { mutableStateOf(false) }

    val now = ZonedDateTime.now()

    val tasksToday = state.tasks.filter { task ->
        val dueDateTime = task.dueDateTime
        dueDateTime != null && dueDateTime.isBefore(now.with(LocalTime.MAX))
    }.sortedBy { it.dueDateTime }

    val tasksTomorrow = state.tasks.filter { task ->
        val dueDateTime = task.dueDateTime
        dueDateTime != null && dueDateTime.isAfter(now.with(LocalTime.MAX)) && dueDateTime.isBefore(
            now.plusDays(1).with(LocalTime.MAX)
        )
    }.sortedBy { it.dueDateTime }

    val tasksUpcoming = state.tasks.filter { task ->
        val dueDateTime = task.dueDateTime
        dueDateTime == null || dueDateTime.isAfter(now.plusDays(1).with(LocalTime.MAX))
    }.sortedBy { it.dueDateTime }

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            // fetch something
            viewModel.fetchTasks()
            refreshState.endRefresh()
        }
    }

    if (filterOpen) {
        TaskFilterSheet(
            onDismiss = {
                filterOpen = false
                viewModel.applyFilter(it)
                viewModel.fetchTasks()
            },
            recentProjects = state.recentProjects,
            recentTags = state.recentTags,
            currentFilter = state.filter
        )
    }


    if (showSheet) {
        CreateTaskSheet(
            onDismiss = { showSheet = false },
            onCreate = { description, due, project, priority ->
                viewModel.createTask(description, due, project, priority)
            })
    }

    Scaffold(
        modifier = Modifier.nestedScroll(refreshState.nestedScrollConnection),
        bottomBar = {
            BottomNav(navController = navController)
        },
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
            Box(Modifier.padding(paddingValues)) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 8.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, top = 8.dp)
                            ) {
                                Text(
                                    text = "Tasks",
                                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (state.filter == TaskFilterModel(status = "pending")) {
                                    OutlinedButton(onClick = {
                                        filterOpen = true
                                    }) {
                                        Text("Filter")
                                    }
                                } else {
                                    Button(onClick = { filterOpen = true }) {
                                        Text("Filter")
                                    }
                                }
                            }
                        }
                    }
                    if (tasksToday.isNotEmpty()) {
                        item {
                            Text(
                                text = "Today",
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = 8.dp, top = 16.dp)
                                    .alpha(0.5f)
                            )
                        }
                    }
                    items(tasksToday, key = { it.uuid }) {
                        Box(modifier = Modifier.animateItemPlacement()) {
                            TaskItem(
                                it,
                                onCheck = { viewModel.markTaskDone(it.id) },
                                onClick = {
                                    viewModel.selectTask(it)
                                    navController.navigate(TaskDetailDestination.route)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (tasksTomorrow.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tomorrow",
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = 8.dp, top = 16.dp)
                                    .alpha(0.5f)
                            )
                        }
                    }
                    items(tasksTomorrow, key = { it.uuid }) {
                        Box(modifier = Modifier.animateItemPlacement()) {
                            TaskItem(
                                it,
                                onCheck = { viewModel.markTaskDone(it.id) },
                                onClick = {
                                    viewModel.selectTask(it)
                                    navController.navigate(TaskDetailDestination.route)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (tasksUpcoming.isNotEmpty()) {
                        item {
                            Text(
                                text = "Upcoming",
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = 8.dp, top = 16.dp)
                                    .alpha(0.5f)
                            )
                        }
                    }
                    items(tasksUpcoming, key = { it.uuid }) {
                        Box(modifier = Modifier.animateItemPlacement()) {
                            TaskItem(
                                it,
                                onCheck = { viewModel.markTaskDone(it.id) },
                                onClick = {
                                    viewModel.selectTask(it)
                                    navController.navigate(TaskDetailDestination.route)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                PullToRefreshContainer(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = refreshState,
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFilterSheet(
    onDismiss: (filter: TaskFilterModel) -> Unit,
    recentProjects: List<String> = emptyList(),
    recentTags: List<String> = emptyList(),
    currentFilter: TaskFilterModel,
) {
    val modalState = rememberModalBottomSheetState()
    var filter by remember { mutableStateOf(currentFilter) }
    var dateSheet by remember { mutableStateOf(false) }
    var timeSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var selectedDueDate by remember { mutableStateOf(filter.due) }

    val statuses = listOf("pending", "active", "completed", "deleted")

    if (dateSheet) {
        TaskDateSheet(onDismiss = {
            scope.launch {
                dateSheet = false
                modalState.show()
            }
        },
            onCreate = {
                selectedDueDate = it
                scope.launch {
                    dateSheet = false
                    timeSheet = true
                }
            })
    }

    if (timeSheet) {
        TaskTimeSheet(onDismiss = {
            scope.launch {
                timeSheet = false
                modalState.show()
            }
        },
            onCreate = {
                // Set the time of the selectedDueDate
                selectedDueDate =
                    selectedDueDate?.withHour(it.hour)?.withMinute(it.minute)

                filter = filter.copy(due = selectedDueDate)
                scope.launch {
                    timeSheet = false
                    modalState.show()
                }
            })
    }

    ModalBottomSheet(
        sheetState = modalState,
        onDismissRequest = { onDismiss(filter) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp, 4.dp, 16.dp, 70.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    filter = TaskFilterModel(
                        status = "pending",
                        project = null,
                        priority = null,
                        due = null,
                        tags = emptyList(),
                        description = ""
                    )
                }) {
                    Text("Clear")
                }
            }
            Text("Description", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(
                value = filter.description,
                placeholder = { Text("Enter description") },
                onValueChange = { filter = filter.copy(description = it) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Due", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    content = {
                        if (filter.due != null) Text(
                            filter.due!!.format(
                                DateTimeFormatter.ofPattern(
                                    "dd.MM.yyyy HH:mm"
                                )
                            )
                        ) else Text("Select date")
                    },
                    onClick = {
                        scope.launch {
                            dateSheet = true
                            modalState.hide()
                        }
                    },
                )

                if (filter.due != null) {
                    TextButton(onClick = {
                        filter = filter.copy(due = null)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear due date")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (status in statuses) {
                    FilterChip(selected = filter.status == status, onClick = {
                        filter = filter.copy(status = status)
                    }, label = { Text(status.capitalize()) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Project", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = filter.project == null, onClick = {
                    filter = filter.copy(project = null)
                }, label = { Text("All") })

                for (projectName in recentProjects) {
                    FilterChip(selected = filter.project == projectName, onClick = {
                        filter = filter.copy(project = projectName)
                    }, label = { Text(projectName) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Priority", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = filter.priority == null, onClick = {
                    filter = filter.copy(priority = null)
                }, label = { Text("All") })

                for (priority in listOf("L", "M", "H")) {
                    FilterChip(selected = filter.priority == priority, onClick = {
                        filter = filter.copy(priority = priority)
                    }, label = {
                        Text(priority.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                            ) else it.toString()
                        })
                    })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Tags", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = filter.tags.isEmpty(), onClick = {
                    filter = filter.copy(tags = emptyList())
                }, label = { Text("All") })

                for (tag in recentTags) {
                    FilterChip(selected = filter.tags.contains(tag), onClick = {
                        val tags = filter.tags.toMutableList()
                        if (tags.contains(tag)) {
                            tags.remove(tag)
                        } else {
                            tags.add(tag)
                        }
                        filter = filter.copy(tags = tags)
                    }, label = { Text(tag) })
                }
            }
        }
    }
}






