package com.example.flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flow.ui.screens.CalendarScreen
import com.example.flow.ui.screens.ProfileScreen
import com.example.flow.ui.screens.TaskScreen
import com.example.flow.ui.screens.time.TimeScreen
import com.example.flow.ui.theme.FlowTheme
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.Screen
import com.example.flow.ui.screens.time.TimeDetailScreen
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
                        NavHost(navController, startDestination = Screen.Time.route, Modifier.padding(innerPadding)) {
                            composable(Screen.Time.route) { TimeScreen(navController) }
                            composable(Screen.TimeDetail.route) { TimeDetailScreen(navController) }

                            composable(Screen.Tasks.route) { TaskScreen(navController) }
                            composable(Screen.Calendar.route) { CalendarScreen(navController) }
                            composable(Screen.Profile.route) { ProfileScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}