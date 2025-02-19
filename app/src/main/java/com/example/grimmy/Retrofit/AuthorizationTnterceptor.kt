package com.example.grimmy.Retrofit

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(context: Context) : Interceptor {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val jwtToken = sharedPref.getString("jwtToken", null)
        val requestBuilder = chain.request().newBuilder()

        if (jwtToken.isNullOrEmpty()) {
            Log.e("AuthorizationInterceptor", "❌ JWT 토큰이 없습니다! Authorization 헤더 추가 안됨")
        } else {
            val tokenHeader = "Bearer $jwtToken"
            Log.i("AuthorizationInterceptor", "✅ JWT 토큰 추가됨: $tokenHeader")
            requestBuilder.addHeader("Authorization", tokenHeader)
        }

        val request = requestBuilder.build()

        // 🔥 요청 로그에 Authorization 헤더가 출력되는지 확인
        for (headerName in request.headers.names()) {
            Log.i("AuthorizationInterceptor", "🔍 요청 헤더: $headerName: ${request.headers[headerName]}")
        }

        return chain.proceed(request)
    }
}
