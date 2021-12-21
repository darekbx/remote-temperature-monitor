package com.darekbx.temperaturemonitor.nordicble

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import com.darekbx.temperaturemonitor.ble.BluetoothService
import com.darekbx.temperaturemonitor.ble.ConnectionDescription
import com.darekbx.temperaturemonitor.ble.DeviceStatus
import com.darekbx.temperaturemonitor.ble.NotificationUtil
import com.darekbx.temperaturemonitor.repository.Entity
import com.darekbx.temperaturemonitor.repository.EntityDao
import com.darekbx.temperaturemonitor.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.support.v18.scanner.*
import org.koin.android.ext.android.inject
import java.util.*

class BluetoothService: Service() {

    private val entityDao: EntityDao by inject()
    private val notificationUtil: NotificationUtil by inject()

    companion object {
        var IS_SERVICE_ACTIVE = false
    }

    private val connectionDescription by lazy {
        ConnectionDescription(
            "M5StickC",
            UUID.fromString("89409171-FE10-40B7-80A3-398A8C219855".lowercase()),
            UUID.fromString("89409171-FE10-40AA-80A3-398A8C219855".lowercase())
        )
    }

    private fun notifyStatus(deviceStatus: DeviceStatus) {
        sendBroadcast(Intent(MainActivity.DEVICE_STATUS_ACTION).apply {
            when (deviceStatus) {
                DeviceStatus.NOTIFICATIONS_SET -> putExtra(MainActivity.DEVICE_STATUS, true)
                DeviceStatus.DISCONNECTED -> putExtra(MainActivity.DEVICE_STATUS, false)
                else -> { /* do nothing */ }
            }
        })
    }

    private var clientManager: ClientManager? = null

    override fun onCreate() {
        super.onCreate()
        BluetoothService.IS_SERVICE_ACTIVE = true

        scanForDevices()

        val notification = notificationUtil.createNotification(
            "Current readings",
            "-° / -v"
        )
        startForeground(
            BluetoothService.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
    }

    private fun scanForDevices() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings: ScanSettings = ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(5000)
            .setUseHardwareBatchingIfSupported(true)
            .build()
        val filters: MutableList<ScanFilter> = ArrayList()
        filters.add(ScanFilter.Builder().setDeviceName(connectionDescription.deviceName).build())
        scanner.startScan(filters, settings, scanCallback)
    }

    private val scanCallback = object: ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            results
                .firstOrNull { it.device.name == connectionDescription.deviceName }
                ?.let {
                    addDevice(it.device)
                    stopScanner()
                }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            addDevice(result.device)
            stopScanner()
        }
    }

    private fun stopScanner() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.stopScan(scanCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        disableBleServices()
        stopScanner()
        BluetoothService.IS_SERVICE_ACTIVE = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun disableBleServices() {
        clientManager?.close()
    }

    private fun addDevice(device: BluetoothDevice) {
        clientManager = ClientManager()
        clientManager!!.connect(device).useAutoConnect(true).enqueue()
    }

    private inner class ClientManager: BleManager(this) {

        override fun getGattCallback(): BleManagerGattCallback = GattCallback()

        private inner class GattCallback : BleManagerGattCallback() {

            private var myCharacteristic: BluetoothGattCharacteristic? = null

            override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                val service = gatt.getService(connectionDescription.serviceUUID)
                myCharacteristic =
                    service?.getCharacteristic(connectionDescription.notificationUUID)
                val myCharacteristicProperties = myCharacteristic?.properties ?: 0
                return myCharacteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
            }

            override fun initialize() {
                setNotificationCallback(myCharacteristic).with { _, data ->
                    if (data.value != null) {
                        parseNotificationData(data)
                    }
                }

                notifyStatus(DeviceStatus.CONNECTING)
                beginAtomicRequestQueue()
                    .add(enableNotifications(myCharacteristic)
                        .fail { _: BluetoothDevice?, _: Int ->
                            notifyStatus(DeviceStatus.FAILED)
                            disconnect().enqueue()
                        }
                    )
                    .done {
                        notifyStatus(DeviceStatus.CONNECTED)
                        notifyStatus(DeviceStatus.NOTIFICATIONS_SET)
                    }
                    .enqueue()
            }

            override fun onServicesInvalidated() {
                myCharacteristic = null
            }
        }
    }

    private fun parseNotificationData(data: Data) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val value = String(data.value!!, Charsets.UTF_8)
                val chunks = value.split(';')
                val entity = Entity(
                    null,
                    System.currentTimeMillis(),
                    chunks[1].toFloat(),
                    chunks[2].toFloat(),
                    chunks[0].toFloat()
                )

                entityDao.add(entity)
                notificationUtil.updateNotification(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
