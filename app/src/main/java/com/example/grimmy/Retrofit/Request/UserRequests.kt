package com.example.grimmy.Retrofit.Request

import com.google.gson.annotations.SerializedName

//data class User(
//    val nickname: String,
//    val birthYear: String,
//    val studentStatus: String,
//    val examType: List<String>
//)

data class NicknameRequest(
    @SerializedName("nickname") val nickname: String
)

data class BirthRequest(
    @SerializedName("birthYear") val birthYear: Int
)

data class StatusRequest(
    @SerializedName("status") val studentStatus: String
)

data class CategoryRequest(
    @SerializedName("category") val examType: List<String>
)
