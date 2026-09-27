package com.example.pawberry.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawberry.data.db.ReminderEntity
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.ui.components.PawBerryTopBar
import com.example.pawberry.ui.components.ReminderCard

/**
 * Treatment history for a single category of a single pet: what is still due, and what
 * has already been done.
 */
@Composable
fun CategoryHistoryScreen(
    petName: String,
    category: ReminderCategory,
    reminders: List<ReminderEntity>,
    onCompletedChange: (reminderId: Long, completed: Boolean) -> Unit,
    onAddReminder: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val upcoming = remember(reminders) { reminders.filterNot { it.isCompleted } }
    val history = remember(reminders) { reminders.filter { it.isCompleted } }

    Column(modifier = modifier.fillMaxSize()) {
        PawBerryTopBar(title = category.pluralLabel, onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = petName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }

            item { SectionHeader("Upcoming") }
            if (upcoming.isEmpty()) {
                item { EmptyText("Nothing scheduled.") }
            } else {
                items(upcoming, key = { it.reminderId }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        petName = null,
                        onCompletedChange = { onCompletedChange(reminder.reminderId, it) },
                    )
                }
            }

            item { SectionHeader("History") }
            if (history.isEmpty()) {
                item { EmptyText("Nothing completed yet.") }
            } else {
                items(history, key = { it.reminderId }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        petName = null,
                        onCompletedChange = { onCompletedChange(reminder.reminderId, it) },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAddReminder,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Add Reminder")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
    )
}
