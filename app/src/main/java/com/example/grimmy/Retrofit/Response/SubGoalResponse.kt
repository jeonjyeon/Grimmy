package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class SubGoalResponse(
    @SerializedName("subgoal_id") val subgoal_id : String,
    @SerializedName("goal_id") val goal_id : String,
    @SerializedName("title") val title : String,
    @SerializedName("created_at") val created_at : String
)
