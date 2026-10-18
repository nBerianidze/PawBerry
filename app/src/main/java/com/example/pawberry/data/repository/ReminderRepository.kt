package com.example.pawberry.data.repository

import com.example.pawberry.data.db.ReminderWithPet
import com.example.pawberry.data.model.RecurrenceRule
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.data.remote.CreateReminderRequest
import com.example.pawberry.data.remote.PawBerryApi
import com.example.pawberry.data.remote.UpdateReminderRequest
import com.example.pawberry.data.remote.toDto
import com.example.pawberry.data.remote.toLegacyRepeat
import com.example.pawberry.data.remote.toReminderWithPet

class ReminderRepository(private val api: PawBerryApi) {
    suspend fun getAll(): List<ReminderWithPet> = api.getReminders().map { it.toReminderWithPet() }

    suspend fun insert(
        petId: String,
        title: String,
        category: ReminderCategory,
        dueDate: String,
        dueTime: String,
        recurrence: RecurrenceRule,
    ): ReminderWithPet = api.createReminder(
        CreateReminderRequest(
            pet = petId,
            title = title,
            type = category.toApi(),
            date = dueDate.ifBlank { null },
            time = dueTime,
            repeat = recurrence.toLegacyRepeat(),
            recurrence = recurrence.toDto(),
        )
    ).toReminderWithPet()

    suspend fun complete(reminderId: String): ReminderWithPet =
        api.completeReminder(reminderId).toReminderWithPet()

    suspend fun setCompleted(reminderId: String, completed: Boolean): ReminderWithPet =
        if (completed) {
            complete(reminderId)
        } else {
            api.updateReminder(reminderId, UpdateReminderRequest(completed = false)).toReminderWithPet()
        }

    suspend fun delete(reminderId: String) {
        api.deleteReminder(reminderId)
    }
}
