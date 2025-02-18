package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class StuffDetailRequest(
    @SerializedName("title") val title : String
)
