package com.example.pawberry.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.pawberry.data.db.ReminderEntity
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.data.model.RepeatFrequency
import com.example.pawberry.util.formatDateForDisplay
import com.example.pawberry.util.formatTimeForDisplay

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    petName: String?,
    onCompletedChange: (Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
    deleteEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val category = ReminderCategory.fromStored(reminder.category)
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (petName != null) {
                    Text(
                        text = petName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (reminder.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                )
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${formatDateForDisplay(reminder.dueDate)} at " +
                        formatTimeForDisplay(reminder.dueTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                if (reminder.recurrence.frequency != RepeatFrequency.NONE) {
                    Text(
                        text = "Repeats: ${reminder.recurrence.describe()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
                Text(
                    text = if (reminder.isCompleted) "Completed" else "Not completed",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (reminder.isCompleted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    },
                )
                if (onDelete != null) {
                    TextButton(
                        onClick = { confirmDelete = true },
                        enabled = deleteEnabled,
                    ) {
                        Text("Delete")
                    }
                }
            }
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = onCompletedChange,
            )
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete reminder?",
            message = "Delete \"${reminder.title}\"? This cannot be undone.",
            onConfirm = {
                confirmDelete = false
                onDelete?.invoke()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}
