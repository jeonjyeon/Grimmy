package com.example.grimmy

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.grimmy.Retrofit.Request.KakaoAccessTokenRequest
import com.example.grimmy.Retrofit.Response.KakaoAccessTokenResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.ActivityLoginBinding
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
//            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity)) {
//                // 카카오톡 앱을 통한 로그인
//                UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity) { token, error ->
//                    if (error != null) {
//                        Log.e(TAG, "카카오톡 로그인 실패: ${error.message}")
//                        // 필요 시 fallback: 카카오 계정으로 로그인 시도
//                        loginWithKakaoAccount()
//                    } else if (token != null) {
//                        handleLoginSuccess(token.accessToken)
//                    }
//                }
//            } else {
//                // 카카오톡 앱이 설치되어 있지 않으면 카카오 계정으로 로그인 진행
//                loginWithKakaoAccount()
//            }
            // 카카오 계정 로그인만 호출
            loginWithKakaoAccount()
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

        // ✅ accessToken을 SharedPreferences에 저장
        saveAccessToken(accessToken)

        // ✅ 서버로 액세스 토큰 전송
        sendTokenToServer(accessToken)

        // ✅ 로그인 성공 후 OnboardingActivity로 이동
        moveToOnboardingActivity()

//        requestKakaoUserInfo()
    }

    private fun sendTokenToServer(token: String) {
        val request = KakaoAccessTokenRequest(token)
        RetrofitClient.service.sendAccessToken(request).enqueue(object : Callback<KakaoAccessTokenResponse> {
            override fun onResponse(call: Call<KakaoAccessTokenResponse>, response: Response<KakaoAccessTokenResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.i("Auth", "✅ 로그인 성공! JWT Token: ${responseBody.accessToken}, UserID: ${responseBody.userId}")

                        // ✅ 여기서 accessToken과 userId를 SharedPreferences 등에 저장할 수도 있음
                        saveJwtToken(responseBody.accessToken) // ✅ JWT 토큰 저장
                        saveUserId(responseBody.userId) // ✅ User ID 저장
                        Log.i("Auth", "📌 서버에서 받은 userId 값: ${responseBody.userId}")
                    } else {
                        Log.e("Auth", "❌ 응답이 null입니다.")
                    }
                } else {
                    Log.e("Auth", "❌ 로그인 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<KakaoAccessTokenResponse>, t: Throwable) {
                Log.e("Auth", "❌ 네트워크 오류: ${t.message}")
            }
        })
    }

    // SharedPreferences에 accessToken을 저장하는 함수
    private fun saveAccessToken(token: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("accessToken", token).apply()
        Log.i(TAG, "accessToken 저장 완료: $token")
    }

    private fun saveJwtToken(jwt: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("jwtToken", jwt).apply() // ✅ commit() 사용
        Log.i(TAG, "jwtToken 저장 완료: $jwt")
    }

    private fun saveUserId(userId: Int) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt("userId", userId).apply()
        Log.i("User ID 저장", "userId 저장 완료: $userId")
    }

//    private fun saveRefreshToken(refresh: String) {
//        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//        sharedPref.edit().putString("refreshToken", refresh).apply()
//        Log.i(TAG, "refreshToken 저장 완료: $refresh")
//    }


//    private fun requestKakaoUserInfo() {
//        UserApiClient.instance.me { user, error ->
//            if (error != null) {
//                // 사용자 정보 요청 실패 처리
//                Log.e(TAG, "사용자 정보 요청 실패: ${error.message}")
//            } else if (user != null) {
//                val userId = user.id
//                val nickname = user.kakaoAccount?.profile?.nickname
//                val isEmailVerified = user.kakaoAccount?.isEmailVerified ?: false
//                val email = user.kakaoAccount?.email
//
//                Log.i(TAG, "✅ 사용자 정보 가져오기 성공")
//                Log.i(TAG, "User Id: $userId")
//                Log.i(TAG, "Nickname: $nickname")
//                Log.i(TAG, "Email: $email")
//                Log.i(TAG, "✅ 이메일 인증 여부: $isEmailVerified")
//
//
//                // 이메일 미인증 시 동의창 띄우기
//                if (!isEmailVerified) {
//                    UserApiClient.instance.loginWithNewScopes(
//                        this,
//                        listOf("account_email")
//                    ) { oAuthResponse, consentError ->
//                        if (consentError != null) {
//                            // 동의 실패 처리
//                            Log.e(TAG, "동의 실패: ${consentError.message}")
//                        } else {
//                            // 동의 성공 처리
//                            Log.i(TAG, "동의 성공")
//                            // 동의창 띄운 후 추가 작업 수행
//                        }
//                    }
//                } else {
//                    // 이미 이메일 인증된 사용자의 처리
//                    Log.i(TAG, "이미 이메일 인증된 사용자")
//
//                    // 추가 작업 수행
//                }
//            }
//        }
//    }

    private fun moveToOnboardingActivity() {
        Log.i(TAG, "🔄 OnboardingActivity로 이동 중...")
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }
}
