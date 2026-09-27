package com.example.pawberry.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true),
    ],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val dateOfBirth: String,
    val passwordHash: String,
    val rewardPoints: Int = 0,
)
