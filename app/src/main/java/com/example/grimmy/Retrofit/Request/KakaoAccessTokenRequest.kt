package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

data class KakaoAccessTokenRequest(
    @SerializedName("accessToken") val accessToken: String
)