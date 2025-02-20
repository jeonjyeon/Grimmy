package com.example.grimmy.alarm

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class AlarmWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "알림"
        val message = inputData.getString("message") ?: "설정된 시간입니다!"

        NotificationHelper.showNotification(applicationContext, title, message)

        return Result.success()
    }
}
