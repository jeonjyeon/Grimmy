package com.example.grimmy.Retrofit

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri
) : RequestBody() {

    override fun contentType(): MediaType? {
        // uri의 실제 MIME 타입을 가져옵니다.
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        return mimeType.toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        val cursor = contentResolver.query(uri, arrayOf(android.provider.MediaStore.Images.Media.SIZE), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        return 0L
    }

    override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            sink.writeAll(inputStream.source())
        }
    }
}