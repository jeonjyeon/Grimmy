package com.example.grimmy

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.grimmy.alarm.AlarmScheduler
import com.example.grimmy.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment(), TimePickerDialogFragment.OnTimeSetListener, AlarmDatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentAlarmBinding
    private var activeTextViewId: Int = 0 // 현재 활성화된 TextView ID
    private var isPaintingReminderOn = false // 그림 기록 리마인더 상태 저장
    private var isMaterialReminderOn = false // 재료 리마인더 상태 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)

        // ✅ SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()

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

        // ✅ 저장된 설정 불러오기
        loadReminderSettings()

        binding.alarmPaintingReminderIv.setOnClickListener {
            isPaintingReminderOn = !isPaintingReminderOn // 상태 변경
            updatePaintingReminderUI()
            saveReminderSettings() // ✅ 설정 저장

            if (isPaintingReminderOn) {
                val timeText = binding.alarmPaintingReminderTimeNextTv.text.toString()
                val (hour, minute) = parseTime(timeText)

                AlarmScheduler.scheduleAlarm(
                    requireContext(),
                    hour,
                    minute,
                    "그림 기록 리마인더",
                    "오늘의 그림을 기록하셨나요? ${getNickname()} 님의 그림을 기록해보세요!"
                )
            }
        }

        binding.alarmMaterialReminderIv.setOnClickListener {
            isMaterialReminderOn = !isMaterialReminderOn // 상태 변경
            updateMaterialReminderUI()
            saveReminderSettings() // ✅ 설정 저장

            if (isMaterialReminderOn) {
                val timeText = binding.alarmMaterialReminderTimeNextTv.text.toString()
                val (hour, minute) = parseTime(timeText)

                AlarmScheduler.scheduleAlarm(
                    requireContext(),
                    hour,
                    minute,
                    "재료 리마인더",
                    "오늘 필요한 재료는 챙기셨나요? 재료 준비를 잊지마세요!"
                )
            }
        }

        binding.alarmPaintingReminderTimeBtnCl.setOnClickListener {
            val currentTime = binding.alarmPaintingReminderTimeNextTv.text.toString()
            val (currentHour, currentMinute) = parseTime(currentTime)

            activeTextViewId = binding.alarmPaintingReminderTimeNextTv.id
            showTimePickerDialog(currentHour, currentMinute)
        }

        binding.alarmMaterialReminderDayNextTv.setOnClickListener {
            val pickerFragment = AlarmDatePickerDialogFragment().apply {
                listener = this@AlarmFragment
            }
            pickerFragment.show(parentFragmentManager, "alarmDatePicker")
        }

        binding.alarmMaterialReminderTimeBtnCl.setOnClickListener {
            val currentTime = binding.alarmMaterialReminderTimeNextTv.text.toString()
            val (currentHour, currentMinute) = parseTime(currentTime)

            activeTextViewId = binding.alarmMaterialReminderTimeNextTv.id
            showTimePickerDialog(currentHour, currentMinute)
        }

        return binding.root
    }
    private fun saveReminderSettings() {
        val sharedPref = requireContext().getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            // ✅ 토글 상태 저장
            putBoolean("paintingReminder", isPaintingReminderOn)
            putBoolean("materialReminder", isMaterialReminderOn)

            // ✅ 그림 기록 리마인더 시간 저장
            putString("paintingReminderTime", binding.alarmPaintingReminderTimeNextTv.text.toString())

            // ✅ 재료 리마인더 날짜 & 시간 저장
            putString("materialReminderDate", binding.alarmMaterialReminderDayNextTv.text.toString())
            putString("materialReminderTime", binding.alarmMaterialReminderTimeNextTv.text.toString())

            apply()
        }
    }


    private fun loadReminderSettings() {
        val sharedPref = requireContext().getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)

        // ✅ 저장된 토글 상태 불러오기
        isPaintingReminderOn = sharedPref.getBoolean("paintingReminder", false)
        isMaterialReminderOn = sharedPref.getBoolean("materialReminder", false)

        // ✅ 저장된 시간 & 날짜 불러오기
        binding.alarmPaintingReminderTimeNextTv.text = sharedPref.getString("paintingReminderTime", "시간 설정")
        binding.alarmMaterialReminderDayNextTv.text = sharedPref.getString("materialReminderDate", "날짜 설정")
        binding.alarmMaterialReminderTimeNextTv.text = sharedPref.getString("materialReminderTime", "시간 설정")

        // ✅ UI 업데이트 (토글 상태 반영)
        updatePaintingReminderUI()
        updateMaterialReminderUI()
    }


    // ✅ UI 업데이트 (토글 이미지 변경)
    private fun updatePaintingReminderUI() {
        if (isPaintingReminderOn) {
            binding.alarmPaintingReminderIv.setImageResource(R.drawable.btn_toggle_right) // ON 이미지
        } else {
            binding.alarmPaintingReminderIv.setImageResource(R.drawable.btn_toggle_off) // OFF 이미지
        }
    }

    private fun updateMaterialReminderUI() {
        if (isMaterialReminderOn) {
            binding.alarmMaterialReminderIv.setImageResource(R.drawable.btn_toggle_right) // ON 이미지
        } else {
            binding.alarmMaterialReminderIv.setImageResource(R.drawable.btn_toggle_off) // OFF 이미지
        }
    }

    // 날짜 선택 리스너 구현
    override fun onDateSelected(year: Int, month: Int, day: Int) {
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

    // ✅ 저장된 닉네임 가져오기
    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }
}
