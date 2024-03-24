package com.example.flow.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    data object Time : Screen("time_list", Icons.Filled.Timer, "Time")
    data object TimeDetail : Screen("time_detail", Icons.Filled.Timer, "Time Detail")

    data object TimeTags : Screen("time_tags", Icons.Filled.Timer, "Time Tags")

    data object Tasks: Screen("tasks", Icons.Filled.Task, "Tasks")
    data object Calendar: Screen("calendar", Icons.Filled.CalendarMonth, "Calendar")
    data object Profile: Screen("profile", Icons.Filled.AccountCircle, "Profile")
}

class NavbarItem(
    val screen: Screen,
    val route: String,
)

val items = listOf(
    NavbarItem(Screen.Time, "time"),
    NavbarItem(Screen.Tasks, "tasks"),
    NavbarItem(Screen.Calendar, "calendar"),
    NavbarItem(Screen.Profile, "profile"),
)
