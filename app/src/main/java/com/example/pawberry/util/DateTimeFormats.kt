package com.example.pawberry.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Dates are stored as `yyyy-MM-dd` and times as `HH:mm` so that plain string sorting in
 * SQL also sorts them chronologically. `java.time` is avoided here because the project
 * supports API 24.
 */
private const val ISO_DATE_PATTERN = "yyyy-MM-dd"
private const val ISO_TIME_PATTERN = "HH:mm"

private fun utcFormatter(pattern: String) = SimpleDateFormat(pattern, Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

/** Converts the UTC millis a Material date picker reports into `yyyy-MM-dd`. */
fun isoDateFromUtcMillis(millis: Long): String =
    utcFormatter(ISO_DATE_PATTERN).format(Date(millis))

/** Converts a stored `yyyy-MM-dd` back into UTC millis so a picker can pre-select it. */
fun utcMillisFromIsoDate(isoDate: String): Long? = try {
    utcFormatter(ISO_DATE_PATTERN).parse(isoDate)?.time
} catch (_: java.text.ParseException) {
    null
}

fun isoTime(hour: Int, minute: Int): String = String.format(Locale.US, "%02d:%02d", hour, minute)

/** `2026-03-08` becomes `Mar 8, 2026`; unparseable input is returned unchanged. */
fun formatDateForDisplay(isoDate: String): String {
    val millis = utcMillisFromIsoDate(isoDate) ?: return isoDate
    return utcFormatter("MMM d, yyyy").format(Date(millis))
}

/** `14:30` becomes `2:30 PM`; unparseable input is returned unchanged. */
fun formatTimeForDisplay(isoTime: String): String = try {
    val parsed = utcFormatter(ISO_TIME_PATTERN).parse(isoTime)
    if (parsed == null) isoTime else utcFormatter("h:mm a").format(parsed)
} catch (_: java.text.ParseException) {
    isoTime
}

fun hourOf(isoTime: String): Int = isoTime.substringBefore(":").toIntOrNull() ?: defaultHour()

fun minuteOf(isoTime: String): Int = isoTime.substringAfter(":", "").toIntOrNull() ?: 0

private fun defaultHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
