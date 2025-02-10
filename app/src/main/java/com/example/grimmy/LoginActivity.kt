package com.example.grimmy

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.grimmy.databinding.ActivityLoginBinding
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding 객체 초기화 및 binding.root를 화면에 설정
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // binding을 사용하여 id가 main인 View에 WindowInsets 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginKakaoLoginBtnLl.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity)) {
                // 카카오톡 앱을 통한 로그인
                UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡 로그인 실패: ${error.message}")
                        // 필요 시 fallback: 카카오 계정으로 로그인 시도
                        loginWithKakaoAccount()
                    } else if (token != null) {
                        handleLoginSuccess(token.accessToken)
                    }
                }
            } else {
                // 카카오톡 앱이 설치되어 있지 않으면 카카오 계정으로 로그인 진행
                loginWithKakaoAccount()
            }
        }
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this@LoginActivity) { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오 계정 로그인 실패: ${error.message}")
            } else if (token != null) {
                handleLoginSuccess(token.accessToken)
            }
        }
    }

    private fun handleLoginSuccess(accessToken: String) {
        Log.i(TAG, "카카오 로그인 성공")
        Log.i(TAG, accessToken)
        requestKakaoUserInfo()
    }

    private fun requestKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                // 사용자 정보 요청 실패 처리
                Log.e(TAG, "사용자 정보 요청 실패: ${error.message}")
            } else if (user != null) {
                val userId = user.id
                val nickname = user.kakaoAccount?.profile?.nickname
                val isEmailVerified = user.kakaoAccount?.isEmailVerified ?: false

                // 이메일 미인증 시 동의창 띄우기
                if (!isEmailVerified) {
                    UserApiClient.instance.loginWithNewScopes(
                        this,
                        listOf("account_email")
                    ) { oAuthResponse, consentError ->
                        if (consentError != null) {
                            // 동의 실패 처리
                            Log.e(TAG, "동의 실패: ${consentError.message}")
                        } else {
                            // 동의 성공 처리
                            Log.i(TAG, "동의 성공")
                            // 동의창 띄운 후 추가 작업 수행
                        }
                    }
                } else {
                    // 이미 이메일 인증된 사용자의 처리
                    Log.i(TAG, "이미 이메일 인증된 사용자")
                    // 추가 작업 수행
                }
            }
        }
    }
}
