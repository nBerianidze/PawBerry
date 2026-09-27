package com.example.pawberry.data.db

import androidx.room.Embedded

data class ReminderWithPet(
    @Embedded val reminder: ReminderEntity,
    val petName: String,
)
