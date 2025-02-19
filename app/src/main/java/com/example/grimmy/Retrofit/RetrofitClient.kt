package com.example.grimmy.Retrofit

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {

    companion object {
        private const val BASE_URL = "http://13.124.52.202:8080/"
        private var appContext: Context? = null

        // ✅ RetrofitClient 초기화 (Application 시작 시 한 번만 호출)
        fun init(context: Context) {
            appContext = context.applicationContext
            Log.i("RetrofitClient", "✅ RetrofitClient 초기화 완료")
        }

        // ✅ AuthorizationInterceptor 추가
        private val authorizationInterceptor: AuthorizationInterceptor by lazy {
            checkNotNull(appContext) { "RetrofitClient.init(context)를 먼저 호출해야 합니다!" }
            AuthorizationInterceptor(appContext!!)
        }

        // ✅ HTTP 로그 인터셉터 추가
        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // ✅ OkHttpClient 설정 (JWT 자동 추가)
        private val okHttpClient: OkHttpClient by lazy {
            checkNotNull(appContext) { "RetrofitClient.init(context)를 먼저 호출해야 합니다!" }

            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor) // 🔥 로그 인터셉터 추가
                .addInterceptor(authorizationInterceptor) // 🔥 JWT 토큰 자동 추가
//                .connectTimeout(20, TimeUnit.SECONDS)
//                .readTimeout(20, TimeUnit.SECONDS)
                .build()
        }

        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd") // ✅ 자동 변환 설정
            .create()

        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient) // ✅ JWT 인터셉터가 포함된 클라이언트 사용
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        val service: RetrofitService by lazy {
            retrofit.create(RetrofitService::class.java)
        }
    }
}
