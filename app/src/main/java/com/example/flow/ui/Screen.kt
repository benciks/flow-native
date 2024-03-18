package com.example.flow.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    data object Time : Screen("time", Icons.Filled.Timer, "Time")
    data object TimeDetail : Screen("timeDetail", Icons.Filled.Timer, "Time Detail")

    data object Tasks: Screen("tasks", Icons.Filled.Task, "Tasks")
    data object Calendar: Screen("calendar", Icons.Filled.CalendarMonth, "Calendar")
    data object Profile: Screen("profile", Icons.Filled.AccountCircle, "Profile")
}

val items = listOf(
    Screen.Time,
    Screen.Tasks,
    Screen.Calendar,
    Screen.Profile
)
