package com.example.pawberry.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["petId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("petId")],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val reminderId: Long = 0,
    val petId: Long,
    val title: String,
    val category: String,
    val dueDate: String,
    val dueTime: String,
    val repeatFrequency: String,
    val isCompleted: Boolean = false,
    /**
     * Set once the 10 reward points for this reminder have been granted, so re-checking
     * a reminder can never award points twice.
     */
    val pointsAwarded: Boolean = false,
)
