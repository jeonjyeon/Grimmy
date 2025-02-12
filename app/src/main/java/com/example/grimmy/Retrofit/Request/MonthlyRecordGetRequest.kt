package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class MonthlyRecordGetRequest(
    @SerializedName("userId") val userId : Int,
    @SerializedName("year") val year : Int,
    @SerializedName("month") val month : Int
)
