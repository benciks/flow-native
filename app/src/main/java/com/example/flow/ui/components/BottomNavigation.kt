package com.example.flow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.flow.ui.screens.NavGraphs
import com.example.flow.ui.screens.appCurrentDestinationAsState
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
    NavbarItem(Icons.Filled.Timer, "Time", TimeScreenDestination),
    NavbarItem(Icons.Filled.Task, "Tasks", TasksScreenDestination),
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
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (currentDestination == item.route) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .clickable(onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (currentDestination != item.route) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        item.label,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
