package com.darekbx.temperaturemonitor.system

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PermissionRequester(
    val activity: AppCompatActivity?,
    private val permissions: Array<String>,
    val onDenied: () -> Unit = { }
) {

    private var onGranted: () -> Unit = { }

    private val requestPermissionLauncher =
        activity?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            when {
                permissions.all { it.value } -> onGranted()
                else -> onDenied()
            }
        }

    fun runWithPermission(onGranted: () -> Unit) {
        this.onGranted = onGranted
        requestPermissionLauncher?.launch(permissions)
    }
}