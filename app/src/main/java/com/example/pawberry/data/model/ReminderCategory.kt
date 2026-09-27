package com.example.pawberry.data.model

/**
 * Categories a reminder can belong to. The enum [name] is what gets stored in the
 * `reminders.category` column and used as a navigation argument, so it must stay
 * stable even if the displayed text changes.
 */
enum class ReminderCategory(
    val label: String,
    val pluralLabel: String,
) {
    VACCINATION("Vaccination", "Vaccinations"),
    MEDICATION("Medication", "Medications"),
    DEWORMING("Deworming", "Deworming"),
    FLEA_TICK("Flea & Tick", "Flea & Tick Treatments"),
    FEEDING("Feeding", "Feeding"),
    OTHER("Other", "Other");

    companion object {
        /** Categories shown as clickable sections on the pet profile. */
        val petProfileCategories = listOf(VACCINATION, MEDICATION, DEWORMING, FLEA_TICK)

        fun fromStored(value: String): ReminderCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}
