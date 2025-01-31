package com.example.grimmy

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.example.grimmy.databinding.FragmentScheduleAddClassBinding

class ScheduleAddClassFragment : Fragment() {
    private lateinit var binding: FragmentScheduleAddClassBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleAddClassBinding.inflate(inflater,container,false)

        // "완료" 버튼 클릭 시 동작
        binding.scheduleClassAddOkTv.setOnClickListener {
            // 사용자가 입력한 데이터를 가져오고 끝의 공백 제거
            val className = binding.scheduleAddClassNameEt.text.toString().trim()
            val classPlace = binding.scheduleAddClassPlaceEt.text.toString().trim()

            // 데이터 처리 로직 추가 (예: 데이터베이스에 저장)
            // 예: saveData(className, classPlace, classTime)
            saveData(className, classPlace)

            // 이전 프래그먼트로 돌아가면서 네비게이션 바 보이기
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }
    private fun saveData(className: String, classPlace: String) {
        // 예시: 입력된 데이터를 로그로 출력
        // 실제 데이터베이스 저장 또는 서버 전송 로직을 여기에 추가
        Log.d("ScheduleAddClassFragment", "Class Name: $className")
        Log.d("ScheduleAddClassFragment", "Class Place: $classPlace")

        // 예를 들어 Room 데이터베이스에 저장하는 로직 추가
        // val classEntity = ClassEntity(name = className, place = classPlace, time = classTime)
        // database.classDao().insert(classEntity)
    }

    override fun onResume() {
        super.onResume()
        // 프래그먼트가 활성화되면 네비게이션 바 숨기기
        (activity as MainActivity).hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        // 프래그먼트가 비활성화되면 네비게이션 바 보이기
        (activity as MainActivity).showBottomNav()
    }
}