package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class TestRecordGetRequest(
    @SerializedName("userId") val userId : Int,
    @SerializedName("date") val date : String
)
