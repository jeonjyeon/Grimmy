package com.example.grimmy

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.grimmy.databinding.DialogAlertCustomBinding
import com.example.grimmy.databinding.FragmentScheduleAddClassBinding
import com.example.grimmy.utils.parseDayToIndex
import com.example.grimmy.utils.parseTimeToMinutes
import com.example.grimmy.viewmodel.ScheduleViewModel

class ScheduleAddClassFragment : Fragment(), StartTimePickerDialogFragment.OnTimeSetListener,
    EndTimePickerDialogFragment.OnTimeSetListener, DayPickerDialogFragment.OnDaySetListener {
    private lateinit var binding: FragmentScheduleAddClassBinding
    private lateinit var scheduleViewModel: ScheduleViewModel

    val startTimePickerDialog = StartTimePickerDialogFragment()
    val endTimePickerDialog = EndTimePickerDialogFragment()
    val dayPickerDialog = DayPickerDialogFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleAddClassBinding.inflate(inflater, container, false)
        scheduleViewModel = ViewModelProvider(requireActivity()).get(ScheduleViewModel::class.java)

        binding.scheduleClassAddOkTv.setOnClickListener {
//            val className = binding.scheduleAddClassNameEt.text.toString().trim()
//            val classPlace = binding.scheduleAddClassPlaceEt.text.toString().trim()
//            saveData(className, classPlace)
            val className = binding.scheduleAddClassNameEt.text.toString().trim()
            val classPlace = binding.scheduleAddClassPlaceEt.text.toString().trim()
            val classDay = binding.scheduleAddClassDaypickerBtnTv.text.toString()
            val startTime = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val endTime = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()

            if (className.isEmpty() || classPlace.isEmpty()) {
                showAlert("수업 명과 장소를 입력해 주세요.")
            } else {
                // 수업 등록 로직
                val newClass = ClassSchedule(className, classPlace, classDay, startTime, endTime)
                // 📌 기존 수업과 겹치는지 확인
                if (isOverlapping(newClass)) {
                    showAlert("시간표가 겹쳐 추가할 수 없습니다.")
                } else {
                    Log.d("ScheduleAddClassFragment", "Class: ${scheduleViewModel.classSchedules.value}")
                    Log.d("ScheduleAddClassFragment", "Class: ${scheduleViewModel.classSchedules.value}")
                    scheduleViewModel.addClass(newClass)
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }

        binding.scheduleAddClassDayLl.setOnClickListener {
            val currentDay = binding.scheduleAddClassDaypickerBtnTv.text.toString()
            dayPickerDialog.setInitialDay(currentDay)
            dayPickerDialog.setOnDaySetListener(this)
            dayPickerDialog.show(parentFragmentManager, "dayPicker")
        }

        binding.scheduleAddClassStartTimeLl.setOnClickListener {
            val currentTime = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val parts = currentTime.split(":")
            val currentHour = parts[0].toInt()
            val currentMinute = parts[1].toInt()

            val args = Bundle().apply {
                putInt("initialHour", currentHour)
                putInt("initialMinute", currentMinute)
            }
            startTimePickerDialog.arguments = args
            startTimePickerDialog.setOnTimeSetListener(this)
            startTimePickerDialog.show(parentFragmentManager, "startTimePicker")
        }

        binding.scheduleAddClassEndTimeLl.setOnClickListener {
            val currentTime = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()
            val parts = currentTime.split(":")
            val currentHour = parts[0].toInt()
            val currentMinute = parts[1].toInt()

            val args = Bundle().apply {
                putInt("initialHour", currentHour)
                putInt("initialMinute", currentMinute)
            }
            endTimePickerDialog.arguments = args
            endTimePickerDialog.setOnTimeSetListener(this)
            endTimePickerDialog.show(parentFragmentManager, "startTimePicker")
        }

        return binding.root
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogBinding = DialogAlertCustomBinding.inflate(layoutInflater) // 커스텀 레이아웃의 ViewBinding 생성
        builder.setView(dialogBinding.root)
        // 다이얼로그의 배경을 투명하게 설정
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // ViewBinding을 사용하여 TextView와 Button에 접근
        dialogBinding.alertDialogMessageTv.text = message
        dialogBinding.alertDialogBtnTv.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    // 📌 기존 수업과 1분이라도 겹치는지 확인하는 함수
    private fun isOverlapping(newClass: ClassSchedule): Boolean {
        val existingSchedules = scheduleViewModel.classSchedules.value ?: return false
        val newStartTime = parseTimeToMinutes(newClass.startTime)
        val newEndTime = parseTimeToMinutes(newClass.endTime)
        val newDayIndex = parseDayToIndex(newClass.day)

        for (existingClass in existingSchedules) {
            if (parseDayToIndex(existingClass.day) == newDayIndex) { // 같은 요일인지 확인
                val existingStart = parseTimeToMinutes(existingClass.startTime)
                val existingEnd = parseTimeToMinutes(existingClass.endTime)

                // 📌 1분이라도 겹치면 true 반환
                if (!(newEndTime <= existingStart || newStartTime >= existingEnd)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showBottomNav()
    }

    override fun onTimeSet(hour: Int, minute: Int) {
        // 선택한 시간(분 단위)
        val chosenTimeInMinutes = hour * 60 + minute

        if (startTimePickerDialog.isVisible) {
            // 시작 시간 텍스트 업데이트
            binding.scheduleAddClassStartTimepickerBtnTv.text = String.format("%02d:%02d", hour, minute)

            // 현재 설정된 종료 시간 파싱
            val currentEndTimeStr = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()
            val parts = currentEndTimeStr.split(":")
            if (parts.size == 2) {
                val endHour = parts[0].toIntOrNull() ?: hour
                val endMinute = parts[1].toIntOrNull() ?: minute
                val currentEndTimeInMinutes = endHour * 60 + endMinute

                // 시작 시간이 종료 시간보다 크면 종료 시간을 시작 시간 + 1시간으로 설정 (24시간 형식 고려)
                if (chosenTimeInMinutes >= currentEndTimeInMinutes) {
                    val newEndHour = (hour + 1) % 24
                    binding.scheduleAddClassEndTimepickerBtnTv.text = String.format("%02d:%02d", newEndHour, minute)
                }
            }
        } else if (endTimePickerDialog.isVisible) {
            // 종료 시간 텍스트 업데이트
            binding.scheduleAddClassEndTimepickerBtnTv.text = String.format("%02d:%02d", hour, minute)

            // 현재 설정된 시작 시간 파싱
            val currentStartTimeStr = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val parts = currentStartTimeStr.split(":")
            if (parts.size == 2) {
                val startHour = parts[0].toIntOrNull() ?: hour
                val startMinute = parts[1].toIntOrNull() ?: minute
                val currentStartTimeInMinutes = startHour * 60 + startMinute

                // 종료 시간이 시작 시간보다 작으면 시작 시간을 종료 시간 - 1시간으로 설정 (24시간 형식 고려)
                if (chosenTimeInMinutes <= currentStartTimeInMinutes) {
                    val newStartHour = (hour + 23) % 24  // (hour - 1) mod 24
                    binding.scheduleAddClassStartTimepickerBtnTv.text = String.format("%02d:%02d", newStartHour, minute)
                }
            }
        }
    }


    override fun onDaySet(day: String) {
        binding.scheduleAddClassDaypickerBtnTv.text = day
    }
}