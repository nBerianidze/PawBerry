package com.example.pawberry

import android.app.Application
import com.example.pawberry.data.db.PawBerryDatabase
import com.example.pawberry.data.repository.PetRepository
import com.example.pawberry.data.repository.ReminderRepository
import com.example.pawberry.data.repository.UserRepository

class PawBerryApplication : Application() {
    val database: PawBerryDatabase by lazy { PawBerryDatabase.getInstance(this) }
    val userRepository: UserRepository by lazy { UserRepository(database.userDao()) }
    val petRepository: PetRepository by lazy { PetRepository(database.petDao()) }
    val reminderRepository: ReminderRepository by lazy { ReminderRepository(database.reminderDao()) }
}
