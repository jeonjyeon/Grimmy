package com.example.grimmy

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.grimmy.databinding.DialogDayPickerBinding

class DayPickerDialogFragment : DialogFragment() {
    private lateinit var binding: DialogDayPickerBinding
    var listener: OnDaySetListener? = null

    interface OnDaySetListener {
        fun onDaySet(day: String)
    }

    fun setInitialDay(day: String) {
        selectedDay = day
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogDayPickerBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 초기 값 설정
        when (selectedDay) {
            "월요일" -> binding.dayPickerMonday.isSelected = true
            "화요일" -> binding.dayPickerTuesday.isSelected = true
            "수요일" -> binding.dayPickerWednesday.isSelected = true
            "목요일" -> binding.dayPickerThursday.isSelected = true
            "금요일" -> binding.dayPickerFriday.isSelected = true
            "토요일" -> binding.dayPickerSaturday.isSelected = true
            "일요일" -> binding.dayPickerSunday.isSelected = true
        }
        binding.dayPickerMonday.setOnClickListener { selectDay("월요일") }
        binding.dayPickerTuesday.setOnClickListener { selectDay("화요일") }
        binding.dayPickerWednesday.setOnClickListener { selectDay("수요일") }
        binding.dayPickerThursday.setOnClickListener { selectDay("목요일") }
        binding.dayPickerFriday.setOnClickListener { selectDay("금요일") }
        binding.dayPickerSaturday.setOnClickListener { selectDay("토요일") }
        binding.dayPickerSunday.setOnClickListener { selectDay("일요일") }

        binding.dayPickerOkBtnTv.setOnClickListener {
            listener?.onDaySet(selectedDay)
            dismiss()
        }

        binding.dayPickerCancelBtnTv.setOnClickListener {
            dismiss()
        }
    }

    private var selectedDay: String = ""

    private fun selectDay(day: String) {
        selectedDay = day
        // 선택된 요일을 강조 표시하는 로직을 추가할 수 있습니다.
    }

    fun setOnDaySetListener(listener: OnDaySetListener) {
        this.listener = listener
    }
}

