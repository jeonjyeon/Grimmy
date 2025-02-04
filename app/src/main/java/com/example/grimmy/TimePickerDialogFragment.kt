package com.example.grimmy

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.grimmy.databinding.DialogAlarmTimePickerBinding
import com.example.grimmy.viewmodel.TimeViewModel

class TimePickerDialogFragment : DialogFragment() {

    private lateinit var binding: DialogAlarmTimePickerBinding
    private val viewModel: TimeViewModel by activityViewModels() // ViewModel 초기화
    private var isMaterialReminder: Boolean = true // 기본값을 재료 알림으로 설정
    var listener: OnTimeSetListener? = null

    interface OnTimeSetListener {
        fun onTimeSet(hour: Int, minute: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAlarmTimePickerBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alarmTimePickerTp.setIs24HourView(true)
        setMinuteInterval(binding.alarmTimePickerTp, 5)

        // 초기 시간 설정
        val initialHour = arguments?.getInt("initialHour") ?: 0
        val initialMinute = arguments?.getInt("initialMinute") ?: 0
        setInitialTime(initialHour, initialMinute)

        binding.alarmTimePickerOkBtnTv.setOnClickListener {
            val hour = binding.alarmTimePickerTp.hour
            val minute = binding.alarmTimePickerTp.minute
            val selectedMinutes = minute * 5

            // 선택된 시간 ViewModel에 저장
            if (isMaterialReminder) {
                viewModel.setMaterialReminderTime(hour, selectedMinutes)
            } else {
                viewModel.setPaintingReminderTime(hour, selectedMinutes)
            }

            // 선택된 시간 전달
            listener?.onTimeSet(hour, selectedMinutes)
            dismiss()
        }

        binding.alarmTimePickerCancelBtnTv.setOnClickListener {
            dismiss()
        }

        fun setReminderType(isMaterial: Boolean) {
            this.isMaterialReminder = isMaterial
        }
    }

    private fun setInitialTime(hour: Int, minute: Int) {
        binding.alarmTimePickerTp.hour = hour
        binding.alarmTimePickerTp.minute = minute
    }

    private fun setMinuteInterval(timePicker: TimePicker, interval: Int) {
        try {
            val minutePicker = timePicker.findViewById<NumberPicker>(
                resources.getIdentifier("minute", "id", "android")
            )
            val minuteValues = Array(60 / interval) { String.format("%02d", it * interval) }
            minutePicker.minValue = 0
            minutePicker.maxValue = 60 / interval - 1
            minutePicker.displayedValues = minuteValues
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOnTimeSetListener(listener: OnTimeSetListener) {
        this.listener = listener
    }
}
