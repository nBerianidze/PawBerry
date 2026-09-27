package com.example.pawberry.data

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pawberry.PawBerryApplication
import com.example.pawberry.data.db.PetEntity
import com.example.pawberry.data.db.ReminderEntity
import com.example.pawberry.data.db.ReminderWithPet
import com.example.pawberry.data.db.UserEntity
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.data.model.RepeatFrequency
import com.example.pawberry.data.repository.PetRepository
import com.example.pawberry.data.repository.ReminderRepository
import com.example.pawberry.data.repository.UserRepository
import com.example.pawberry.data.security.PasswordHasher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single source of truth for the signed-in session. Every screen reads from the
 * [StateFlow]s exposed here, which are backed by Room queries, so a write made on one
 * screen shows up on all the others without any manual refresh.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PawBerryViewModel(
    private val userRepository: UserRepository,
    private val petRepository: PetRepository,
    private val reminderRepository: ReminderRepository,
) : ViewModel() {

    private val currentUserId = MutableStateFlow<Long?>(null)

    /**
     * Validation message for whichever form is currently on screen. Only one form is
     * ever visible at a time, so a single field keeps the plumbing simple.
     */
    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = currentUserId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else userRepository.observeById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val pets: StateFlow<List<PetEntity>> = currentUserId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else petRepository.observeByUser(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val reminders: StateFlow<List<ReminderWithPet>> = currentUserId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else reminderRepository.observeForUser(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** The next three reminders that have not been ticked off yet, for the Home screen. */
    val upcomingReminders: StateFlow<List<ReminderWithPet>> = reminders
        .map { all -> all.filterNot { it.reminder.isCompleted }.take(HOME_REMINDER_COUNT) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val rewardPoints: StateFlow<Int> = currentUser
        .map { it?.rewardPoints ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    val username: StateFlow<String> = currentUser
        .map { it?.username.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), "")

    fun clearFormError() {
        _formError.value = null
    }

    // ---------------------------------------------------------------- authentication

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isEmpty() || password.isEmpty()) {
            _formError.value = "Username and password cannot be empty."
            return
        }

        viewModelScope.launch {
            val user = userRepository.getByUsername(trimmedUsername)
            if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
                _formError.value = "Incorrect username or password."
                return@launch
            }
            _formError.value = null
            currentUserId.value = user.userId
            onSuccess()
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

        if (name.isBlank() || surname.isBlank() || trimmedUsername.isEmpty() ||
            trimmedEmail.isEmpty() || phoneNumber.isBlank() || dateOfBirth.isBlank() ||
            password.isEmpty()
        ) {
            _formError.value = "Please fill in all required fields."
            return
        }
        if (!trimmedEmail.contains("@") || !trimmedEmail.substringAfterLast("@").contains(".")) {
            _formError.value = "Please enter a valid email address."
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

        viewModelScope.launch {
            if (userRepository.getByUsername(trimmedUsername) != null) {
                _formError.value = "That username is already taken."
                return@launch
            }
            if (userRepository.getByEmail(trimmedEmail) != null) {
                _formError.value = "An account already uses that email address."
                return@launch
            }

            try {
                userRepository.insert(
                    UserEntity(
                        name = name.trim(),
                        surname = surname.trim(),
                        username = trimmedUsername,
                        email = trimmedEmail,
                        phoneNumber = phoneNumber.trim(),
                        dateOfBirth = dateOfBirth.trim(),
                        passwordHash = PasswordHasher.hash(password),
                    )
                )
            } catch (_: SQLiteConstraintException) {
                // The unique indexes on username/email are the final word on duplicates.
                _formError.value = "That username or email is already registered."
                return@launch
            }
            _formError.value = null
            onSuccess()
        }
    }

    fun logout() {
        currentUserId.value = null
        _formError.value = null
    }

    // ------------------------------------------------------------------------- pets

    fun addPet(
        name: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
        onSuccess: () -> Unit,
    ) {
        val ownerId = currentUserId.value
        if (ownerId == null) {
            _formError.value = "Please log in again."
            return
        }
        if (!validatePetFields(name, breed, dateOfBirth, favoriteToy)) return

        viewModelScope.launch {
            petRepository.insert(
                PetEntity(
                    userId = ownerId,
                    name = name.trim(),
                    breed = breed.trim(),
                    dateOfBirth = dateOfBirth.trim(),
                    favoriteToy = favoriteToy.trim(),
                )
            )
            _formError.value = null
            onSuccess()
        }
    }

    fun updatePet(
        pet: PetEntity,
        name: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
        onSuccess: () -> Unit,
    ) {
        if (!validatePetFields(name, breed, dateOfBirth, favoriteToy)) return

        viewModelScope.launch {
            petRepository.update(
                pet.copy(
                    name = name.trim(),
                    breed = breed.trim(),
                    dateOfBirth = dateOfBirth.trim(),
                    favoriteToy = favoriteToy.trim(),
                )
            )
            _formError.value = null
            onSuccess()
        }
    }

    private fun validatePetFields(
        name: String,
        breed: String,
        dateOfBirth: String,
        favoriteToy: String,
    ): Boolean {
        if (name.isBlank() || breed.isBlank() || dateOfBirth.isBlank() || favoriteToy.isBlank()) {
            _formError.value = "Please fill in all pet fields."
            return false
        }
        return true
    }

    // -------------------------------------------------------------------- reminders

    fun addReminder(
        petId: Long?,
        title: String,
        category: ReminderCategory,
        dueDate: String,
        dueTime: String,
        repeatFrequency: RepeatFrequency,
        onSuccess: () -> Unit,
    ) {
        if (petId == null) {
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

        viewModelScope.launch {
            reminderRepository.insert(
                ReminderEntity(
                    petId = petId,
                    title = title.trim(),
                    category = category.name,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    repeatFrequency = repeatFrequency.name,
                )
            )
            _formError.value = null
            onSuccess()
        }
    }

    /**
     * Ticks a reminder off (or back on). The first time a reminder is completed the user
     * earns [POINTS_PER_COMPLETED_REMINDER]; `pointsAwarded` makes sure un-checking and
     * re-checking it cannot farm more points.
     */
    fun setReminderCompleted(reminderId: Long, completed: Boolean) {
        viewModelScope.launch {
            val reminder = reminderRepository.getById(reminderId) ?: return@launch
            val shouldAwardPoints = completed && !reminder.pointsAwarded

            reminderRepository.update(
                reminder.copy(
                    isCompleted = completed,
                    pointsAwarded = reminder.pointsAwarded || completed,
                )
            )

            if (shouldAwardPoints) {
                currentUserId.value?.let { userId ->
                    userRepository.addRewardPoints(userId, POINTS_PER_COMPLETED_REMINDER)
                }
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
                )
            }
        }
    }
}
