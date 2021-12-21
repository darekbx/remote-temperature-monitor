package com.darekbx.temperaturemonitor.ble

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.darekbx.temperaturemonitor.R
import com.darekbx.temperaturemonitor.repository.Entity
import com.darekbx.temperaturemonitor.ui.MainActivity

class NotificationUtil(val applicationContext: Application) {

    fun updateNotification(entity: Entity) {
        val notification = createNotification(
            "Current readings",
            "${entity.temperature}° / ${entity.voltage}v"
        )
        notificationManager.notify(BluetoothService.NOTIFICATION_ID, notification)
    }

    fun createNotification(title: String, text: String): Notification {

        val tracksIntent = Intent(applicationContext, MainActivity::class.java)
        val tracksPendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            tracksIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            BluetoothService.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_thermometer)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(tracksPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var channel =
            notificationManager.getNotificationChannel(BluetoothService.NOTIFICATION_CHANNEL_ID)
        if (channel == null) {
            channel = NotificationChannel(
                BluetoothService.NOTIFICATION_CHANNEL_ID,
                title,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return builder.build()
    }

    private val notificationManager by lazy { applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
}
