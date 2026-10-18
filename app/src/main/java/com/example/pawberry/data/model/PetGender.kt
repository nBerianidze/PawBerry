package com.example.pawberry.data.model

enum class PetGender(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    UNKNOWN("Unknown");

    fun toApi(): String = label

    companion object {
        fun fromApi(value: String?): PetGender? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull { it.label.equals(value, ignoreCase = true) }
        }
    }
}
