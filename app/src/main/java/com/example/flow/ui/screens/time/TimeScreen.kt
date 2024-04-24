package com.example.flow.ui.screens.time

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.flow.TimeNavGraph
import com.example.flow.data.model.TimeRecord
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.screens.destinations.TimeDetailScreenDestination
import com.example.flow.ui.screens.destinations.TimeTagsScreenDestination
import com.example.flow.ui.theme.Primary
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.time.LocalDateTime
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@TimeNavGraph(start = true)
@Destination
@Composable
fun TimeScreen(
    navController: NavController,
    viewModel: TimeRecordsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(

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
                    TimerHeader(
                        isTracking = state.isTracking,
                        startTimer = viewModel::startTimer,
                        stopTimer = viewModel::stopTimer,
                        currentTime = state.currentTimeSeconds,
                        secondsToTime = viewModel::secondsToTime,
                        startedAt = state.startedAt,
                        navController = navController,
                        selectCurrentRecord = viewModel::selectCurrentRecord
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Time Records",
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                items(state.timeRecords, key = { it.end }) {
                    Box(modifier = Modifier.animateItemPlacement()) {
                        TimeRecordItem(
                            it,
                            viewModel::toDisplayDateTime,
                            viewModel::displayDifference,
                            navController,
                            viewModel::onSelectRecord
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TimerHeader(
    isTracking: Boolean = false,
    startTimer: () -> Unit = {},
    stopTimer: () -> Unit = {},
    currentTime: Int = 0,
    secondsToTime: (Int) -> String = { it.toString() },
    startedAt: String? = null,
    navController: NavController,
    selectCurrentRecord: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(12.dp))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = secondsToTime(currentTime),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = MaterialTheme.typography.displayLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (!isTracking) startTimer() else stopTimer() })
                    {
                        if (!isTracking) Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Start"
                        ) else Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                }
            }
            if (isTracking) {
                Text(
                    text = "Started at $startedAt",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.SemiBold
                )

                FilledTonalButton(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        selectCurrentRecord()
                        navController.navigate(TimeTagsScreenDestination.route)

                    }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Tag",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(text = "Modify tags")
                }
            } else {
                Text(
                    text = "No active tracking",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TimeRecordItem(
    record: TimeRecord,
    toDisplayDateTime: (ZonedDateTime?) -> String = { it.toString() },
    displayDifference: (ZonedDateTime?, ZonedDateTime?) -> String = { _, _ -> "12:33" },
    navController: NavController,
    onSelectItem: (timeRecord: TimeRecord) -> Unit = {}
) {
    OutlinedCard(
        onClick = {
            onSelectItem(record)
            navController.navigate(TimeDetailScreenDestination.route)
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row {
                        Text(
                            text = "Start:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            modifier = Modifier.width(56.dp)
                        )
                        Text(
                            text = toDisplayDateTime(record.startDateTime),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row {
                        Text(
                            text = "End:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            modifier = Modifier.width(56.dp)
                        )
                        if (record.end.isEmpty()) {
                            Text(
                                text = "Ongoing",
                                color = Primary,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = toDisplayDateTime(record.endDateTime),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (record.end.isNotEmpty()) {
                    Column {
                        Text(
                            text = displayDifference(record.startDateTime, record.endDateTime),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            if (record.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    record.tags.forEach {
                        Box(
                            modifier = Modifier
                                .clip(shape = MaterialTheme.shapes.large)
                                .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Text(
                                text = it,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(6.dp, 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}