package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class TestCommentSaveRequest(
    @SerializedName("title") val title : String,
    @SerializedName("content") val content : String,
    @SerializedName("x") val x : Float,
    @SerializedName("y") val y : Float
)
