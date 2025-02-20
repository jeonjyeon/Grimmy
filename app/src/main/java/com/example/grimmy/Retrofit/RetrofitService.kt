package com.example.grimmy.Retrofit

import com.example.grimmy.Retrofit.Request.BigGoalRequest
import com.example.grimmy.Retrofit.Request.BirthRequest
import com.example.grimmy.Retrofit.Request.CategoryRequest
import com.example.grimmy.Retrofit.Request.ClassAddRequest
import com.example.grimmy.Retrofit.Request.ClassUpdateRequest
import com.example.grimmy.Retrofit.Request.DailyCommentSaveRequest
import com.example.grimmy.Retrofit.Request.KakaoAccessTokenRequest
import com.example.grimmy.Retrofit.Request.NicknameRequest
import com.example.grimmy.Retrofit.Request.StatusRequest
import com.example.grimmy.Retrofit.Request.StuffDetailRequest
import com.example.grimmy.Retrofit.Request.StuffRequest
import com.example.grimmy.Retrofit.Request.SubGoalReqeust
import com.example.grimmy.Retrofit.Request.TestCommentSaveRequest

import com.example.grimmy.Retrofit.Response.BigGoalResponse
import com.example.grimmy.Retrofit.Response.ClassAddResponse
import com.example.grimmy.Retrofit.Response.DailyCommentGetResponse
import com.example.grimmy.Retrofit.Response.DailyRecordGetResponse
import com.example.grimmy.Retrofit.Response.DailyRecordSaveResponse
import com.example.grimmy.Retrofit.Response.KakaoAccessTokenResponse
import com.example.grimmy.Retrofit.Response.GoalGetResponse
import com.example.grimmy.Retrofit.Response.MonthlyRecordGetResponse
import com.example.grimmy.Retrofit.Response.StuffDetailResponse
import com.example.grimmy.Retrofit.Response.StuffResponse
import com.example.grimmy.Retrofit.Response.SubGoalResponse
import com.example.grimmy.Retrofit.Response.TestCommentGetResponse
import com.example.grimmy.Retrofit.Response.TestCommentSaveResponse
import com.example.grimmy.Retrofit.Response.TestRecordGetResponse
import com.example.grimmy.Retrofit.Response.TestRecordSaveResponse
import com.example.grimmy.Retrofit.Response.UserResponse
import com.example.grimmy.Retrofit.Response.GetScheduleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PATCH
import retrofit2.http.PUT
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
    ):Call<UserResponse>

    @PATCH("/user/{userId}/birthYear")
    fun updateBirthYear(
        @Path("userId") userId: Int,
        @Body request: BirthRequest
    ):Call<UserResponse>

    @PATCH("/user/{userId}/status")
    fun updateStatus(
        @Path("userId") userId: Int,
        @Body request: StatusRequest
    ):Call<UserResponse>

    @PATCH("/user/{userId}/category")
    fun updateCategory(
        @Path("userId") userId: Int,
        @Body request: CategoryRequest
    ):Call<UserResponse>

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

    // 큰 목표 작성
    @POST("/goal/bigGoal")
    fun postBigGoalSave(
        @Body request: BigGoalRequest
    ): Call<BigGoalResponse>

    // 세부 목표 작성
    @POST("/goal/{goal_id}/subgoal")
    fun postSubGoalSave(
        @Path("goal_id") goal_id: Int,
        @Body request: SubGoalReqeust
    ): Call<SubGoalResponse>

    // 재료 리마인더 큰 제목 작성
    @POST("/stuff")
    fun postStuffSave(
        @Body request: StuffRequest
    ): Call<StuffResponse>

    // 재료 리마인더 세부 제목 작성
    @POST("/stuff/{stuff_id}/detail")
    fun postStuffDetailSave(
        @Path("stuff_id") stuff_id: Int,
        @Body request: StuffDetailRequest
    ): Call<StuffDetailResponse>

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

    // 목표 조회
    @GET("/goal")
    fun getGoal(): Call<GoalGetResponse>

    // 시간표 조회
    @GET("/schedule/{userId}/{year}")
    fun getSchedule(
        @Path("userId") userId: Int,
        @Path("year") year: Int
    ): Call<GetScheduleResponse>

    // 일정 조회
    @GET("/schedule-details/{detailId}")
    fun getScheduleDetail(
        @Path("detailId") userId: Int
    ): Call<ClassAddResponse>

    // 시간표 수업 추가
    @POST("/schedule-details/{userId}")
    fun addClass(
        @Path("userId") userId: Int,
        @Body request: ClassAddRequest
    ): Call<ClassAddResponse>

    // 시간표 수업 삭제
    @DELETE("/scedule-details/{detailId}")
    fun deleteClass(
        @Path("detailId") scheduleDetailId: Int
    ): Call<Void>

    // 시간표 수업 수정
    @PUT("/scedule-details/{scheduleDetailId}")
    fun updateClass(
        @Path("detailId") scheduleDetailId: Int,
        @Body request: ClassUpdateRequest
    ): Call<ClassAddResponse>
}