package com.example.flow.ui.screens.time

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDetailScreen(
    navController: NavController,
    viewModel: TimeRecordsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { TopAppBar(
            title = { Text(text = "Time Detail") },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                    viewModel.clearSelectedRecord()
                },
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }

        ) },
    ) {
        padding ->
        LazyColumn(contentPadding = padding) {
            item {
                Text("Time Detail Screen")
            }

        }
    }
}
