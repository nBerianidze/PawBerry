package com.example.pawberry

import android.app.Application
import com.example.pawberry.data.remote.ApiClient
import com.example.pawberry.data.remote.BackendLocator
import com.example.pawberry.data.remote.BaseUrlInterceptor
import com.example.pawberry.data.remote.SessionStore
import com.example.pawberry.data.repository.PetRepository
import com.example.pawberry.data.repository.ReminderRepository
import com.example.pawberry.data.repository.UserRepository
import com.example.pawberry.notifications.ReminderScheduler

class PawBerryApplication : Application() {
    val sessionStore: SessionStore by lazy { SessionStore(this) }
    val baseUrlInterceptor: BaseUrlInterceptor by lazy { BaseUrlInterceptor() }
    val backendLocator: BackendLocator by lazy { BackendLocator(baseUrlInterceptor) }
    private val api by lazy { ApiClient.create(sessionStore, baseUrlInterceptor) }
    val userRepository: UserRepository by lazy { UserRepository(api, backendLocator) }
    val petRepository: PetRepository by lazy { PetRepository(api, this) }
    val reminderRepository: ReminderRepository by lazy { ReminderRepository(api) }
    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(this) }
}
