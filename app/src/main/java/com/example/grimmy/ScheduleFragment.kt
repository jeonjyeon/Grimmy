package com.example.grimmy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.example.grimmy.databinding.FragmentReportBinding
import com.example.grimmy.databinding.FragmentScheduleBinding

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding

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

        return binding.root
    }
}
