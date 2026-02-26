package com.example.taskreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Duration
import java.time.Instant

object AlarmScheduler {

    fun scheduleExact(
        context: Context,
        taskId: String,
        title: String,
        triggerAtEpochMillis: Long,
        focusDurationMillis: Long,
    ): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            ReminderReceiver.newIntent(context, taskId, title, focusDurationMillis),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val now = Instant.now()
        val trigger = Instant.ofEpochMilli(triggerAtEpochMillis)
        val delayMillis = Duration.between(now, trigger).toMillis().coerceAtLeast(0L)
        val safeTriggerAt = now.plusMillis(delayMillis).toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            safeTriggerAt,
            pendingIntent,
        )
        return true
    }

    fun cancel(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            ReminderReceiver.newIntent(context, taskId, "", 0L),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }
}
