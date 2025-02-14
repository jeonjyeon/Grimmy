package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class DailyCommentGetResponse(
    @SerializedName("id") val id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("content") val content : String,
    @SerializedName("x") val x : Float,
    @SerializedName("y") val y : Float
)
