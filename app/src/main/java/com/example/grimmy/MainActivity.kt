package com.example.grimmy

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import com.example.grimmy.databinding.ActivityMainBinding
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity(), OnPageUpListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavView()

        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = com.example.grimmy.R.id.fragment_home
        }

//        val keyHash = getKeyHash(this, packageName)
//        Log.d("KeyHash", keyHash!!)
    }

    override fun onPageUpClicked() {
        // fragment_home의 인스턴스를 찾은 후 scrollToTop() 메서드를 호출합니다.
        // 예를 들어, fragment_home은 R.id.home_fragment_container에 배치되었다고 가정합니다.
        val homeFragment = supportFragmentManager.findFragmentById(R.id.main_frame) as? HomeFragment
        homeFragment?.scrollToTop()
    }

//    fun getKeyHash(context: Context, packageName: String): String? {
//        val packageInfo = try {
//            context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//        } catch (e: PackageManager.NameNotFoundException) {
//            return null
//        }
//
//        for (signature in packageInfo.signatures) {
//            try {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
//            } catch (e: NoSuchAlgorithmException) {
//                Log.e("KeyHash", "Unable to get MessageDigest. signature=$signature", e)
//            }
//        }
//        return null
//    }

    private fun setBottomNavView() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_frame,
                        HomeFragment()
                    ).commit()
                    true
                }
                R.id.fragment_goal -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_frame,
                        GoalFragment()
                    ).commit()
                    true
                }
                R.id.fragment_report -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_frame,
                        ReportFragment()
                    ).commit()
                    true
                }
                R.id.fragment_schedule -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_frame,
                        ScheduleFragment()
                    ).commit()
                    true
                }
                R.id.fragment_mypage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_frame,
                        MypageFragment()
                    ).commit()
                    true
                }

                else -> false
            }
        }
    }

    // 바텀 네비게이션 바 숨기기
    fun hideBottomNav() {
        binding.bottomNav.visibility = View.GONE
    }

    // 바텀 네비게이션 바 보이기
    fun showBottomNav() {
        binding.bottomNav.visibility = View.VISIBLE
    }
}