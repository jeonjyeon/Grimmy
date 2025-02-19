package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class BigGoalRequest(
    @SerializedName("title") val title : String,
    @SerializedName("type") val type : String
)