package com.example.flow.ui.screens.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.components.tasks.CreateTaskSheet
import com.example.flow.ui.components.tasks.TaskItem
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Destination
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val now = ZonedDateTime.now()

    val tasksToday = state.tasks.filter { task ->
        val dueDateTime = task.dueDateTime
        dueDateTime != null && dueDateTime.isAfter(
            now.minusDays(1).with(LocalTime.MAX)
        ) && dueDateTime.isBefore(now.with(LocalTime.MAX))
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


    if (showSheet) {
        CreateTaskSheet(
            onDismiss = { showSheet = false },
            onCreate = { description, due, project, priority ->
                viewModel.createTask(description, due, project, priority)
            })
    }

    val views = listOf(
        "all",
        "ongoing",
        "completed",
    )

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
                            Text(
                                text = "Tasks",
                                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                for (view in views) {
                                    Text(
                                        text = view.replaceFirstChar { it.uppercase() },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable {
                                                scope.launch {
                                                    viewModel.updateView(view)
                                                }
                                            }
                                            .background(
                                                if (state.view == view) {
                                                    MaterialTheme.colorScheme.primaryContainer
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .padding(16.dp, 6.dp),
                                        color = if (state.view == view) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
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
                                    showSheet = true
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
                                    showSheet = true
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
                                    showSheet = true
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










