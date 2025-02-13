package com.example.grimmy

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.grimmy.BuildConfig.KAKAO_REST_API_KEY
import com.example.grimmy.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    companion object {
        // 실제 발급받은 앱 키와 리다이렉트 URI로 교체하세요.
        private const val KAKAO_REDIRECT_URI = "http://13.124.52.202:8080/login/oauth2/code/kakao"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 시스템 바 영역에 맞게 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginKakaoLoginBtnLl.setOnClickListener {
            // 인가코드 요청 URL 생성
            val authUrl = Uri.parse("https://kauth.kakao.com/oauth/authorize").buildUpon()
                .appendQueryParameter("client_id", BuildConfig.KAKAO_REST_API_KEY)
                .appendQueryParameter("redirect_uri", KAKAO_REDIRECT_URI)
                .appendQueryParameter("response_type", "code")
                .build()
            Log.i(TAG, "카카오 인가코드 요청 URL: $authUrl")

            // 외부 브라우저를 통해 카카오 로그인 페이지 열기
            val intent = Intent(Intent.ACTION_VIEW, authUrl)
            startActivity(intent)
        }
    }

    // 앱이 리다이렉트 URI로 재실행될 때 호출 (AndroidManifest.xml에 인텐트 필터 설정 필요)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            // 리다이렉트 URI에서 인가코드 추출
            val authCode = uri.getQueryParameter("code")
            if (authCode != null) {
                Log.i(TAG, "카카오 인가코드 받음: $authCode")
                handleLoginSuccess(authCode)
            } else {
                Log.e(TAG, "인가코드가 존재하지 않습니다.")
            }
        }
    }


    private fun handleLoginSuccess(authCode: String) {
        Log.i(TAG, "카카오 인가코드 성공")
        Log.i(TAG, "인가코드: $authCode")
        // TODO: 받은 인가코드를 서버로 전송하여 access token으로 교환하는 로직 구현

        // 로그인 성공 후 OnboardingActivity로 이동
        moveToOnboardingActivity()
    }

    private fun moveToOnboardingActivity() {
        Log.i(TAG, "OnboardingActivity로 이동 중...")
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }
}
