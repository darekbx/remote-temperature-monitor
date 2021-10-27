package com.darekbx.temperaturemonitor.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Entity(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo val timestamp: Long,
    @ColumnInfo val temperature: Float,
    @ColumnInfo val humidity: Float,
    @ColumnInfo val voltage: Float
)