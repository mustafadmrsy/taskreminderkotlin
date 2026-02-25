package com.example.taskreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val ChannelId = "task_reminders"

    private const val DataKeyTitle = "title"

    fun scheduleReminder(
        context: Context,
        taskId: String,
        title: String,
        delayMillis: Long,
    ) {
        ensureChannel(context)

        val safeDelay = delayMillis.coerceAtLeast(0L)
        val data = Data.Builder()
            .putString(DataKeyTitle, title)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(safeDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(taskId)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("task_reminder_$taskId", ExistingWorkPolicy.REPLACE, request)
    }

    fun showInstantPreviewNotification(context: Context, title: String) {
        ensureChannel(context)

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(intent)
            return
        }

        val notification = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hatırlatıcı")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(ChannelId)
        if (existing != null) return

        val channel = NotificationChannel(
            ChannelId,
            "Görev Hatırlatıcıları",
            NotificationManager.IMPORTANCE_HIGH,
        )
        manager.createNotificationChannel(channel)
    }
}
