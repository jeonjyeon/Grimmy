package com.example.grimmy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // SplashScreen을 설치. 이 호출은 super.onCreate() 이전에 이루어져야 함.
        val splashScreen = installSplashScreen()

        // warm boot 때 default splash 화면을 최소한으로 나타나도록 설정
        splashScreen.setKeepOnScreenCondition {false}
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 2초 후에 LoginActivity로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()  // SplashActivity 종료
        }, 2000)
    }
}