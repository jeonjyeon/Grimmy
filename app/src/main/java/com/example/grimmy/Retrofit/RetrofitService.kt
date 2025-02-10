package com.example.grimmy.Retrofit

import retrofit2.Call
import com.example.grimmy.Retrofit.Request.DailyRecordSaveRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitService {
    @POST("/record/daily/save")
    fun postDailyRecordSave(
        @Body request : DailyRecordSaveRequest
    ):Call<Void>
}