package com.darekbx.temperaturemonitor.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.darekbx.temperaturemonitor.R
import com.darekbx.temperaturemonitor.repository.Entity
import com.darekbx.temperaturemonitor.repository.EntityDao
import com.darekbx.temperaturemonitor.ui.MainActivity
import com.darekbx.temperaturemonitor.ui.MainActivity.Companion.DEVICE_STATUS
import com.darekbx.temperaturemonitor.ui.MainActivity.Companion.DEVICE_STATUS_ACTION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*

class BluetoothService : Service() {

    private val entityDao: EntityDao by inject()

    companion object {
        var IS_SERVICE_ACTIVE = false
        const val NOTIFICATION_ID = 210
        const val NOTIFICATION_CHANNEL_ID = "sensor_channel_id"
    }

    override fun onCreate() {
        super.onCreate()

        val notification = createNotification(
            "Current readings",
            "-° / -v"
        )
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        IS_SERVICE_ACTIVE = true

        with(bluetoothConnection) {
            connect(connectionDescription)
            deviceStatus = { notifyStatus(it) }
            receivedNewData = { addEntry(it) }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        IS_SERVICE_ACTIVE = false
        bluetoothConnection.dispose()
    }

    private fun addEntry(it: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val chunks = it.split(';')
                val entity = Entity(
                    null,
                    System.currentTimeMillis(),
                    chunks[1].toFloat(),
                    chunks[2].toFloat(),
                    chunks[0].toFloat()
                )
                entityDao.add(entity)
                updateNotification(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun notifyStatus(deviceStatus: DeviceStatus) {
        sendBroadcast(Intent(DEVICE_STATUS_ACTION).apply {
            when (deviceStatus) {
                DeviceStatus.NOTIFICATIONS_SET -> putExtra(DEVICE_STATUS, true)
                DeviceStatus.DISCONNECTED -> putExtra(DEVICE_STATUS, false)
                else -> { /* do nothing */ }
            }
        })
    }

    private val connectionDescription by lazy {
        ConnectionDescription(
            "M5StickC",
            UUID.fromString("89409171-FE10-40B7-80A3-398A8C219855".lowercase()),
            UUID.fromString("89409171-FE10-40AA-80A3-398A8C219855".lowercase())
        )
    }

    private fun updateNotification(entity: Entity) {
        val notification = createNotification(
            "Current readings",
            "${entity.temperature}° / ${entity.voltage}v"
        )
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(title: String, text: String): Notification {

        val tracksIntent = Intent(applicationContext, MainActivity::class.java)
        val tracksPendingIntent = getActivity(
            applicationContext, 0,
            tracksIntent, FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_thermometer)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(tracksPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        if (channel == null) {
            channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                title,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return builder.build()
    }

    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val bluetoothManager by lazy { getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }

    private val bluetoothConnection by lazy {
        BluetoothConnection(
            applicationContext,
            bluetoothManager
        )
    }
}
