package com.example.pawberry.data.model

enum class RecurrenceUnit(val label: String, val apiValue: String) {
    DAY("Days", "day"),
    WEEK("Weeks", "week"),
    MONTH("Months", "month");

    companion object {
        fun fromApi(value: String?): RecurrenceUnit =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: DAY
    }
}

enum class RepeatFrequency(val label: String, val apiValue: String) {
    NONE("Does not repeat", "none"),
    DAILY("Every day", "daily"),
    WEEKLY("Every week", "weekly"),
    BIWEEKLY("Every 2 weeks", "biweekly"),
    MONTHLY("Every month", "monthly"),
    YEARLY("Every year", "yearly"),
    CUSTOM("Custom recurrence", "custom");

    companion object {
        fun fromApi(value: String?): RepeatFrequency {
            if (value.isNullOrBlank()) return NONE
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) }?.let { return it }
            return when (value.trim().lowercase()) {
                "none", "does not repeat" -> NONE
                "daily" -> DAILY
                "weekly" -> WEEKLY
                "every 2 weeks" -> BIWEEKLY
                "monthly" -> MONTHLY
                "yearly" -> YEARLY
                "custom" -> CUSTOM
                else -> NONE
            }
        }
    }
}

data class RecurrenceRule(
    val frequency: RepeatFrequency = RepeatFrequency.NONE,
    val interval: Int = 1,
    val unit: RecurrenceUnit = RecurrenceUnit.DAY,
    val weekDays: List<Int> = emptyList(),
    val monthDay: Int? = null,
    val endsOnDate: Boolean = false,
    val endDate: String = "",
) {
    fun describe(): String {
        val base = when (frequency) {
            RepeatFrequency.NONE -> "Does not repeat"
            RepeatFrequency.DAILY -> "Every day"
            RepeatFrequency.WEEKLY -> "Every week"
            RepeatFrequency.BIWEEKLY -> "Every 2 weeks"
            RepeatFrequency.MONTHLY -> "Every month"
            RepeatFrequency.YEARLY -> "Every year"
            RepeatFrequency.CUSTOM -> customDescription()
        }
        return if (endsOnDate && endDate.isNotBlank()) "$base until $endDate" else base
    }

    private fun customDescription(): String {
        val count = interval.coerceAtLeast(1)
        val unitLabel = when (unit) {
            RecurrenceUnit.DAY -> if (count == 1) "day" else "days"
            RecurrenceUnit.WEEK -> if (count == 1) "week" else "weeks"
            RecurrenceUnit.MONTH -> if (count == 1) "month" else "months"
        }
        val days = if (unit == RecurrenceUnit.WEEK && weekDays.isNotEmpty()) {
            " on " + weekDays.sorted().joinToString { WEEKDAY_LABELS.getOrElse(it) { it.toString() } }
        } else {
            ""
        }
        return "Every $count $unitLabel$days"
    }

    companion object {
        val WEEKDAY_LABELS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val WEEKDAY_FULL = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }
}
