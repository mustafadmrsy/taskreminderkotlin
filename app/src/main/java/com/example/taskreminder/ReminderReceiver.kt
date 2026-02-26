package com.example.taskreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ExtraTitle) ?: return
        val focusDurationMillis = intent.getLongExtra(ExtraFocusDurationMillis, 0L)

        ReminderScheduler.ensureChannel(context)

        val notification = NotificationCompat.Builder(context, ReminderScheduler.ChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hatırlatıcı")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(title.hashCode(), notification)

        if (focusDurationMillis > 0L) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    FocusPreferences.startFocusSession(context.applicationContext, focusDurationMillis)
                } finally {
                    pending.finish()
                }
            }
        }
    }

    companion object {
        private const val ExtraTaskId = "task_id"
        private const val ExtraTitle = "title"
        private const val ExtraFocusDurationMillis = "focus_duration_millis"

        fun newIntent(context: Context, taskId: String, title: String, focusDurationMillis: Long): Intent {
            return Intent(context, ReminderReceiver::class.java)
                .putExtra(ExtraTaskId, taskId)
                .putExtra(ExtraTitle, title)
                .putExtra(ExtraFocusDurationMillis, focusDurationMillis)
        }
    }
}
