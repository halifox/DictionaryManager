package com.github.dictionary.importer

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.dictionary.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@SuppressLint("MissingPermission")
class NotificationTaskCallback(val context: Context, val id: Int) : DictionaryImporter.TaskCallback, KoinComponent {
    companion object {
        private const val CHANNEL_ID = "task_channel"
    }

    private var lastUpdateTime = System.currentTimeMillis()
    private val notificationManagerCompat by inject<NotificationManagerCompat>()

    private val notificationChannelBuilder = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
        .setName(context.getString(R.string.notification_name))
        .setDescription(context.getString(R.string.notification_description))
        .setSound(null, null)
        .setVibrationEnabled(false)

    private val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_search)
        .setProgress(0, 0, true)

    private fun notify0() {
        notificationManagerCompat.notify(id, notification.build())
    }

    override fun onParser(fileName: String) {
        notificationManagerCompat.createNotificationChannel(notificationChannelBuilder.build())
        notification
            .setContentTitle(fileName)
            .setContentText(context.getString(R.string.notification_task_parser))
            .setOngoing(true)
        notify0()
    }

    override fun onStart() {
        notification.setContentText(context.getString(R.string.notification_task_start))
        notify0()
    }

    override fun onProgress(index: Int, size: Int) {
        if (System.currentTimeMillis() - lastUpdateTime > 1000) {
            notification.setProgress(size, index, false)
            notify0()
            lastUpdateTime = System.currentTimeMillis()
        }
    }

    override fun onComplete() {
        notification
            .setContentText(context.getText(R.string.notification_task_complete))
            .setProgress(0, 0, false)
            .setOngoing(false)
        notify0()
    }

    override fun onException(e: Exception) {
        notification
            .setContentText(context.getString(R.string.notification_task_exception, e.message))
            .setProgress(0, 0, false)
            .setOngoing(false)
        notify0()
    }
}
