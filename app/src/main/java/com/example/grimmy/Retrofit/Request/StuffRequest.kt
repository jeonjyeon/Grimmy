package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class StuffRequest(
    @SerializedName("title") val title : String
)
