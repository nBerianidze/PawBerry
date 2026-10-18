package com.example.pawberry

import com.example.pawberry.data.model.RecurrenceCalculator
import com.example.pawberry.data.model.RecurrenceRule
import com.example.pawberry.data.model.RepeatFrequency
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RecurrenceCalculatorTest {
    @Test
    fun oneShotInThePastIsNotScheduled() {
        val next = RecurrenceCalculator.nextTriggerMillis(
            dueDate = "2020-01-01",
            dueTime = "09:00",
            rule = RecurrenceRule(frequency = RepeatFrequency.NONE),
            nowMillis = System.currentTimeMillis(),
        )
        assertNull(next)
    }

    @Test
    fun dailyAdvancesPastNow() {
        val now = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DATE, -1)
        val next = RecurrenceCalculator.nextTriggerMillis(
            dueDate = isoDate(yesterday),
            dueTime = isoTime(yesterday),
            rule = RecurrenceRule(frequency = RepeatFrequency.DAILY),
            nowMillis = now.timeInMillis,
        )
        assertNotNull(next)
        assertTrue(next!! > now.timeInMillis)
    }

    @Test
    fun endDateStopsRecurrence() {
        val next = RecurrenceCalculator.nextTriggerMillis(
            dueDate = "2020-01-01",
            dueTime = "09:00",
            rule = RecurrenceRule(
                frequency = RepeatFrequency.DAILY,
                endsOnDate = true,
                endDate = "2020-01-02",
            ),
            nowMillis = System.currentTimeMillis(),
        )
        assertNull(next)
    }

    private fun isoDate(calendar: Calendar): String =
        String.format(
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )

    private fun isoTime(calendar: Calendar): String =
        String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
        )
}
