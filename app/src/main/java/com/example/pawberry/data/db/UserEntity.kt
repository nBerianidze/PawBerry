package com.example.pawberry.data.db

data class UserEntity(
    val userId: String,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val dateOfBirth: String,
    val rewardPoints: Int = 0,
)
