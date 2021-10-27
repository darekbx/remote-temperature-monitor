package com.darekbx.temperaturemonitor

import android.app.Application
import com.darekbx.temperaturemonitor.di.CommonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TemperatureMonitorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TemperatureMonitorApplication)
            modules(CommonModule.get())
        }
    }
}
