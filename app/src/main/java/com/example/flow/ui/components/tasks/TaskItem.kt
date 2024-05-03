package com.example.flow.ui.components.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flow.data.model.Task
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun displayDate(dueDateTime: ZonedDateTime): String {
    // If today, show "Today"
    val now = ZonedDateTime.now()
    if (dueDateTime.isAfter(now.withHour(0).withMinute(0).withSecond(0).withNano(0)) &&
        dueDateTime.isBefore(now.withHour(23).withMinute(59).withSecond(59).withNano(999))
    ) {
        return "Today" + dueDateTime.format(DateTimeFormatter.ofPattern(" HH:mm"))
    }
    if (dueDateTime.isAfter(
            now.withHour(0).withMinute(0).withSecond(0).withNano(0).plusDays(1)
        ) &&
        dueDateTime.isBefore(
            now.withHour(23).withMinute(59).withSecond(59).withNano(999).plusDays(1)
        )
    ) {
        return "Tomorrow" + dueDateTime.format(DateTimeFormatter.ofPattern(" HH:mm"))
    }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return dueDateTime.format(formatter)
}

@Preview
@Composable
fun TaskItem(
    task: Task = Task(
        uuid = "1",
        id = "1",
        description = "This is extremely long title i wonder what happens with it",
        status = "completed",
        entry = "2021-10-10T10:00:00Z",
        modified = "2021-10-10T10:00:00Z",
        urgency = 0.6,
        priority = "H",
        due = "2021-10-10T10:00:00Z",
        dueDateTime = ZonedDateTime.now(),
        project = "Project",
        tags = emptyList(),
        depends = emptyList(),
        parent = null,
        recur = null,
        until = null,
        untilDateTime = null,
        start = null
    ),
    onClick: () -> Unit = {},
    onCheck: () -> Unit = {},
    onStart: () -> Unit = {},
    disabled: Boolean = false
) {
    val checkBoxState = remember { mutableStateOf(task.status == "completed") }

    val markAsDone = SwipeAction(
        icon = rememberVectorPainter(image = Icons.Default.Done),
        background = MaterialTheme.colorScheme.tertiaryContainer,
        onSwipe = {
            checkBoxState.value = true
            onCheck()
        }
    )

    val makeActive = SwipeAction(
        icon = if (task.start?.isNotEmpty() == true) {
            rememberVectorPainter(image = Icons.Default.StopCircle)
        } else {
            rememberVectorPainter(image = Icons.Default.PlayCircle)
        },
        background = MaterialTheme.colorScheme.secondaryContainer,
        onSwipe = {
            onStart()
        }
    )

    OutlinedCard(
        onClick = {
            onClick()
        },
    ) {
        SwipeableActionsBox(
            endActions = if (disabled) listOf() else listOf(makeActive, markAsDone),
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!disabled) {
                    Checkbox(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 4.dp),
                        checked = checkBoxState.value,
                        enabled = !checkBoxState.value,
                        onCheckedChange = {
                            checkBoxState.value = it
                            onCheck()
                        })
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.description,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    if (task.due.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayDate(task.dueDateTime!!),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            modifier = Modifier.alpha(0.7f),
                            color = if (task.dueDateTime.isBefore(ZonedDateTime.now())) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                if (task.project.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(8.dp, 0.dp)
                    ) {
                        Text(
                            text = task.project,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        )
                    }
                }
            }
        }
    }
}
