package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class StuffResponse(
    @SerializedName("stuff_id") val stuff_id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("created_at") val created_at : String
)
