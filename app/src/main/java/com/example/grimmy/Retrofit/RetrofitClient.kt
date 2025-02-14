package com.example.grimmy.Retrofit

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


        //추가
        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        //추가
        private val okHttpClient = OkHttpClient.Builder()
            //.addInterceptor(ContentTypeInterceptor())
            // .connectTimeout(20, TimeUnit.SECONDS)  // 연결 타임아웃 20초
            // .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd") // ✅ 자동 변환 설정
            .create()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(this.BASE_URL)
            .client(okHttpClient) // 추가
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service: RetrofitService = retrofit.create(RetrofitService::class.java)


    }
}