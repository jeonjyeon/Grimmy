package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.descriptors.PrimitiveKind

data class RecordImageFileResponse(
    @SerializedName("imageUrl") val imageUrl : String
)
