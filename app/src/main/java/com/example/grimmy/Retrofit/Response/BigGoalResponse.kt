package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class BigGoalResponse(
    @SerializedName("goal_id") val goal_id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("type") val type : String,
    @SerializedName("created_at") val created_at : String
)
