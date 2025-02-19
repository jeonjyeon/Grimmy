package com.example.grimmy.Retrofit

import com.example.grimmy.Retrofit.Request.BirthRequest
import com.example.grimmy.Retrofit.Request.CategoryRequest
import com.example.grimmy.Retrofit.Request.DailyCommentSaveRequest
import com.example.grimmy.Retrofit.Request.DailyRecordGetRequest
import com.example.grimmy.Retrofit.Request.DailyRecordSaveRequest
import com.example.grimmy.Retrofit.Request.KakaoAccessTokenRequest
import com.example.grimmy.Retrofit.Request.NicknameRequest
import com.example.grimmy.Retrofit.Request.StatusRequest
import com.example.grimmy.Retrofit.Request.TestCommentSaveRequest

import com.example.grimmy.Retrofit.Request.TestRecordSaveRequest
import com.example.grimmy.Retrofit.Response.DailyCommentGetResponse
import com.example.grimmy.Retrofit.Response.DailyRecordGetResponse
import com.example.grimmy.Retrofit.Response.DailyRecordSaveResponse
import com.example.grimmy.Retrofit.Response.KakaoAccessTokenResponse
import com.example.grimmy.Retrofit.Response.MonthlyRecordGetResponse
import com.example.grimmy.Retrofit.Response.TestCommentGetResponse
import com.example.grimmy.Retrofit.Response.TestCommentSaveResponse
import com.example.grimmy.Retrofit.Response.TestRecordGetResponse
import com.example.grimmy.Retrofit.Response.TestRecordSaveResponse
import com.kakao.sdk.auth.model.AccessTokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PATCH
import retrofit2.http.Part

interface RetrofitService {
    @POST("/auth/kakao")
    fun sendAccessToken(
        @Body request: KakaoAccessTokenRequest
    ): Call<KakaoAccessTokenResponse>

    @PATCH("/user/{userId}/nickname")
    fun updateNickname(
        @Path("userId") userId: Int,
        @Body request: NicknameRequest
    ):Call<Void>

    @PATCH("/user/{userId}/birthYear")
    fun updateBirthYear(
        @Path("userId") userId: Int,
        @Body request: BirthRequest
    ):Call<Void>

    @PATCH("/user/{userId}/status")
    fun updateStatus(
        @Path("userId") userId: Int,
        @Body request: StatusRequest
    ):Call<Void>

    @PATCH("/user/{userId}/category")
    fun updateCategory(
        @Path("userId") userId: Int,
        @Body request: CategoryRequest
    ):Call<Void>

    // 데일리 기록 작성
    @Multipart
    @POST("/record/daily/save")
    fun postDailyRecordSave(
        @Part drawing : MultipartBody.Part,
        @Part("request") request :RequestBody
    ):Call<DailyRecordSaveResponse>

    // 테스트 기록 작성
    @Multipart
    @POST("/record/test/save")
    fun postTestRecordSave(
        @Part drawing : MultipartBody.Part,
        @Part("request") request : RequestBody
    ):Call<TestRecordSaveResponse>

    // 데일리 코멘트 작성
    @POST("/record/daily/comment/{dailyId}/save")
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