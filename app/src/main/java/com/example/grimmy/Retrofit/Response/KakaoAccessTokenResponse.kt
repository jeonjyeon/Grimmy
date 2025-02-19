package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class KakaoAccessTokenResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("userId") val userId: Int
)
