package com.darekbx.temperaturemonitor.repository

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Entity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun entityDao() : EntityDao

    companion object {
        const val DB_NAME = "temperature_data"
    }
}