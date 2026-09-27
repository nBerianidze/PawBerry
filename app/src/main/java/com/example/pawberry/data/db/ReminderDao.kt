package com.example.pawberry.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE reminderId = :reminderId LIMIT 1")
    suspend fun getById(reminderId: Long): ReminderEntity?

    @Query(
        """
        SELECT reminders.*, pets.name AS petName
        FROM reminders
        INNER JOIN pets ON reminders.petId = pets.petId
        WHERE pets.userId = :userId
        ORDER BY reminders.isCompleted ASC, reminders.dueDate ASC, reminders.dueTime ASC
        """,
    )
    fun observeForUser(userId: Long): Flow<List<ReminderWithPet>>

    @Query(
        """
        SELECT * FROM reminders
        WHERE petId = :petId AND category = :category
        ORDER BY isCompleted ASC, dueDate ASC, dueTime ASC
        """,
    )
    fun observeForPetAndCategory(petId: Long, category: String): Flow<List<ReminderEntity>>
}
