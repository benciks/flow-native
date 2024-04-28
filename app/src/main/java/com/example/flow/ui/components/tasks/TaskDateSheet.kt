package com.example.flow.ui.components.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateSheet(
    onDismiss: () -> Unit,
    onCreate: (ZonedDateTime) -> Unit = {},
    selectedDate: ZonedDateTime? = null
) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    )
    {
        val initialDate = selectedDate?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()

        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
        val scope = rememberCoroutineScope()

        DatePicker(
            state = dateState
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                scope.launch {
                    onCreate(
                        ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(dateState.selectedDateMillis!!),
                            ZoneId.systemDefault()
                        )
                    )
                }
            }, content = {
                Text(text = "Done")
            })
        }
    }
}