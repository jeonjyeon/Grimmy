package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class TestRecordGetResponse(
    @SerializedName("userId") val userId : Int,
    @SerializedName("testDayRecording") val testDayRecording : String,
    @SerializedName("drawing") val drawing : String,
    @SerializedName("drawingTime") val drawingTime : String,
    @SerializedName("score") val score : Int,
    @SerializedName("feedback") val feedback : String?,
    @SerializedName("difficultIssue") val difficultIssue : String?,
    @SerializedName("goodIssue") val goodIssue : String?,
    @SerializedName("drawingTime") val addIssue : String?,
    @SerializedName("drawingTime") val satisfication : String?,
    @SerializedName("todayMood") val todayMood : String?,
    @SerializedName("moodDetail") val moodDetail : String?,
    @SerializedName("question") val question : String?,
    @SerializedName("createdAt") val createdAt : String,
    @SerializedName("updateAt") val updateAt : String
)
