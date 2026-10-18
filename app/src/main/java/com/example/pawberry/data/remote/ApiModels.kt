package com.example.pawberry.data.remote

import com.example.pawberry.data.db.PetEntity
import com.example.pawberry.data.db.ReminderEntity
import com.example.pawberry.data.db.ReminderWithPet
import com.example.pawberry.data.db.UserEntity
import com.example.pawberry.data.model.RecurrenceRule
import com.example.pawberry.data.model.RecurrenceUnit
import com.example.pawberry.data.model.ReminderCategory
import com.example.pawberry.data.model.RepeatFrequency

data class LoginRequest(
    val username: String,
    val password: String,
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String?,
    val password: String,
)

data class AuthResponse(
    val message: String? = null,
    val token: String,
    val user: AuthUserDto,
)

data class AuthUserDto(
    val id: String,
    val username: String,
    val firstName: String? = null,
)

data class UserDto(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val username: String,
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null,
)

data class PetDto(
    val id: String,
    val owner: String? = null,
    val name: String,
    val species: String? = null,
    val breed: String? = null,
    val dateOfBirth: String? = null,
    val favoriteToy: String? = null,
    val gender: String? = null,
    val profileImage: String? = null,
)

data class CreatePetRequest(
    val name: String,
    val species: String,
    val breed: String,
    val dateOfBirth: String?,
    val favoriteToy: String,
    val gender: String,
    val profileImage: String,
)

data class UpdatePetRequest(
    val name: String,
    val species: String,
    val breed: String,
    val dateOfBirth: String?,
    val favoriteToy: String,
    val gender: String,
    val profileImage: String,
)

data class RecurrenceDto(
    val frequency: String? = null,
    val interval: Int? = null,
    val unit: String? = null,
    val weekDays: List<Int>? = null,
    val monthDay: Int? = null,
    val ends: String? = null,
    val endDate: String? = null,
)

data class ReminderDto(
    val id: String,
    val pet: String,
    val petName: String? = null,
    val title: String,
    val type: String? = null,
    val date: String? = null,
    val time: String? = null,
    val repeat: String? = null,
    val recurrence: RecurrenceDto? = null,
    val completed: Boolean = false,
)

data class CreateReminderRequest(
    val pet: String,
    val title: String,
    val type: String,
    val date: String?,
    val time: String,
    val repeat: String,
    val recurrence: RecurrenceDto,
)

data class UpdateReminderRequest(
    val completed: Boolean? = null,
    val recurrence: RecurrenceDto? = null,
)

data class UploadResponse(
    val url: String,
)

data class RewardDto(
    val points: Int = 0,
)

data class ApiMessage(
    val message: String? = null,
)

fun UserDto.toEntity(rewardPoints: Int): UserEntity = UserEntity(
    userId = id,
    name = firstName.orEmpty(),
    surname = lastName.orEmpty(),
    username = username,
    email = email.orEmpty(),
    phoneNumber = phone.orEmpty(),
    dateOfBirth = dateOfBirth.orEmpty(),
    rewardPoints = rewardPoints,
)

fun PetDto.toEntity(): PetEntity = PetEntity(
    petId = id,
    userId = owner.orEmpty(),
    name = name,
    species = species.orEmpty(),
    breed = breed.orEmpty(),
    dateOfBirth = dateOfBirth.orEmpty(),
    favoriteToy = favoriteToy.orEmpty(),
    gender = gender.orEmpty(),
    profileImage = profileImage.orEmpty(),
)

fun RecurrenceDto.toRule(legacyRepeat: String?): RecurrenceRule {
    val structured = RepeatFrequency.fromApi(frequency)
    val frequency = if (structured == RepeatFrequency.NONE) {
        RepeatFrequency.fromApi(legacyRepeat)
    } else {
        structured
    }
    return RecurrenceRule(
        frequency = frequency,
        interval = (interval ?: 1).coerceAtLeast(1),
        unit = RecurrenceUnit.fromApi(unit),
        weekDays = weekDays.orEmpty().filter { it in 0..6 },
        monthDay = monthDay?.takeIf { it in 1..31 },
        endsOnDate = ends.equals("on_date", ignoreCase = true),
        endDate = endDate.orEmpty(),
    )
}

fun RecurrenceRule.toDto(): RecurrenceDto = RecurrenceDto(
    frequency = frequency.apiValue,
    interval = interval.coerceAtLeast(1),
    unit = unit.apiValue,
    weekDays = weekDays,
    monthDay = monthDay,
    ends = if (endsOnDate) "on_date" else "never",
    endDate = endDate.takeIf { it.isNotBlank() },
)

fun RecurrenceRule.toLegacyRepeat(): String = when (frequency) {
    RepeatFrequency.NONE -> "None"
    RepeatFrequency.DAILY -> "Daily"
    RepeatFrequency.WEEKLY -> "Weekly"
    RepeatFrequency.BIWEEKLY -> "Every 2 weeks"
    RepeatFrequency.MONTHLY -> "Monthly"
    RepeatFrequency.YEARLY -> "Yearly"
    RepeatFrequency.CUSTOM -> "Custom"
}

fun ReminderDto.toReminderWithPet(): ReminderWithPet = ReminderWithPet(
    reminder = ReminderEntity(
        reminderId = id,
        petId = pet,
        title = title,
        category = ReminderCategory.fromApi(type).name,
        dueDate = date.orEmpty(),
        dueTime = time.orEmpty(),
        recurrence = recurrence?.toRule(repeat) ?: RecurrenceDto().toRule(repeat),
        isCompleted = completed,
    ),
    petName = petName.orEmpty(),
)
