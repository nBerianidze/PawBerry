package com.example.pawberry.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawberry.data.db.ReminderWithPet
import com.example.pawberry.ui.components.ReminderCard

@Composable
fun RemindersScreen(
    reminders: List<ReminderWithPet>,
    onCompletedChange: (reminderId: Long, completed: Boolean) -> Unit,
    onAddReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (reminders.isEmpty()) {
            item {
                Text(
                    text = "No reminders yet. Add one to start tracking pet care.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            items(reminders, key = { it.reminder.reminderId }) { item ->
                ReminderCard(
                    reminder = item.reminder,
                    petName = item.petName,
                    onCompletedChange = { completed ->
                        onCompletedChange(item.reminder.reminderId, completed)
                    },
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
