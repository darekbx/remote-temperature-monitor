package com.darekbx.temperaturemonitor.ble

import java.util.*

class SensorOutput(
    val temperature: Float,
    val humidity: Float,
    val voltage: Float
)

class ConnectionDescription(
    val deviceName: String,
    val serviceUUID: UUID,
    val notificationUUID: UUID
)

enum class DeviceStatus {
    CONNECTING,
    CONNECTED,
    NOTIFICATIONS_SET,
    DISCONNECTED,
    FAILED
}
