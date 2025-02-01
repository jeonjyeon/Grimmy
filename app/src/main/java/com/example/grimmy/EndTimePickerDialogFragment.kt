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
import com.example.grimmy.databinding.DialogEndTimePickerBinding

class EndTimePickerDialogFragment : DialogFragment() {
    private lateinit var binding: DialogEndTimePickerBinding
    var listener: OnTimeSetListener? = null

    interface OnTimeSetListener {
        fun onTimeSet(hour: Int, minute: Int)
    }

    fun setInitialTime(hour: Int, minute: Int) {
        val adjustedMinute = minute / 5
        binding.endTimePickerTp.hour = hour
        binding.endTimePickerTp.minute = adjustedMinute
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogEndTimePickerBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.endTimePickerTp.setIs24HourView(true)
        setMinuteInterval(binding.endTimePickerTp, 5)

        val initialHour = arguments?.getInt("initialHour") ?: 0
        val initialMinute = arguments?.getInt("initialMinute") ?: 0
        setInitialTime(initialHour, initialMinute)

        binding.endTimePickerOkBtnTv.setOnClickListener {
            val hour = binding.endTimePickerTp.hour
            val minute = binding.endTimePickerTp.minute
            listener?.onTimeSet(hour, minute)
            dismiss()
        }
        binding.endTimePickerCancelBtnTv.setOnClickListener {
            dismiss()
        }
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