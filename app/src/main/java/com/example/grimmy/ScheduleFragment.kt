package com.example.grimmy

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.grimmy.Retrofit.Request.ClassAddRequest
import com.example.grimmy.Retrofit.Response.ClassAddResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentScheduleBinding
import com.example.grimmy.viewmodel.ScheduleViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            if (classSchedules != null) {
                binding.dynamicScheduleView.updateSchedule(classSchedules)

                // 디버깅: 마지막 수업 정보 로깅
                if (classSchedules.isNotEmpty()) {
                    val lastClass = classSchedules.last()
                    Log.d("ClassSchedule", "Class Name: ${lastClass.className}")
                    Log.d("ClassSchedule", "Class Place: ${lastClass.classPlace}")
                    Log.d("ClassSchedule", "Day: ${lastClass.day}")
                    Log.d("ClassSchedule", "Start Time: ${lastClass.startTime}")
                    Log.d("ClassSchedule", "End Time: ${lastClass.endTime}")
                }
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

    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }
}
