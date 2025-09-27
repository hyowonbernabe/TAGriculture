package com.example.tagriculture.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.tagriculture.R
import com.example.tagriculture.data.database.Notification

object NotificationHelper {

    private const val CHANNEL_ID = "tagriculture_alerts"
    private const val CHANNEL_NAME = "Livestock Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications for important livestock events"

    /**
     * Creates the notification channel. This only needs to be run once.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and displays a notification popup.
     */
    fun showNotification(context: Context, notification: Notification) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alert: ${notification.animalName}")
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // TODO: In a real app, set a PendingIntent to open the app to the specific animal's page

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notification.id.toInt(), builder.build())
        }
    }
}