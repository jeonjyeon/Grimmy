package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName
import java.util.Date

data class DailyRecordSaveRequest(
    @SerializedName("userId") val userId : Int,
    @SerializedName("dailyDayRecording") val dailyDayRecording : Date,
    @SerializedName("drawing") val drawing : String,
    @SerializedName("drawingTime") val drawingTime : String,
    @SerializedName("feedback") val feedback : String?,
    @SerializedName("dfficultIssue") val dfficultIssue : String?,
    @SerializedName("goodIssue") val goodIssue : String?,
    @SerializedName("todayMood") val todayMood : String?,
    @SerializedName("moodDetail") val moodDetail : String?,
    @SerializedName("question") val question : String?,
    @SerializedName("createdAt") val createdAt : String,
    @SerializedName("updatedAt") val updatedAt : String
)
