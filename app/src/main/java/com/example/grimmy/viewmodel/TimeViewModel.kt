package com.example.grimmy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grimmy.ReminderTime

class TimeViewModel : ViewModel() {
    private val _paintingReminderTime = MutableLiveData<ReminderTime?>() // 그림 알림 시간
    val paintingReminderTime: LiveData<ReminderTime?> get() = _paintingReminderTime

    private val _materialReminderTime = MutableLiveData<ReminderTime?>() // 재료 알림 시간
    val materialReminderTime: LiveData<ReminderTime?> get() = _materialReminderTime

    fun setPaintingReminderTime(hour: Int, minute: Int) {
        _paintingReminderTime.value = ReminderTime(hour, minute)
    }

    fun setMaterialReminderTime(hour: Int, minute: Int) {
        _materialReminderTime.value = ReminderTime(hour, minute)
    }
}
