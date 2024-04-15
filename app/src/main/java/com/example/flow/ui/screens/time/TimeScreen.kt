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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.flow.domain.model.TimeRecord
import com.example.flow.ui.theme.Gray500
import com.example.flow.ui.theme.Gray600
import com.example.flow.ui.theme.Primary
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeScreen(
    navController: NavHostController,
    viewModel: TimeRecordsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold { paddingValues ->
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
                            .padding(top = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Time Records",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                items(state.timeRecords, key = { it.start }) {
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
    navController: NavHostController,
    selectCurrentRecord: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(12.dp))
            .background(color = Primary)
            .animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = secondsToTime(currentTime),
                    color = Color.Black,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.SemiBold
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
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                FilledTonalButton(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        selectCurrentRecord()
                        navController.navigate("time_tags")

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
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TimeRecordItem(
    record: TimeRecord,
    toDisplayDateTime: (LocalDateTime?) -> String = { it.toString() },
    displayDifference: (LocalDateTime?, LocalDateTime?) -> String = { _, _ -> "12:33" },
    navController: NavHostController,
    onSelectItem: (timeRecord: TimeRecord) -> Unit = {}
) {
    Card(
        onClick = {
            onSelectItem(record)
            navController.navigate("time_detail")
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(12.dp))
            .background(color = Color.White)
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
                            color = Gray600,
                            fontSize = 16.sp,
                            modifier = Modifier.width(56.dp)
                        )
                        Text(
                            text = toDisplayDateTime(record.startDateTime),
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row {
                        Text(
                            text = "End:",
                            color = Gray600,
                            fontSize = 16.sp,
                            modifier = Modifier.width(56.dp)
                        )
                        if (record.end.isEmpty()) {
                            Text(
                                text = "Ongoing",
                                color = Gray600,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = toDisplayDateTime(record.endDateTime),
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (record.end.isNotEmpty()) {
                    Column {
                        Text(
                            text = displayDifference(record.startDateTime, record.endDateTime),
                            fontSize = 16.sp,
                            color = Gray500
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
                                .clip(shape = RoundedCornerShape(8.dp))
                                .background(color = Color.Black)
                        ) {
                            Text(
                                text = it,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(6.dp, 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}