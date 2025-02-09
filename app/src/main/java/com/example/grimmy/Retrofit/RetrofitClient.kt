package com.example.grimmy.Retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
            .addInterceptor(loggingInterceptor)
            .build()



        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(this.BASE_URL)
            .client(okHttpClient) // 추가
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: RetrofitService = retrofit.create(RetrofitService::class.java)


    }
}