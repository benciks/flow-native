package com.example.flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.theme.FlowTheme
import com.example.flow.ui.screens.NavGraphs
import com.example.flow.ui.screens.appCurrentDestinationAsState
import com.example.flow.ui.screens.destinations.Destination
import com.example.flow.ui.screens.destinations.RegisterScreenDestination
import com.example.flow.ui.screens.startAppDestination
import com.example.flow.ui.screens.time.TimeRecordsViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Qualifier

@RootNavGraph
@NavGraph
annotation class TimeNavGraph(
    val start: Boolean = false
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowTheme {
                val navController = rememberNavController()

                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                    navController = navController,
                    dependenciesContainerBuilder = {
                        dependency(NavGraphs.time) {
                            val parentEntry = remember(navBackStackEntry) {
                                navController.getBackStackEntry(NavGraphs.time.route)
                            }
                            hiltViewModel<TimeRecordsViewModel>(parentEntry)
                        }
                    }
                )
            }
        }
    }
}