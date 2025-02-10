package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class DailyRecordSaveRequest(
    @SerializedName("userId") val userId : Int,
    @SerializedName("dailyDayRecording") val dailyDayRecording : String,
    @SerializedName("drawing") val drawing : String,
    @SerializedName("feedback") val feedback : String,
    @SerializedName("difficultIssue") val difficultIssue : String,
    @SerializedName("goodIssue") val goodIssue : String,
    @SerializedName("todayMood") val todayMood : String,
    @SerializedName("question") val question : String,
    @SerializedName("createdAt") val createdAt : String,
    @SerializedName("updateAt") val updateAt : String
)
