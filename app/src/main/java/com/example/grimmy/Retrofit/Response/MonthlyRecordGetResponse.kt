package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class MonthlyRecordGetResponse(
    @SerializedName("date") val date : String,
    @SerializedName("displayImage") val displayImage : String
)
