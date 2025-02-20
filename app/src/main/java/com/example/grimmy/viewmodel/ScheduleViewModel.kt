package com.example.grimmy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grimmy.Retrofit.Response.ClassAddResponse

class ScheduleViewModel : ViewModel() {

    private val _classSchedules = MutableLiveData<List<ClassAddResponse>>()
    val classSchedules: LiveData<List<ClassAddResponse>> get() = _classSchedules

    fun setClassSchedules(newSchedules: List<ClassAddResponse>) {
        _classSchedules.value = newSchedules
    }
}
