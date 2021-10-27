package com.darekbx.temperaturemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darekbx.temperaturemonitor.repository.EntityDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SensorViewModel(
    private val entityDao: EntityDao
): ViewModel() {

    fun entries() = entityDao.loadAll()

    fun reset() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                entityDao.deleteAll()
            }
        }
    }
}