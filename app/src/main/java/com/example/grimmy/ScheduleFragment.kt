package com.example.grimmy

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.example.grimmy.databinding.FragmentScheduleBinding
import com.example.grimmy.viewmodel.ScheduleViewModel

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용하여 binding 객체 초기화
        binding = FragmentScheduleBinding.inflate(inflater, container, false)

        binding.scheduleAddClassIv.setOnClickListener(){
            // ScheduleAddClassFragment로 전환
            val scheduleAddClassFragment = ScheduleAddClassFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.schedule_add_class_frame, scheduleAddClassFragment) // fragment_container는 프래그먼트를 표시할 컨테이너의 ID입니다.
                .addToBackStack(null) // 뒤로 가기 스택에 추가
                .commit()
        }

        binding.scheduleTimetableListIv.setOnClickListener(){
            // ScheduleListFragment로 전환
            val scheduleListFragment = ScheduleListFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.schedule_add_class_frame, scheduleListFragment) // fragment_container는 프래그먼트를 표시할 컨테이너의 ID입니다.
                .addToBackStack(null) // 뒤로 가기 스택에 추가
                .commit()
        }

        scheduleViewModel.classSchedules.observe(viewLifecycleOwner) { classSchedule ->
            classSchedule?.let {
                if (it.isNotEmpty()) {
                    val lastClassSchedule = it.last() // 마지막 요소 가져오기
                    Log.d("ClassSchedule", "Class Name: ${lastClassSchedule.className}")
                    Log.d("ClassSchedule", "Class Place: ${lastClassSchedule.classPlace}")
                    Log.d("ClassSchedule", "Day: ${lastClassSchedule.day}")
                    Log.d("ClassSchedule", "Start Time: ${lastClassSchedule.startTime}")
                    Log.d("ClassSchedule", "End Time: ${lastClassSchedule.endTime}")
                }
            }
        }

        return binding.root
    }
}
