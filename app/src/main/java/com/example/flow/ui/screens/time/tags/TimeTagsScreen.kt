package com.example.flow.ui.screens.time.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flow.TimeNavGraph
import com.example.flow.ui.components.BottomNav
import com.example.flow.ui.screens.time.TimeRecordsViewModel
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@TimeNavGraph
@Destination
@Composable
fun TimeTagsScreen(
    navController: NavController,
    viewModel: TimeRecordsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            BottomNav(navController = navController)
        },
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit tags") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            Text("Selected tags", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (state.selectedRecord?.tags!!.isEmpty()) {
                Text("No tags selected")
            } else {
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    for (tag in state.selectedRecord?.tags!!) {
                        InputChip(
                            onClick = {
                                viewModel.untagSelectedRecord(tag)
                            },
                            label = {
                                Text(tag, fontSize = 16.sp)
                            },
                            selected = true,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(MaterialTheme.shapes.large),
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Localized description",
                                    Modifier.size(18.dp)
                                )
                            },
                        )
                    }
                }
            }

            var text by remember { mutableStateOf("") }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 24.dp),
            ) {
                TextField(
                    value = text,
                    singleLine = true,
                    onValueChange = { text = it },
                    label = { Text("Add tag") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    viewModel.tagSelectedRecord(text)
                    text = ""
                }, Modifier.height(52.dp)) {
                    Text("Add")
                }
            }


            // TODO: Filter tags that are already selected
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                for (tag in state.recentTags) {
                    if (tag !in state.selectedRecord?.tags!!) {
                        item {
                            ClickableText(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                text = AnnotatedString(tag),
                                onClick = { viewModel.tagSelectedRecord(tag) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
