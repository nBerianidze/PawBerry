package com.example.pawberry.data.repository

import com.example.pawberry.data.db.UserEntity
import com.example.pawberry.data.remote.AuthResponse
import com.example.pawberry.data.remote.BackendLocator
import com.example.pawberry.data.remote.LoginRequest
import com.example.pawberry.data.remote.PawBerryApi
import com.example.pawberry.data.remote.RegisterRequest
import com.example.pawberry.data.remote.toEntity

class UserRepository(
    private val api: PawBerryApi,
    private val backendLocator: BackendLocator,
) {
    suspend fun checkHealth(): String = backendLocator.locate()

    suspend fun login(username: String, password: String): AuthResponse {
        backendLocator.locate()
        return api.login(LoginRequest(username = username, password = password))
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        phone: String,
        dateOfBirth: String,
        password: String,
    ): AuthResponse {
        backendLocator.locate()
        return api.register(
            RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                phone = phone,
                dateOfBirth = dateOfBirth.ifBlank { null },
                password = password,
            )
        )
    }

    suspend fun getProfile(rewardPoints: Int): UserEntity = api.getMe().toEntity(rewardPoints)

    suspend fun getRewardPoints(): Int = api.getRewards().points
}
