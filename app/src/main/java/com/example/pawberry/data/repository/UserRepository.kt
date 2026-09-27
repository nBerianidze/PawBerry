package com.example.pawberry.data.repository

import com.example.pawberry.data.db.UserDao
import com.example.pawberry.data.db.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun insert(user: UserEntity): Long = userDao.insert(user)

    suspend fun update(user: UserEntity) = userDao.update(user)

    suspend fun getByUsername(username: String): UserEntity? = userDao.getByUsername(username)

    suspend fun getByEmail(email: String): UserEntity? = userDao.getByEmail(email)

    fun observeById(userId: Long): Flow<UserEntity?> = userDao.observeById(userId)

    suspend fun addRewardPoints(userId: Long, points: Int) =
        userDao.addRewardPoints(userId, points)
}
