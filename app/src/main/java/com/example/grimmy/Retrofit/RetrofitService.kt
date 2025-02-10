package com.example.grimmy.Retrofit

import retrofit2.Call
import com.example.grimmy.Retrofit.Request.DailyRecordSaveRequest
import com.example.grimmy.Retrofit.Request.BirthRequest
import com.example.grimmy.Retrofit.Request.CategoryRequest
import com.example.grimmy.Retrofit.Request.NicknameRequest
import com.example.grimmy.Retrofit.Request.StatusRequest
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface RetrofitService {
    @PATCH("user/{userId}/nickname")
    fun updateNickname(
        @Body request: NicknameRequest
    ):Call<Void>

    @PATCH("/user/{userId}/birthYear")
    fun updateBirthYear(
        @Body request: BirthRequest
    ):Call<Void>

    @PATCH("/user/{userId}/status")
    fun updateStatus(
        @Body request: StatusRequest
    ):Call<Void>

    @PATCH("/user/{userId}/category")
    fun updateCategory(
        @Body request: CategoryRequest
    ):Call<Void>

    @POST("/record/daily/save")
    fun postDailyRecordSave(
        @Body request : DailyRecordSaveRequest
    ):Call<Void>
}