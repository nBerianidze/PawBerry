package com.example.pawberry.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pawberry.MainActivity
import com.example.pawberry.R
import com.example.pawberry.data.db.ReminderEntity
import com.example.pawberry.data.db.ReminderWithPet
import com.example.pawberry.data.model.RecurrenceCalculator
import com.example.pawberry.data.model.RepeatFrequency
import org.json.JSONArray
import org.json.JSONObject

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun sync(reminders: List<ReminderWithPet>) {
        ensureChannel()
        snapshotIds().forEach { cancel(it) }
        val snapshot = JSONArray()
        reminders.forEach { item ->
            if (!item.reminder.isCompleted) {
                schedule(item.reminder, item.petName)
                snapshot.put(toJson(item))
            }
        }
        prefs.edit().putString(KEY_SNAPSHOT, snapshot.toString()).apply()
    }

    private fun snapshotIds(): List<String> {
        val raw = prefs.getString(KEY_SNAPSHOT, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    add(array.getJSONObject(index).optString("id"))
                }
            }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun restoreFromSnapshot() {
        val raw = prefs.getString(KEY_SNAPSHOT, null) ?: return
        val array = JSONArray(raw)
        val items = buildList {
            for (index in 0 until array.length()) {
                add(fromJson(array.getJSONObject(index)))
            }
        }
        sync(items)
    }

    fun cancel(reminderId: String) {
        alarmManager.cancel(pendingIntent(reminderId, "", ""))
    }

    private fun schedule(reminder: ReminderEntity, petName: String) {
        val triggerAt = RecurrenceCalculator.nextTriggerMillis(
            dueDate = reminder.dueDate,
            dueTime = reminder.dueTime,
            rule = reminder.recurrence,
        ) ?: return
        val intent = pendingIntent(reminder.reminderId, reminder.title, petName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    private fun pendingIntent(reminderId: String, title: String, petName: String): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_REMINDER
            data = android.net.Uri.parse("pawberry://reminder/$reminderId")
            putExtra(EXTRA_ID, reminderId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_PET, petName)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(reminderId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pet reminders",
            NotificationManager.IMPORTANCE_HIGH,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "pawberry_reminders"
        const val ACTION_REMINDER = "com.example.pawberry.REMINDER_ALARM"
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_TITLE = "reminder_title"
        const val EXTRA_PET = "reminder_pet"
        private const val PREFS = "pawberry_reminder_alarms"
        private const val KEY_SNAPSHOT = "snapshot"

        fun requestCode(id: String): Int = id.hashCode() and 0x7fffffff

        private fun toJson(item: ReminderWithPet): JSONObject = JSONObject().apply {
            put("id", item.reminder.reminderId)
            put("petId", item.reminder.petId)
            put("title", item.reminder.title)
            put("category", item.reminder.category)
            put("dueDate", item.reminder.dueDate)
            put("dueTime", item.reminder.dueTime)
            put("petName", item.petName)
            put("completed", item.reminder.isCompleted)
            put("frequency", item.reminder.recurrence.frequency.apiValue)
            put("interval", item.reminder.recurrence.interval)
            put("unit", item.reminder.recurrence.unit.apiValue)
            put("weekDays", JSONArray(item.reminder.recurrence.weekDays))
            put("monthDay", item.reminder.recurrence.monthDay)
            put("endsOnDate", item.reminder.recurrence.endsOnDate)
            put("endDate", item.reminder.recurrence.endDate)
        }

        private fun fromJson(json: JSONObject): ReminderWithPet {
            val weekDays = json.optJSONArray("weekDays")
            val days = buildList {
                if (weekDays != null) {
                    for (index in 0 until weekDays.length()) add(weekDays.getInt(index))
                }
            }
            val reminder = ReminderEntity(
                reminderId = json.getString("id"),
                petId = json.optString("petId"),
                title = json.optString("title"),
                category = json.optString("category"),
                dueDate = json.optString("dueDate"),
                dueTime = json.optString("dueTime"),
                isCompleted = json.optBoolean("completed"),
                recurrence = com.example.pawberry.data.model.RecurrenceRule(
                    frequency = RepeatFrequency.fromApi(json.optString("frequency")),
                    interval = json.optInt("interval", 1),
                    unit = com.example.pawberry.data.model.RecurrenceUnit.fromApi(json.optString("unit")),
                    weekDays = days,
                    monthDay = json.optInt("monthDay").takeIf { json.has("monthDay") && !json.isNull("monthDay") && it > 0 },
                    endsOnDate = json.optBoolean("endsOnDate"),
                    endDate = json.optString("endDate"),
                ),
            )
            return ReminderWithPet(reminder, json.optString("petName"))
        }
    }
}

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler(context.applicationContext).restoreFromSnapshot()
            return
        }
        if (intent.action != ReminderScheduler.ACTION_REMINDER) return

        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE).orEmpty()
        val petName = intent.getStringExtra(ReminderScheduler.EXTRA_PET).orEmpty()
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_ID).orEmpty()
        val launch = PendingIntent.getActivity(
            context,
            ReminderScheduler.requestCode("open-$reminderId"),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminders)
            .setContentTitle(if (petName.isBlank()) "PawBerry reminder" else petName)
            .setContentText(title.ifBlank { "You have a pet care reminder." })
            .setAutoCancel(true)
            .setContentIntent(launch)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(
                ReminderScheduler.requestCode(reminderId),
                notification,
            )
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS may be denied on API 33+.
        }
        ReminderScheduler(context.applicationContext).restoreFromSnapshot()
    }
}
