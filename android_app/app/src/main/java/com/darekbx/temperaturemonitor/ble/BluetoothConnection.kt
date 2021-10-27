package com.darekbx.temperaturemonitor.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context

@SuppressLint("MissingPermission")
class BluetoothConnection(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {

    private var connectedDeviceGatt: BluetoothGatt? = null
    private lateinit var connectionDescription: ConnectionDescription

    var deviceStatus: (DeviceStatus) -> Unit = { }
    var receivedNewData: (String) -> Unit = { }

    fun connect(connectionDescription: ConnectionDescription) {
        this.connectionDescription = connectionDescription
        deviceStatus(DeviceStatus.CONNECTING)
        leScanner.startScan(scanCallback)
    }

    fun dispose() {
        connectedDeviceGatt?.disconnect()
        connectedDeviceGatt?.close()
        leScanner?.stopScan(scanCallback)
    }

    private fun connectGatt(device: BluetoothDevice) {
        leScanner.stopScan(scanCallback)
        connectedDeviceGatt = device.connectGatt(
            context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result
                ?.takeIf { isCorrectDevice(it.device) }
                ?.let { connectGatt(it.device) }
        }

        private fun isCorrectDevice(device: BluetoothDevice) =
            device.name == connectionDescription.deviceName
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            gatt: BluetoothGatt?,
            status: Int,
            newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> gatt?.discoverServices()
                BluetoothProfile.STATE_DISCONNECTED -> deviceStatus(DeviceStatus.DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(
            gatt: BluetoothGatt?,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    connectedDeviceGatt = gatt
                    deviceStatus(DeviceStatus.CONNECTED)
                    enableNotifications()
                }
                else -> deviceStatus(DeviceStatus.FAILED)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            if (characteristic != null) {
                receivedNewData(characteristic.getStringValue(0))
            }
        }
    }

    private fun enableNotifications() {
        connectedDeviceGatt
            ?.getService(connectionDescription.serviceUUID)
            ?.getCharacteristic(connectionDescription.notificationUUID)
            ?.let { characteristic ->
                connectedDeviceGatt?.setCharacteristicNotification(characteristic, true)
                characteristic.descriptors.forEach {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    connectedDeviceGatt?.writeDescriptor(it)
                }
                deviceStatus(DeviceStatus.NOTIFICATIONS_SET)
            }
    }

    private val leScanner by lazy { bluetoothManager.adapter.bluetoothLeScanner }
}
