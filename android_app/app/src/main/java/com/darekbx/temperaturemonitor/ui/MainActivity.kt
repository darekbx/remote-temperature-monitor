package com.darekbx.temperaturemonitor.ui

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.darekbx.temperaturemonitor.R
import com.darekbx.temperaturemonitor.nordicble.BluetoothService
import com.darekbx.temperaturemonitor.databinding.ActivityMainBinding
import com.darekbx.temperaturemonitor.repository.Entity
import com.darekbx.temperaturemonitor.system.PermissionRequester
import com.darekbx.temperaturemonitor.viewmodel.SensorViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val DEVICE_STATUS_ACTION = "deviceStatusAction"
        const val DEVICE_STATUS = "deviceStatus"
    }

    private val sensorViewModel: SensorViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding

    private val deviceStatusReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DEVICE_STATUS_ACTION) {
                val status = intent.getBooleanExtra(DEVICE_STATUS, false)
                updateButtonState(status)
                updateConnectingProgressState(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        requiredPermissions.runWithPermission {
            binding.stateButton.setOnClickListener { startStopService() }
            updateButtonState(BluetoothService.IS_SERVICE_ACTIVE)
        }

        sensorViewModel.entries().observe(this, { displayEntries(it) })
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(deviceStatusReceiver, IntentFilter(DEVICE_STATUS_ACTION))
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(deviceStatusReceiver)
        } catch (e: Exception) { }
    }

    private fun displayEntries(entries: List<Entity>) {
        val oldest = entries.minByOrNull { it.timestamp }
        val newest = entries.maxByOrNull { it.timestamp }

        if (newest != null && oldest != null) {
            binding.temperatureView.text = "${newest.temperature}°"
            binding.batteryView.text = "${newest.voltage}v"
            binding.dataCountView.text = "${entries.size}"
            binding.humidityView.text = if (newest.humidity == 0.0F) "n/a" else "${newest.humidity}%"

            val calendar = Calendar.getInstance().apply { this.timeInMillis = newest.timestamp }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            binding.timeView.text = "${hour.padNumber()}:${minute.padNumber()}"

            var diff = newest.timestamp - oldest.timestamp
            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60

            val hours: Long = diff / hoursInMilli
            diff = diff % hoursInMilli
            val minutes: Long = diff / minutesInMilli

            binding.elapsedTimeView.text = "${hours.padNumber()}h ${minutes.padNumber()}m"
        }

        with (binding.temperatureChart) {
            values = entries.map { it.temperature }
            unit = "°"
            guideDigits = 1
        }
        with (binding.voltageChart) {
            values = entries.map { it.voltage }
            unit = "v"
            guideDigits = 2
        }
    }

    private fun Int.padNumber() = toString().padStart(2, '0')
    private fun Long.padNumber() = toString().padStart(2, '0')

    private fun startStopService() {
        if (!isBluetoothEnabled()) {
            showSimpleDialog(R.string.bluetooth_is_off)
            return
        }
        if (isConnectingInProgress()) {
            return
        }
        if (BluetoothService.IS_SERVICE_ACTIVE) {
            stopService(serviceIntent)
            updateButtonState(false)
        } else {
            resetTopBar()
            sensorViewModel.reset()
            startForegroundService(serviceIntent)
            updateConnectingProgressState(true)
        }
    }

    private fun resetTopBar() {
        with (binding) {
            elapsedTimeView.text = "-h -m"
            timeView.text = "-:-"
            temperatureView.text = "-°"
            batteryView.text = "-v"
            dataCountView.text = "-"
            humidityView.text = "-%"
        }
    }

    private fun updateConnectingProgressState(isVisible: Boolean) {
        binding.connectingProgress.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun isConnectingInProgress() = binding.connectingProgress.visibility == View.VISIBLE

    private fun updateButtonState(isActive: Boolean) {
        binding.stateButton.setImageResource(
            when (isActive) {
                true -> R.drawable.ic_stop
                else -> R.drawable.ic_record
            }
        )
    }

    private fun isBluetoothEnabled(): Boolean {
        val adapter = bluetoothManager.adapter
        return when {
            adapter == null -> false
            !adapter.isEnabled -> false
            else -> true
        }
    }

    private val requiredPermissions by lazy {
        PermissionRequester(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE
            ),
            onDenied = { showSimpleDialog(R.string.permissions_are_required) }
        )
    }

    private fun showSimpleDialog(messageResId: Int) {
        AlertDialog.Builder(this)
            .setMessage(messageResId)
            .setPositiveButton(R.string.button_ok, null)
            .show()
    }

    private val bluetoothManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val serviceIntent by lazy { Intent(this, BluetoothService::class.java) }
}
