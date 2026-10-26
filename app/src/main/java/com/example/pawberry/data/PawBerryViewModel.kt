package com.example.pawberry.data

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pawberry.PawBerryApplication
import com.example.pawberry.data.db.PetEntity
import com.example.pawberry.data.db.ReminderWithPet
import com.example.pawberry.data.db.UserEntity
import com.example.pawberry.data.model.RecurrenceRule
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.data.remote.ApiConfig
import com.example.pawberry.data.remote.BackendConnectionState
import com.example.pawberry.data.remote.BaseUrlInterceptor
import com.example.pawberry.data.remote.SessionStore
import com.example.pawberry.data.remote.toApiError
import com.example.pawberry.data.remote.toUserMessage
import com.example.pawberry.util.EmailValidator
import com.example.pawberry.data.repository.PetRepository
import com.example.pawberry.data.repository.ReminderRepository
import com.example.pawberry.data.repository.UserRepository
import com.example.pawberry.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PawBerryViewModel(
    private val userRepository: UserRepository,
    private val petRepository: PetRepository,
    private val reminderRepository: ReminderRepository,
    private val sessionStore: SessionStore,
    private val reminderScheduler: ReminderScheduler,
    private val baseUrlInterceptor: BaseUrlInterceptor,
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _pets = MutableStateFlow<List<PetEntity>>(emptyList())
    val pets: StateFlow<List<PetEntity>> = _pets.asStateFlow()

    private val _reminders = MutableStateFlow<List<ReminderWithPet>>(emptyList())
    val reminders: StateFlow<List<ReminderWithPet>> = _reminders.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _backendStatus = MutableStateFlow<BackendConnectionState>(BackendConnectionState.Checking)
    val backendStatus: StateFlow<BackendConnectionState> = _backendStatus.asStateFlow()

    val upcomingReminders: StateFlow<List<ReminderWithPet>> = reminders
        .map { all -> all.filterNot { it.reminder.isCompleted }.take(HOME_REMINDER_COUNT) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val rewardPoints: StateFlow<Int> = currentUser
        .map { it?.rewardPoints ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    val username: StateFlow<String> = currentUser
        .map { it?.username.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), "")

    fun imageBaseUrl(): String = baseUrlInterceptor.currentBaseUrl()

    fun clearFormError() {
        _formError.value = null
        _emailError.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun checkBackendConnection() {
        viewModelScope.launch {
            _backendStatus.value = BackendConnectionState.Checking
            try {
                val message = userRepository.checkHealth()
                Log.i(ApiConfig.LOG_TAG, "GET / reachable: $message")
                _backendStatus.value = BackendConnectionState.Reachable(message)
            } catch (error: Exception) {
                Log.e(ApiConfig.LOG_TAG, "GET / failed: ${error.javaClass.simpleName}")
                _backendStatus.value = BackendConnectionState.Unreachable(error.toUserMessage())
            }
        }
    }

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isEmpty() || password.isEmpty()) {
            _formError.value = "Username and password cannot be empty."
            return
        }

        runRequest {
            try {
                val response = userRepository.login(trimmedUsername, password)
                sessionStore.token = response.token
                refreshSession()
                _formError.value = null
                onSuccess()
            } catch (error: Exception) {
                sessionStore.clear()
                _formError.value = error.toUserMessage()
            }
        }
    }

    fun register(
        name: String,
        surname: String,
        username: String,
        email: String,
        phoneNumber: String,
        dateOfBirth: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
    ) {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        val emailProblem = EmailValidator.formatError(trimmedEmail)
        _emailError.value = emailProblem

        if (name.isBlank() || surname.isBlank() || trimmedUsername.isEmpty() ||
            trimmedEmail.isEmpty() || phoneNumber.isBlank() || dateOfBirth.isBlank() ||
            password.isEmpty()
        ) {
            _formError.value = "Please fill in all required fields."
            return
        }
        if (emailProblem != null) {
            _formError.value = null
            return
        }
        if (trimmedUsername.length < MIN_USERNAME_LENGTH) {
            _formError.value = "Username must be at least $MIN_USERNAME_LENGTH characters."
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            _formError.value = "Password must be at least $MIN_PASSWORD_LENGTH characters."
            return
        }
        if (password != confirmPassword) {
            _formError.value = "Passwords do not match."
            return
        }

        runRequest {
            try {
                userRepository.register(
                    firstName = name.trim(),
                    lastName = surname.trim(),
                    username = trimmedUsername,
                    email = trimmedEmail,
                    phone = phoneNumber.trim(),
                    dateOfBirth = dateOfBirth.trim(),
                    password = password,
                )
                sessionStore.clear()
                _formError.value = null
                _emailError.value = null
                onSuccess()
            } catch (error: Exception) {
                val apiError = error.toApiError()
                if (apiError.field == "email") {
                    _emailError.value = apiError.message
                    _formError.value = null
                } else {
                    _formError.value = apiError.message
                }
            }
        }
    }

    fun logout() {
        sessionStore.clear()
        reminderScheduler.sync(emptyList())
        _currentUser.value = null
        _pets.value = emptyList()
        _reminders.value = emptyList()
        _formError.value = null
        _emailError.value = null
    }

    fun addPet(
        name: String,
        species: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
        gender: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
    ) {
        if (_currentUser.value == null) {
            _formError.value = "Please log in again."
            return
        }
        if (!validatePetFields(name, species, breed, dateOfBirth, favoriteToy, gender)) return

        runRequest {
            try {
                petRepository.insert(
                    name = name.trim(),
                    species = species.trim(),
                    breed = breed.trim(),
                    dateOfBirth = dateOfBirth.trim(),
                    favoriteToy = favoriteToy.trim(),
                    gender = gender.trim(),
                    imageUri = imageUri,
                    existingImage = "",
                )
                refreshPetsAndReminders()
                _formError.value = null
                _userMessage.value = "Pet saved."
                onSuccess()
            } catch (error: Exception) {
                _formError.value = error.toUserMessage()
            }
        }
    }

    fun updatePet(
        pet: PetEntity,
        name: String,
        species: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
        gender: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
    ) {
        if (!validatePetFields(name, species, breed, dateOfBirth, favoriteToy, gender)) return

        runRequest {
            try {
                petRepository.update(
                    pet.copy(
                        name = name.trim(),
                        species = species.trim(),
                        breed = breed.trim(),
                        dateOfBirth = dateOfBirth.trim(),
                        favoriteToy = favoriteToy.trim(),
                        gender = gender.trim(),
                    ),
                    imageUri = imageUri,
                )
                refreshPetsAndReminders()
                _formError.value = null
                _userMessage.value = "Pet updated."
                onSuccess()
            } catch (error: Exception) {
                _formError.value = error.toUserMessage()
            }
        }
    }

    fun deletePet(pet: PetEntity, onSuccess: () -> Unit) {
        runRequest {
            try {
                petRepository.delete(pet.petId)
                refreshPetsAndReminders()
                _userMessage.value = "${pet.name} was deleted."
                onSuccess()
            } catch (error: Exception) {
                _formError.value = error.toUserMessage()
                _userMessage.value = error.toUserMessage()
            }
        }
    }

    private fun validatePetFields(
        name: String,
        species: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
        gender: String,
    ): Boolean {
        if (name.isBlank() || species.isBlank() || breed.isBlank() ||
            dateOfBirth.isBlank() || favoriteToy.isBlank() || gender.isBlank()
        ) {
            _formError.value = "Please fill in all pet fields."
            return false
        }
        return true
    }

    fun addReminder(
        petId: String?,
        title: String,
        category: ReminderCategory,
        dueDate: String,
        dueTime: String,
        recurrence: RecurrenceRule,
        onSuccess: () -> Unit,
    ) {
        if (petId.isNullOrBlank()) {
            _formError.value = "Please choose a pet for this reminder."
            return
        }
        if (title.isBlank()) {
            _formError.value = "Please enter a reminder title."
            return
        }
        if (dueDate.isBlank() || dueTime.isBlank()) {
            _formError.value = "Please choose a due date and time."
            return
        }
        if (recurrence.interval < 1) {
            _formError.value = "Repeat interval must be at least 1."
            return
        }
        if (recurrence.endsOnDate) {
            if (recurrence.endDate.isBlank()) {
                _formError.value = "Please choose an end date, or repeat indefinitely."
                return
            }
            if (recurrence.endDate < dueDate) {
                _formError.value = "The end date cannot be earlier than the reminder date."
                return
            }
        }

        runRequest {
            try {
                reminderRepository.insert(
                    petId = petId,
                    title = title.trim(),
                    category = category,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    recurrence = recurrence,
                )
                refreshPetsAndReminders()
                _formError.value = null
                _userMessage.value = "Reminder saved."
                onSuccess()
            } catch (error: Exception) {
                _formError.value = error.toUserMessage()
            }
        }
    }

    fun setReminderCompleted(reminderId: String, completed: Boolean) {
        viewModelScope.launch {
            try {
                reminderRepository.setCompleted(reminderId, completed)
                refreshPetsAndReminders()
                refreshProfile()
            } catch (error: Exception) {
                _formError.value = error.toUserMessage()
                _userMessage.value = error.toUserMessage()
            }
        }
    }

    fun deleteReminder(reminderId: String, title: String) {
        runRequest {
            try {
                reminderRepository.delete(reminderId)
                refreshPetsAndReminders()
                _userMessage.value = "Deleted \"$title\"."
            } catch (error: Exception) {
                _userMessage.value = error.toUserMessage()
            }
        }
    }

    private suspend fun refreshSession() {
        val points = userRepository.getRewardPoints()
        _currentUser.value = userRepository.getProfile(points)
        refreshPetsAndReminders()
    }

    private suspend fun refreshProfile() {
        val points = userRepository.getRewardPoints()
        _currentUser.value = userRepository.getProfile(points)
    }

    private suspend fun refreshPetsAndReminders() {
        _pets.value = petRepository.getAll()
        val loaded = reminderRepository.getAll()
        _reminders.value = loaded
        reminderScheduler.sync(loaded)
    }

    private fun runRequest(block: suspend () -> Unit) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            try {
                block()
            } finally {
                _isBusy.value = false
            }
        }
    }

    companion object {
        const val POINTS_PER_COMPLETED_REMINDER = 10
        private const val HOME_REMINDER_COUNT = 3
        private const val MIN_USERNAME_LENGTH = 3
        private const val MIN_PASSWORD_LENGTH = 6
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as PawBerryApplication
                PawBerryViewModel(
                    userRepository = application.userRepository,
                    petRepository = application.petRepository,
                    reminderRepository = application.reminderRepository,
                    sessionStore = application.sessionStore,
                    reminderScheduler = application.reminderScheduler,
                    baseUrlInterceptor = application.baseUrlInterceptor,
                )
            }
        }
    }
}
