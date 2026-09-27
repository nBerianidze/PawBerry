package com.example.pawberry.data.repository

import com.example.pawberry.data.db.PetDao
import com.example.pawberry.data.db.PetEntity
import kotlinx.coroutines.flow.Flow

class PetRepository(private val petDao: PetDao) {
    suspend fun insert(pet: PetEntity): Long = petDao.insert(pet)

    suspend fun update(pet: PetEntity) = petDao.update(pet)

    fun observeByUser(userId: Long): Flow<List<PetEntity>> = petDao.observeByUser(userId)

    fun observeById(petId: Long): Flow<PetEntity?> = petDao.observeById(petId)
}
