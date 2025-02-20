package com.example.grimmy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // SplashScreen을 설치. 이 호출은 super.onCreate() 이전에 이루어져야 함.
        val splashScreen = installSplashScreen()

        // warm boot 때 default splash 화면을 최소한으로 나타나도록 설정
        splashScreen.setKeepOnScreenCondition {false}
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 2초 후에 SharedPreferences에 저장된 accessToken을 확인하여 다음 화면으로 전환
        Handler(Looper.getMainLooper()).postDelayed({
//            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//            val accessToken = sharedPref.getString("accessToken", null)
//            Log.d("TokenCheck", "Stored accessToken: $accessToken")
//
//            // accessToken이 있으면 이미 가입된 사용자 -> MainActivity로, 없으면 LoginActivity로 이동
//            val nextIntent = if (accessToken != null) {
//                Intent(this, MainActivity::class.java)
//            } else {
//                Intent(this, LoginActivity::class.java)
//            }
//            startActivity(nextIntent)

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // SplashActivity 종료
        }, 2000)


    }
}