package com.example.grimmy.Retrofit

import com.example.grimmy.Retrofit.Request.DailyCommentSaveRequest
import com.example.grimmy.Retrofit.Request.DailyRecordGetRequest
import retrofit2.Call
import com.example.grimmy.Retrofit.Request.DailyRecordSaveRequest
import com.example.grimmy.Retrofit.Request.TestCommentSaveRequest
import com.example.grimmy.Retrofit.Request.TestRecordSaveRequest
import com.example.grimmy.Retrofit.Response.DailyCommentGetResponse
import com.example.grimmy.Retrofit.Response.DailyRecordGetResponse
import com.example.grimmy.Retrofit.Response.MonthlyRecordGetResponse
import com.example.grimmy.Retrofit.Response.TestCommentGetResponse
import com.example.grimmy.Retrofit.Response.TestCommentSaveResponse
import com.example.grimmy.Retrofit.Response.TestRecordGetResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {
    // 데일리 기록 작성
    @POST("/record/daily/save")
    fun postDailyRecordSave(
        @Body request : DailyRecordSaveRequest
    ):Call<Void>

    // 테스트 기록 작성
    @POST("/record/test/save")
    fun postTestRecordSave(
        @Body request : TestRecordSaveRequest
    ):Call<Void>

    // 데일리 코멘트 작성
    @POST("/record/test/comment/{dailyId}/save")
    fun postDailyCommentSave(
        @Path("dailyId") dailyId: Int,
        @Body request: DailyCommentSaveRequest
    ): Call<Void>

    // 테스트 코멘트 작성
    @POST("/record/test/comment/{dailyId}/save")
    fun postTestCommentSave(
        @Path("dailyId") dailyId: Int,
        @Body request: TestCommentSaveRequest
    ): Call<TestCommentSaveResponse>

    // 데일리 기록 조회
    @GET("/record/daily/get")
    fun getDailyRecordGet(
        @Query(value = "userId") userId: Int,
        @Query(value = "date") date: String
    ):Call<DailyRecordGetResponse>

    // 테스트 기록 조회
    @GET("/record/test/get")
    fun getTestRecordGet(
        @Query(value = "userId") userId: Int,
        @Query(value = "date") date: String
    ):Call<TestRecordGetResponse>

    // 먼슬리 기록 조회
    @GET("/record/monthly")
    fun getMonthlyRecord(
        @Query(value = "userId") userId: Int,
        @Query(value = "year") year: Int,
        @Query(value = "month") month: Int
    ):Call<List<MonthlyRecordGetResponse>>

    // 데일리 코멘트 조회
    @GET("/record/daily/comment/{dailyId}")
    fun getDailyComment(
        @Path("dailyId") dailyId: Int
    ): Call<List<DailyCommentGetResponse>>

    // 테스트 코멘트 조회
    @GET("/record/test/comment/{dailyId}")
    fun getTestComment(
        @Path("dailyId") dailyId: Int
    ): Call<List<TestCommentGetResponse>>
}