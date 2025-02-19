package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class StuffDetailResponse(
    @SerializedName("title") val substuff_id : Int,
    @SerializedName("title") val stuff_id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("title") val is_completed : String,
    @SerializedName("title") val alarm_time : String,
    @SerializedName("title") val created_at : String
)
