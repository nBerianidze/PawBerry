package com.example.pawberry.data.model

/**
 * How often a reminder repeats. The enum [name] is stored in the
 * `reminders.repeatFrequency` column.
 */
enum class RepeatFrequency(val label: String) {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    EVERY_3_MONTHS("Every 3 months"),
    EVERY_6_MONTHS("Every 6 months"),
    YEARLY("Yearly");

    companion object {
        fun fromStored(value: String): RepeatFrequency =
            entries.firstOrNull { it.name == value } ?: NONE
    }
}
