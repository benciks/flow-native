package com.example.flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.flow.ui.screens.CalendarScreen
import com.example.flow.ui.screens.time.TimeScreen
import com.example.flow.ui.theme.FlowTheme
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.Screen
import com.example.flow.ui.screens.ProfileScreen
import com.example.flow.ui.screens.tasks.TasksScreen
import com.example.flow.ui.screens.time.TimeDetailScreen
import com.example.flow.ui.screens.time.TimeRecordsViewModel
import com.example.flow.ui.screens.time.TimeTagsScreen
import com.example.flow.ui.theme.Gray100
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { BottomNav(navController) },
                    ) { innerPadding ->
                        NavHost(
                            navController,
                            startDestination = "time",
                            Modifier.padding(innerPadding)
                        ) {
                            navigation(
                                startDestination = Screen.Time.route,
                                route = "time"
                            ) {
                                composable(Screen.Time.route) {
                                    val viewModel =
                                        it.sharedViewModel<TimeRecordsViewModel>(navController)
                                    TimeScreen(navController, viewModel)
                                }
                                composable(Screen.TimeDetail.route) {
                                    val viewModel =
                                        it.sharedViewModel<TimeRecordsViewModel>(navController)
                                    TimeDetailScreen(navController, viewModel)
                                }
                                composable(Screen.TimeTags.route) {
                                    val viewModel =
                                        it.sharedViewModel<TimeRecordsViewModel>(navController)
                                    TimeTagsScreen(navController, viewModel)
                                }
                            }

                            navigation(
                                startDestination = Screen.Tasks.route,
                                route = "task"
                            ) {
                                composable(Screen.Tasks.route) { TasksScreen(navController) }
                            }

                            composable(Screen.Calendar.route) { CalendarScreen(navController) }
                            composable(Screen.Profile.route) { ProfileScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return hiltViewModel<T>(parentEntry)
}