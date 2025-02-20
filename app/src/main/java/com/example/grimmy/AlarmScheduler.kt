package com.example.grimmy.alarm

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar

object AlarmScheduler {
    fun scheduleAlarm(context: Context, hour: Int, minute: Int, title: String, message: String) {
        val now = Calendar.getInstance()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (alarmTime.before(now)) {
            alarmTime.add(Calendar.DAY_OF_MONTH, 1) // 만약 시간이 이미 지났다면 다음 날로 설정
        }

        val delay = alarmTime.timeInMillis - now.timeInMillis

        val data = workDataOf(
            "title" to title,
            "message" to message
        )

        val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
