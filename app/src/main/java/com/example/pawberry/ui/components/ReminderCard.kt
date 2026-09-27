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
import androidx.compose.runtime.Composable
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

/**
 * One reminder row. Used on both the Reminders tab and a pet's category history, so the
 * completion checkbox behaves identically in both places.
 */
@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    petName: String?,
    onCompletedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val category = ReminderCategory.fromStored(reminder.category)
    val repeat = RepeatFrequency.fromStored(reminder.repeatFrequency)

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
                if (repeat != RepeatFrequency.NONE) {
                    Text(
                        text = "Repeats: ${repeat.label}",
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
            }
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = onCompletedChange,
            )
        }
    }
}
