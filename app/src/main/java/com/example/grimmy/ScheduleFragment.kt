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
import com.example.grimmy.Retrofit.Response.GetScheduleResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentScheduleBinding
import com.example.grimmy.viewmodel.ScheduleViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        fetchScheduleData() // onResume 시 최신 시간표 데이터를 다시 불러옵니다.
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBinding.inflate(inflater, container, false)

        // 📌 SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.scheduleProfileUsernameTv.text = nickname ?: "사용자"

        // 📌 새로운 프래그먼트를 schedule_frame에 추가
        binding.scheduleNotifyBtnIv.setOnClickListener {
            openFragment(AlarmFragment())
        }

        binding.scheduleAddClassIv.setOnClickListener {
            openFragment(ScheduleAddClassFragment())
        }

        binding.scheduleTimetableListIv.setOnClickListener {
            openFragment(ScheduleListFragment())
        }

        // 📌 서버에서 시간표 데이터를 불러와 ViewModel에 저장
        fetchScheduleData()

        // 📌 ViewModel의 수업 데이터를 관찰하여 동적으로 시간표 업데이트
        scheduleViewModel.classSchedules.observe(viewLifecycleOwner) { classSchedules ->
            if (classSchedules != null) {
                binding.dynamicScheduleView.updateSchedule(classSchedules)

                // 디버깅: 마지막 수업 정보 로깅
                if (classSchedules.isNotEmpty()) {
                    val lastClass = classSchedules.last()
                    Log.d("ClassSchedule", "Class Name: ${lastClass.title}")
                    Log.d("ClassSchedule", "Class Place: ${lastClass.location}")
                    Log.d("ClassSchedule", "Day: ${lastClass.day}")
                    Log.d("ClassSchedule", "Start Time: ${lastClass.startTime}")
                    Log.d("ClassSchedule", "End Time: ${lastClass.endTime}")
                }
            }
        }

        return binding.root
    }


    // 📌 서버에서 시간표 데이터 가져오기
    private fun fetchScheduleData() {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(context, "유효한 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val year = 2025 // ✅ 연도는 필요에 따라 동적으로 변경 가능

        RetrofitClient.service.getSchedule(userId, year).enqueue(object : Callback<GetScheduleResponse> {
            override fun onResponse(
                call: Call<GetScheduleResponse>,
                response: Response<GetScheduleResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val scheduleDetails = response.body()?.details ?: emptyList()

                    // ✅ ViewModel에 데이터 업데이트
                    scheduleViewModel.setClassSchedules(scheduleDetails)

                    Log.d("ScheduleAPI", "시간표 데이터 가져오기 성공: $scheduleDetails")
                } else {
                    Log.e("ScheduleAPI", "시간표 불러오기 실패: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "시간표 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
                Log.e("ScheduleAPI", "시간표 조회 API 실패", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 📌 Fragment 전환 함수 (새 프래그먼트가 최상단에 뜨도록 함)
    private fun openFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.schedule_frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }
}
