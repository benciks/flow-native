package com.example.flow.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.flow.ui.screens.NavGraphs
import com.example.flow.ui.screens.appCurrentDestinationAsState
import com.example.flow.ui.screens.destinations.CalendarScreenDestination
import com.example.flow.ui.screens.destinations.Destination
import com.example.flow.ui.screens.destinations.ProfileScreenDestination
import com.example.flow.ui.screens.destinations.TasksScreenDestination
import com.example.flow.ui.screens.destinations.TimeScreenDestination
import com.example.flow.ui.screens.startAppDestination

class NavbarItem(
    val icon: ImageVector,
    val label: String,
    val route: Destination,
)

val items = listOf(
    NavbarItem(Icons.Filled.Timer, "Time records", TimeScreenDestination),
    NavbarItem(Icons.Filled.Task, "Tasks", TasksScreenDestination),
    NavbarItem(Icons.Filled.CalendarMonth, "Calendar", CalendarScreenDestination),
    NavbarItem(Icons.Filled.AccountCircle, "Profile", ProfileScreenDestination),
)

@Composable
fun BottomNav(navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
    ) {
        val currentDestination: Destination = navController.appCurrentDestinationAsState().value
            ?: NavGraphs.root.startAppDestination

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                selected = currentDestination == item.route,
                onClick = {
                    navController.navigate(item.route.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
