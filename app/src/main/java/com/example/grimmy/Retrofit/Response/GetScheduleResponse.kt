package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class GetScheduleResponse(
    @SerializedName("scheduleId") val scheduleId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("details") val details: List<ClassAddResponse>
)