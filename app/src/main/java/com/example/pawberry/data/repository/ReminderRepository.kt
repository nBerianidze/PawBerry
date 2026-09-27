package com.example.pawberry.data.repository

import com.example.pawberry.data.db.ReminderDao
import com.example.pawberry.data.db.ReminderEntity
import com.example.pawberry.data.db.ReminderWithPet
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    suspend fun insert(reminder: ReminderEntity): Long = reminderDao.insert(reminder)

    suspend fun getById(reminderId: Long): ReminderEntity? = reminderDao.getById(reminderId)

    suspend fun update(reminder: ReminderEntity) = reminderDao.update(reminder)

    fun observeForUser(userId: Long): Flow<List<ReminderWithPet>> =
        reminderDao.observeForUser(userId)

    fun observeForPetAndCategory(petId: Long, category: String): Flow<List<ReminderEntity>> =
        reminderDao.observeForPetAndCategory(petId, category)
}
