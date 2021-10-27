package com.darekbx.temperaturemonitor.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EntityDao {

    @Query("SELECT * FROM entity")
    fun loadAll(): LiveData<List<Entity>>

    @Query("DELETE FROM entity")
    fun deleteAll()

    @Insert
    fun add(entity: Entity)
}