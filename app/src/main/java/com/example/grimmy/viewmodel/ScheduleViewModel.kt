package com.example.grimmy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grimmy.ClassSchedule


class ScheduleViewModel : ViewModel() {
    private val _classSchedules = MutableLiveData<MutableList<ClassSchedule>>(mutableListOf())
    val classSchedules: LiveData<MutableList<ClassSchedule>> get() = _classSchedules

    fun addClass(classSchedule: ClassSchedule) {
        _classSchedules.value?.add(classSchedule)
        _classSchedules.value = _classSchedules.value // LiveData 업데이트
    }

    fun removeClass(className: String) {
        _classSchedules.value?.removeIf { it.className == className }
        _classSchedules.value = _classSchedules.value // Notify observers
    }
}