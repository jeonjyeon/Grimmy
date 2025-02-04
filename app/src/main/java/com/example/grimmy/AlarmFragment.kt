package com.example.grimmy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.grimmy.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment(), TimePickerDialogFragment.OnTimeSetListener, AlarmDatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentAlarmBinding
    private var activeTextViewId: Int = 0 // 현재 활성화된 TextView ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)

        binding.alarmPaintingReminderTimeNextTv.text = "시간 설정"
        binding.alarmMaterialReminderDayNextTv.text = "날짜 설정"
        binding.alarmMaterialReminderTimeNextTv.text = "시간 설정"

        binding.alarmPaintingReminderTimeBtnCl.setOnClickListener {
            val currentTime = binding.alarmPaintingReminderTimeNextTv.text.toString()
            val (currentHour, currentMinute) = parseTime(currentTime)

            activeTextViewId = binding.alarmPaintingReminderTimeNextTv.id // 활성화된 TextView ID 설정
            showTimePickerDialog(currentHour, currentMinute)
        }

        // 날짜 선택 버튼 클릭 리스너 추가
        binding.alarmMaterialReminderDayNextTv.setOnClickListener {
            val pickerFragment = AlarmDatePickerDialogFragment().apply {
                listener = this@AlarmFragment
            }
            pickerFragment.show(parentFragmentManager, "alarmDatePicker")
        }

        binding.alarmMaterialReminderTimeBtnCl.setOnClickListener {
            val currentTime = binding.alarmMaterialReminderTimeNextTv.text.toString()
            val (currentHour, currentMinute) = parseTime(currentTime)

            activeTextViewId = binding.alarmMaterialReminderTimeNextTv.id // 활성화된 TextView ID 설정
            showTimePickerDialog(currentHour, currentMinute)
        }

        binding.alarmBackIv.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }

    // 날짜 선택 리스너 구현
    override fun onDateSelected(year: Int, month: Int, day: Int) {
//        val selectedDate = "${year}년 ${month}월 ${day}일"
        val selectedDate = "${month}월 ${day}일"
        binding.alarmMaterialReminderDayNextTv.text = selectedDate // 선택된 날짜를 표시할 TextView
    }

    private fun showTimePickerDialog(currentHour: Int, currentMinute: Int) {
        val timePickerDialog = TimePickerDialogFragment()
        timePickerDialog.setOnTimeSetListener(this)

        val args = Bundle().apply {
            putInt("initialHour", currentHour)
            putInt("initialMinute", currentMinute)
        }
        timePickerDialog.arguments = args
        timePickerDialog.show(childFragmentManager, "timePicker")
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        if (timeString.isNotEmpty()) {
            val parts = timeString.split(" ")
            // 문자열이 두 부분으로 나뉘어져 있는지 확인
            if (parts.size == 2) {
                val period = parts[0] // "오전" 또는 "오후"
                val timeParts = parts[1].split(":")
                if (timeParts.size == 2) {
                    val hour = timeParts[0].toIntOrNull() ?: 0
                    val minute = timeParts[1].toIntOrNull() ?: 0

                    val adjustedHour = when (period) {
                        "오후" -> if (hour < 12) hour + 12 else hour
                        "오전" -> if (hour == 12) 0 else hour
                        else -> hour
                    }
                    return Pair(adjustedHour, minute)
                }
            }
        }
        return Pair(0, 0) // 기본값
    }

    override fun onTimeSet(hour: Int, minute: Int) {
        val period = if (hour < 12) "오전" else "오후"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val timeString = String.format("%s %02d:%02d", period, displayHour, minute)

        // 선택된 시간에 따라 적절한 TextView에 시간 설정
        val resultTextView = requireActivity().findViewById<TextView>(activeTextViewId)
        resultTextView.text = timeString
    }
}
