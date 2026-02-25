package com.example.taskreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ExtraTitle) ?: return

        ReminderScheduler.ensureChannel(context)

        val notification = NotificationCompat.Builder(context, ReminderScheduler.ChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hatırlatıcı")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
    }

    companion object {
        private const val ExtraTaskId = "task_id"
        private const val ExtraTitle = "title"

        fun newIntent(context: Context, taskId: String, title: String): Intent {
            return Intent(context, ReminderReceiver::class.java)
                .putExtra(ExtraTaskId, taskId)
                .putExtra(ExtraTitle, title)
        }
    }
}
