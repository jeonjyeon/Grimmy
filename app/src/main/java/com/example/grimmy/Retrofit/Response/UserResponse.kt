package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class UserResponse (
    @SerializedName("nickname") val nickname: String,
    @SerializedName("birthYear") val birthYear: String,
    @SerializedName("status") val studentStatus: String,
    @SerializedName("category") val category: List<String>
)