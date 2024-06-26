package com.example.flow.ui.components.tasks

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import keyboardAsState
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTaskSheet(
    onDismiss: () -> Unit,
    onCreate: (description: String, due: ZonedDateTime?, project: String, priority: String) -> Unit,
    recentProjects: List<String> = List<String>(5) { "Project $it" }
) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isKeyboardOpen by keyboardAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = {
            if (isKeyboardOpen) WindowInsets.ime else WindowInsets.navigationBars
        }
    )
    {
        var text by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        val keyboardController = LocalSoftwareKeyboardController.current
        var priority by remember { mutableStateOf("") }

        val dateSheet = remember { mutableStateOf(false) }
        val timeSheet = remember { mutableStateOf(false) }
        val projectSheet = remember { mutableStateOf(false) }

        val selectedDueDate = remember { mutableStateOf<ZonedDateTime?>(null) }
        var priorityExpanded by remember { mutableStateOf(false) }
        var projectExpanded by remember { mutableStateOf(false) }
        var project by remember { mutableStateOf("") }

        if (dateSheet.value) {
            TaskDateSheet(onDismiss = {
                scope.launch {
                    dateSheet.value = false
                    modalState.show()
                }
            },
                onCreate = {
                    selectedDueDate.value = it
                    scope.launch {
                        dateSheet.value = false
                        timeSheet.value = true
                    }
                })
        }

        if (timeSheet.value) {
            TaskTimeSheet(onDismiss = {
                scope.launch {
                    timeSheet.value = false
                    modalState.show()
                }
            },
                onCreate = {
                    // Set the time of the selectedDueDate
                    Log.i("CreateTaskSheet", "Selected time: $it");
                    selectedDueDate.value =
                        selectedDueDate.value?.withHour(it.hour)?.withMinute(it.minute)

                    Log.i("CreateTaskSheet", "Selected due date: ${selectedDueDate.value}");
                    scope.launch {
                        timeSheet.value = false
                        modalState.show()
                    }
                })
        }

        if (projectSheet.value) {
            CreateProjectSheet(onDismiss = {
                scope.launch {
                    projectSheet.value = false
                    modalState.show()
                }
            },
                onCreate = {
                    project = it
                    scope.launch {
                        projectSheet.value = false
                        modalState.show()
                    }
                })
        }

        Row {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Enter task description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                )

            }
            Button(onClick = {
                scope.launch {
                    onCreate(text, selectedDueDate.value, project, priority)
                    onDismiss()
                }
            }, content = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            },
                modifier = Modifier
                    .padding(end = 12.dp)
            )
        }

        Row(
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            TextButton(onClick = {
                scope.launch {
                    keyboardController?.hide()
                    modalState.hide()
                    dateSheet.value = true
                }
            },
                content = {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Add due date",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    if (selectedDueDate.value != null) {
                        Text(text = selectedDueDate.value!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                    } else {
                        Text(text = "Add due date")
                    }
                }
            )

            Box {
                DropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false },
                ) {
                    DropdownMenuItem(text = {
                        Text(text = "Low")
                    }, onClick = {
                        priority = "L"
                        priorityExpanded = false
                    })
                    DropdownMenuItem(text = {
                        Text(text = "Medium")
                    }, onClick = {
                        priority = "M"
                        priorityExpanded = false
                    })
                    DropdownMenuItem(text = {
                        Text(text = "High")
                    }, onClick = {
                        priority = "H"
                        priorityExpanded = false
                    })
                    DropdownMenuItem(
                        text = { Text(text = "No priority") },
                        onClick = {
                            priority = ""
                            priorityExpanded = false
                        })
                }
                TextButton(onClick = { priorityExpanded = !priorityExpanded },
                    content = {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Priority",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        if (priority.isNotEmpty()) {
                            when (priority) {
                                "L" -> {
                                    Text(text = "Low")
                                }

                                "M" -> {
                                    Text(text = "Medium")
                                }

                                else -> {
                                    Text(text = "High")
                                }
                            }
                        } else {
                            Text(text = "Priority")
                        }
                    })
            }

            Box {
                DropdownMenu(
                    expanded = projectExpanded,
                    onDismissRequest = { projectExpanded = false },
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Add project") },
                        onClick = {
                            scope.launch {
                                modalState.hide()
                                projectSheet.value = true
                                projectExpanded = false
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add project"
                            )
                        })
                    DropdownMenuItem(
                        text = { Text(text = "No project") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "No project"
                            )
                        },
                        onClick = {
                            project = ""
                            projectExpanded = false
                        })
                    recentProjects.forEach {
                        DropdownMenuItem(text = {
                            Text(text = it)
                        }, onClick = {
                            project = it
                            projectExpanded = false
                        })
                    }


                }
                TextButton(onClick = { projectExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "Project",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    if (project.isNotEmpty()) {
                        Text(text = project)
                    } else {
                        Text(text = "Project")
                    }
                }
            }
        }
    }
}