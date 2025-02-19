package com.example.grimmy

import android.app.Application
import com.example.grimmy.Retrofit.RetrofitClient
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들
        RetrofitClient.init(this) // ✅ 앱 실행 시 RetrofitClient 초기화

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}