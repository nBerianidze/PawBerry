package com.example.pawberry.data.db

import com.example.pawberry.data.model.RecurrenceRule

data class ReminderEntity(
    val reminderId: String,
    val petId: String,
    val title: String,
    val category: String,
    val dueDate: String,
    val dueTime: String,
    val recurrence: RecurrenceRule = RecurrenceRule(),
    val isCompleted: Boolean = false,
) {
    val repeatFrequency: String get() = recurrence.frequency.name
}
