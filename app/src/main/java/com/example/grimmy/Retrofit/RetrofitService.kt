package com.example.grimmy.Retrofit

import com.example.grimmy.Retrofit.Request.DailyRecordGetRequest
import retrofit2.Call
import com.example.grimmy.Retrofit.Request.DailyRecordSaveRequest
import com.example.grimmy.Retrofit.Request.TestRecordSaveRequest
import com.example.grimmy.Retrofit.Response.DailyRecordGetResponse
import com.example.grimmy.Retrofit.Response.MonthlyRecordGetResponse
import com.example.grimmy.Retrofit.Response.TestRecordGetResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitService {
    @POST("/record/daily/save")
    fun postDailyRecordSave(
        @Body request : DailyRecordSaveRequest
    ):Call<Void>

    @POST("/record/test/save")
    fun postTestRecordSave(
        @Body request : TestRecordSaveRequest
    ):Call<Void>

    @GET("/record/daily/get")
    fun getDailyRecordGet(
        @Query(value = "userId") userId: Int,
        @Query(value = "date") date: String
    ):Call<DailyRecordGetResponse>

    @GET("/record/test/get")
    fun getTestRecordGet(
        @Query(value = "userId") userId: Int,
        @Query(value = "date") date: String
    ):Call<TestRecordGetResponse>

    @GET("/record/monthly")
    fun getMonthlyRecord(
        @Query(value = "userId") userId: Int,
        @Query(value = "year") year: Int,
        @Query(value = "month") month: Int
    ):Call<List<MonthlyRecordGetResponse>>
}