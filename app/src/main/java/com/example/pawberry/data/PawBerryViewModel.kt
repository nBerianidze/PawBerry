package com.example.pawberry.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.UUID

class PawBerryViewModel : ViewModel() {
    private val _users = mutableStateListOf<User>()
    val users: List<User> get() = _users

    private val _pets = mutableStateListOf<Pet>()
    val pets: List<Pet> get() = _pets

    var currentUsername by mutableStateOf("")
        private set

    fun login(username: String, password: String): String? {
        if (username.isBlank() || password.isBlank()) {
            return "Username and password cannot be empty."
        }
        currentUsername = username.trim()
        return null
    }

    fun register(
        name: String,
        surname: String,
        email: String,
        phoneNumber: String,
        dateOfBirth: String,
    ): String? {
        if (name.isBlank() ||
            surname.isBlank() ||
            email.isBlank() ||
            phoneNumber.isBlank() ||
            dateOfBirth.isBlank()
        ) {
            return "Please fill in all required fields."
        }

        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return "Please enter a valid email address."
        }

        val username = trimmedEmail.substringBefore("@").ifBlank { name.trim() }
        _users.add(
            User(
                name = name.trim(),
                surname = surname.trim(),
                email = trimmedEmail,
                phoneNumber = phoneNumber.trim(),
                dateOfBirth = dateOfBirth.trim(),
                username = username,
            )
        )
        return null
    }

    fun petsForCurrentUser(): List<Pet> =
        _pets.filter { it.ownerUsername.equals(currentUsername, ignoreCase = true) }

    fun addPet(
        name: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
    ): String? {
        if (name.isBlank() || breed.isBlank() || dateOfBirth.isBlank() || favoriteToy.isBlank()) {
            return "Please fill in all pet fields."
        }

        _pets.add(
            Pet(
                id = UUID.randomUUID().toString(),
                ownerUsername = currentUsername,
                name = name.trim(),
                breed = breed.trim(),
                dateOfBirth = dateOfBirth.trim(),
                favoriteToy = favoriteToy.trim(),
            )
        )
        return null
    }
}
