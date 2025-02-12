package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class DailyRecordGetResponse(
    @SerializedName("userId") val userId : Int,
    @SerializedName("dailyDayRecording") val dailyDayRecording : String,
    @SerializedName("drawing") val drawing : String,
    @SerializedName("drawingTime") val drawingTime : String,
    @SerializedName("feedback") val feedback : String?,
    @SerializedName("difficultIssue") val difficultIssue : String?,
    @SerializedName("goodIssue") val goodIssue : String?,
    @SerializedName("todayMood") val todayMood : String?,
    @SerializedName("moodDetail") val moodDetail : String?,
    @SerializedName("question") val question : String?,
    @SerializedName("createdAt") val createdAt : String,
    @SerializedName("updateAt") val updateAt : String
)
