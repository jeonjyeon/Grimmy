package com.example.grimmy

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.grimmy.databinding.FragmentScheduleAddClassBinding
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

        // 기본 시간 설정
        setDefaultTimes()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scheduleClassAddOkTv.setOnClickListener {
            val className = binding.scheduleAddClassNameEt.text.toString().trim()
            val classPlace = binding.scheduleAddClassPlaceEt.text.toString().trim()
            val classDay = binding.scheduleAddClassDaypickerBtnTv.text.toString()
            val startTime = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val endTime = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()

            if (className.isNotEmpty() && classDay.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                val newClass = ClassSchedule(className, classPlace, classDay, startTime, endTime)
                scheduleViewModel.addClass(newClass)
            }

            Log.d("ScheduleAddClassFragment", "Class: ${scheduleViewModel.classSchedules.value}")

            requireActivity().supportFragmentManager.popBackStack()
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
    }

    private fun saveData(className: String, classPlace: String) {
        Log.d("ScheduleAddClassFragment", "Class Name: $className")
        Log.d("ScheduleAddClassFragment", "Class Place: $classPlace")
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
        val adjustedMinute = minute * 5
        val selectedTime = String.format("%02d:%02d", hour, adjustedMinute)

        if (startTimePickerDialog.isVisible) {
            binding.scheduleAddClassStartTimepickerBtnTv.text = selectedTime
            // 시작 시간 변경 시 종료 시간 조정
            adjustEndTimeBasedOnStart(selectedTime)
        } else if (endTimePickerDialog.isVisible) {
            binding.scheduleAddClassEndTimepickerBtnTv.text = selectedTime
            // 종료 시간 변경 시 시작 시간 조정
            adjustStartTimeBasedOnEnd(selectedTime)
        }
    }

    override fun onDaySet(day: String) {
        binding.scheduleAddClassDaypickerBtnTv.text = day
    }

    private fun setDefaultTimes() {
        // 기본 시간 설정
        binding.scheduleAddClassStartTimepickerBtnTv.text = "08:00"
        binding.scheduleAddClassEndTimepickerBtnTv.text = "09:00"
    }

    private fun adjustStartTimeBasedOnEnd(endTime: String) {
        val startTime = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
        val startMinutes = convertToMinutes(startTime)
        val endMinutes = convertToMinutes(endTime)

        if (startMinutes >= endMinutes) {
            // 시작 시간을 종료 시간보다 1시간 이전으로 조정
            val newStartTime = adjustStartTime(endTime)
            binding.scheduleAddClassStartTimepickerBtnTv.text = newStartTime
            Log.d("ScheduleAddClassFragment", "Adjusted Start Time: $newStartTime")
        }
    }

    private fun adjustEndTimeBasedOnStart(startTime: String) {
        val endTime = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()
        val startMinutes = convertToMinutes(startTime)
        val endMinutes = convertToMinutes(endTime)

        if (endMinutes <= startMinutes) {
            // 종료 시간을 시작 시간보다 1시간 이후로 조정
            val newEndTime = adjustEndTime(startTime)
            binding.scheduleAddClassEndTimepickerBtnTv.text = newEndTime
            Log.d("ScheduleAddClassFragment", "Adjusted End Time: $newEndTime")
        }
    }

    private fun convertToMinutes(time: String): Int {
        val parts = time.split(":").map { it.toInt() }
        return parts[0] * 60 + parts[1] // 시간과 분을 분으로 변환
    }

    private fun adjustStartTime(endTime: String): String {
        val endParts = endTime.split(":").map { it.toInt() }
        val adjustedHour = endParts[0] - 1 // 종료 시간에서 1시간 뺀다
        // 음수 시간 처리
        return if (adjustedHour < 0) {
            String.format("23:%02d", endParts[1]) // 0시 이전으로 가는 경우 23시로 설정
        } else {
            String.format("%02d:%02d", adjustedHour, endParts[1])
        }
    }

    private fun adjustEndTime(startTime: String): String {
        val startParts = startTime.split(":").map { it.toInt() }
        val adjustedHour = startParts[0] + 1 // 시작 시간에서 1시간 더한다
        // 24시 이상 처리
        return if (adjustedHour > 23) {
            String.format("00:%02d", startParts[1]) // 24시가 넘어가는 경우 0시로 설정
        } else {
            String.format("%02d:%02d", adjustedHour, startParts[1])
        }
    }
}