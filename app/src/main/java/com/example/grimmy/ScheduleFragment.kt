package com.example.grimmy

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.grimmy.databinding.FragmentScheduleBinding
import com.example.grimmy.viewmodel.ScheduleViewModel

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBinding.inflate(inflater, container, false)

        // 📌 SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.scheduleProfileUsernameTv.text = nickname ?: "사용자"

        // 📌 새로운 프래그먼트를 schedule_frame에 추가 (최상단 유지)
        binding.scheduleNotifyBtnIv.setOnClickListener {
            openFragment(AlarmFragment())
        }

        binding.scheduleAddClassIv.setOnClickListener {
            openFragment(ScheduleAddClassFragment())
        }

        binding.scheduleTimetableListIv.setOnClickListener {
            openFragment(ScheduleListFragment())
        }

        // 📌 XML에 이미 존재하는 DynamicScheduleView를 사용
        val dynamicScheduleView = binding.dynamicScheduleView

        // 📌 ViewModel의 수업 데이터를 관찰하여 동적으로 시간표 업데이트
        scheduleViewModel.classSchedules.observe(viewLifecycleOwner) { classSchedules ->
            val scheduleItems = classSchedules?.map { cs ->
                ScheduleItem(
                    dayOfWeek = parseDayToIndex(cs.day),
                    startTimeMin = parseTimeToMinutes(cs.startTime),
                    endTimeMin = parseTimeToMinutes(cs.endTime),
                    courseName = cs.className,
                    colorResId = android.R.color.holo_red_dark // 필요시 색상 변경 가능
                )
            } ?: emptyList()

            scheduleViewModel.classSchedules.observe(viewLifecycleOwner) { classSchedules ->
                if (classSchedules != null) {
                    binding.dynamicScheduleView.updateSchedule(classSchedules) // ✅ `List<ClassSchedule>`을 전달해야 함
                }
            }

            if (classSchedules?.isNotEmpty() == true) {
                // 디버깅: 마지막 수업 정보 로깅
                val lastClass = classSchedules.last()
                Log.d("ClassSchedule", "Class Name: ${lastClass.className}")
                Log.d("ClassSchedule", "Class Place: ${lastClass.classPlace}")
                Log.d("ClassSchedule", "Day: ${lastClass.day}")
                Log.d("ClassSchedule", "Start Time: ${lastClass.startTime}")
                Log.d("ClassSchedule", "End Time: ${lastClass.endTime}")
            }
        }

        // 📌 수업 데이터가 없더라도 기본 틀이 항상 보이도록 빈 리스트 업데이트
        if (scheduleViewModel.classSchedules.value.isNullOrEmpty()) {
            dynamicScheduleView.updateSchedule(emptyList())
        }

        return binding.root
    }

    // 📌 Fragment 전환 함수 (새 프래그먼트가 최상단에 뜨도록 함)
    private fun openFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.schedule_frame, fragment) // ✅ 항상 최상단에 추가됨
            .addToBackStack(null)
            .commit()
    }

    // "HH:mm" 형식을 분 단위(Int)로 변환하는 헬퍼 함수
    private fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size != 2) return 0
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0
        return hour * 60 + minute
    }

    // 요일 문자열("월", "화", "수", "목", "금")을 0~4 인덱스로 변환하는 헬퍼 함수
    private fun parseDayToIndex(dayStr: String): Int {
        return when(dayStr.trim()) {
            "월" -> 0
            "화" -> 1
            "수" -> 2
            "목" -> 3
            "금" -> 4
            else -> 0
        }
    }

    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }
}
