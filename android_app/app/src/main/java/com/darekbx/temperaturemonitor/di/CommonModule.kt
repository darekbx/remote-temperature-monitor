package com.darekbx.temperaturemonitor.di

import androidx.room.Room
import com.darekbx.temperaturemonitor.ble.NotificationUtil
import com.darekbx.temperaturemonitor.repository.AppDatabase
import com.darekbx.temperaturemonitor.repository.AppDatabase.Companion.DB_NAME
import com.darekbx.temperaturemonitor.viewmodel.SensorViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object CommonModule {
    fun get() = module {
        single {
            Room.databaseBuilder(androidApplication(), AppDatabase::class.java, DB_NAME).build()
        }
        single { get<AppDatabase>().entityDao() }
        single { NotificationUtil(androidApplication()) }
        viewModel { SensorViewModel(get()) }
    }
}
