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

        binding.scheduleClassAddOkTv.setOnClickListener {
//            val className = binding.scheduleAddClassNameEt.text.toString().trim()
//            val classPlace = binding.scheduleAddClassPlaceEt.text.toString().trim()
//            saveData(className, classPlace)
            val className = binding.scheduleAddClassNameEt.text.toString().trim()
            val classPlace = binding.scheduleAddClassPlaceEt.text.toString().trim()
            val classDay = binding.scheduleAddClassDaypickerBtnTv.text.toString()
            val startTime = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val endTime = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()

            if (className.isNotEmpty() && classDay.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                val newClass = ClassSchedule(className, classPlace, classDay, startTime, endTime)
                scheduleViewModel.addClass(newClass)
            }

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

        return binding.root
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
        if (startTimePickerDialog.isVisible) {
            binding.scheduleAddClassStartTimepickerBtnTv.text = String.format("%02d:%02d", hour, minute)
        } else if (endTimePickerDialog.isVisible) {
            binding.scheduleAddClassEndTimepickerBtnTv.text = String.format("%02d:%02d", hour, minute)
        }
    }

    override fun onDaySet(day: String) {
        binding.scheduleAddClassDaypickerBtnTv.text = day
    }
}