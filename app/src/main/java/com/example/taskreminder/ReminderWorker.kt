package com.example.taskreminder

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()

        ReminderScheduler.ensureChannel(applicationContext)

        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.ChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hatırlatıcı")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(title.hashCode(), notification)
        return Result.success()
    }
}
