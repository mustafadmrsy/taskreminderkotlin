package com.example.taskreminder

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val focusDurationMillis = inputData.getLong("focus_duration_millis", 0L)

        ReminderScheduler.ensureChannel(applicationContext)

        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.ChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hatırlatıcı")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(title.hashCode(), notification)

        if (focusDurationMillis > 0L) {
            runBlocking {
                FocusPreferences.startFocusSession(applicationContext, focusDurationMillis)
            }
        }
        return Result.success()
    }
}
