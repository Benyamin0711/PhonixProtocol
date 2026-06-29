package com.benyaminrasouli.phoniexprotocol.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Task
import com.benyaminrasouli.phoniexprotocol.ui.theme.PhoenixOrange
import com.benyaminrasouli.phoniexprotocol.ui.theme.TextSecondary

@Composable
fun TaskListSection(
    title: String,
    tasks: List<Task>,
    onTaskComplete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (tasks.isEmpty()) {
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            tasks.forEach { task ->
                TaskItem(task = task, onComplete = { onTaskComplete(task) })
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onComplete)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${task.difficulty} • ${task.category} • ${task.xpValue} XP",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Complete task",
            tint = PhoenixOrange,
            modifier = Modifier.size(24.dp)
        )
    }
}
