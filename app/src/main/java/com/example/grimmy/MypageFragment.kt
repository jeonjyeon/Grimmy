package com.example.grimmy

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.grimmy.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private lateinit var binding : FragmentMypageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMypageBinding.inflate(inflater,container,false)

        // ✅ SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.mypageProfileNameTv.text = nickname ?: "사용자"

        return binding.root
    }
    // ✅ 저장된 닉네임 가져오기
    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }
}