package com.example.pawberry.data.model

import java.util.Calendar

object RecurrenceCalculator {
    fun nextTriggerMillis(
        dueDate: String,
        dueTime: String,
        rule: RecurrenceRule,
        nowMillis: Long = System.currentTimeMillis(),
    ): Long? {
        val start = calendarFrom(dueDate, dueTime) ?: return null
        val endMillis = if (rule.endsOnDate && rule.endDate.isNotBlank()) {
            calendarFrom(rule.endDate, "23:59")?.timeInMillis
        } else {
            null
        }

        if (rule.frequency == RepeatFrequency.NONE) {
            return start.timeInMillis.takeIf { it > nowMillis }
        }

        var cursor = start.clone() as Calendar
        var guard = 0
        while (cursor.timeInMillis <= nowMillis && guard < 500) {
            advance(cursor, rule, start)
            guard++
        }
        if (cursor.timeInMillis <= nowMillis) return null
        if (endMillis != null && cursor.timeInMillis > endMillis) return null
        return cursor.timeInMillis
    }

    private fun advance(calendar: Calendar, rule: RecurrenceRule, original: Calendar) {
        val interval = rule.interval.coerceAtLeast(1)
        when (rule.frequency) {
            RepeatFrequency.NONE -> Unit
            RepeatFrequency.DAILY -> calendar.add(Calendar.DATE, 1)
            RepeatFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RepeatFrequency.BIWEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            RepeatFrequency.MONTHLY -> addMonthsClamped(calendar, 1, original.get(Calendar.DAY_OF_MONTH))
            RepeatFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
            RepeatFrequency.CUSTOM -> when (rule.unit) {
                RecurrenceUnit.DAY -> calendar.add(Calendar.DATE, interval)
                RecurrenceUnit.WEEK -> {
                    if (rule.weekDays.isNotEmpty()) {
                        advanceToNextSelectedWeekday(calendar, rule.weekDays, interval)
                    } else {
                        calendar.add(Calendar.WEEK_OF_YEAR, interval)
                    }
                }
                RecurrenceUnit.MONTH -> {
                    val day = rule.monthDay ?: original.get(Calendar.DAY_OF_MONTH)
                    addMonthsClamped(calendar, interval, day)
                }
            }
        }
    }

    private fun addMonthsClamped(calendar: Calendar, months: Int, preferredDay: Int) {
        calendar.add(Calendar.MONTH, months)
        val max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, preferredDay.coerceIn(1, max))
    }

    private fun advanceToNextSelectedWeekday(calendar: Calendar, weekDays: List<Int>, interval: Int) {
        val selected = weekDays.filter { it in 0..6 }.toSet()
        if (selected.isEmpty()) {
            calendar.add(Calendar.WEEK_OF_YEAR, interval)
            return
        }
        repeat(8) {
            calendar.add(Calendar.DATE, 1)
            val stored = calendar.get(Calendar.DAY_OF_WEEK) - 1
            if (stored in selected) {
                if (interval > 1 && stored == selected.minOrNull()) {
                    calendar.add(Calendar.WEEK_OF_YEAR, interval - 1)
                }
                return
            }
        }
    }

    fun calendarFrom(isoDate: String, isoTime: String): Calendar? {
        val dateParts = isoDate.split("-")
        if (dateParts.size != 3) return null
        val year = dateParts[0].toIntOrNull() ?: return null
        val month = dateParts[1].toIntOrNull() ?: return null
        val day = dateParts[2].toIntOrNull() ?: return null
        val timeParts = isoTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
