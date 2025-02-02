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

    private var selectedDay: String = ""

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
        highlightSelectedDay(selectedDay)

        // 요일 클릭 리스너 설정
        binding.dayPickerMonday.setOnClickListener { selectDay("월요일") }
        binding.dayPickerTuesday.setOnClickListener { selectDay("화요일") }
        binding.dayPickerWednesday.setOnClickListener { selectDay("수요일") }
        binding.dayPickerThursday.setOnClickListener { selectDay("목요일") }
        binding.dayPickerFriday.setOnClickListener { selectDay("금요일") }
        binding.dayPickerSaturday.setOnClickListener { selectDay("토요일") }
        binding.dayPickerSunday.setOnClickListener { selectDay("일요일") }

        binding.dayPickerOkBtnTv.setOnClickListener {
            listener?.onDaySet(selectedDay) // 선택된 요일 전달
            dismiss()
        }

        binding.dayPickerCancelBtnTv.setOnClickListener {
            dismiss()
        }
    }

    private fun selectDay(day: String) {
        // 이전에 선택된 요일 색상 원래대로 변경
        resetDayColor(selectedDay)

        selectedDay = day // 새로운 요일 선택
        highlightSelectedDay(day) // 선택된 요일 강조
    }

    private fun highlightSelectedDay(day: String) {
        when (day) {
            "월요일" -> binding.dayPickerMonday.setTextColor(resources.getColor(R.color.main_color))
            "화요일" -> binding.dayPickerTuesday.setTextColor(resources.getColor(R.color.main_color))
            "수요일" -> binding.dayPickerWednesday.setTextColor(resources.getColor(R.color.main_color))
            "목요일" -> binding.dayPickerThursday.setTextColor(resources.getColor(R.color.main_color))
            "금요일" -> binding.dayPickerFriday.setTextColor(resources.getColor(R.color.main_color))
            "토요일" -> binding.dayPickerSaturday.setTextColor(resources.getColor(R.color.main_color))
            "일요일" -> binding.dayPickerSunday.setTextColor(resources.getColor(R.color.main_color))
        }
    }

    private fun resetDayColor(day: String) {
        when (day) {
            "월요일" -> binding.dayPickerMonday.setTextColor(resources.getColor(R.color.gray8))
            "화요일" -> binding.dayPickerTuesday.setTextColor(resources.getColor(R.color.gray8))
            "수요일" -> binding.dayPickerWednesday.setTextColor(resources.getColor(R.color.gray8))
            "목요일" -> binding.dayPickerThursday.setTextColor(resources.getColor(R.color.gray8))
            "금요일" -> binding.dayPickerFriday.setTextColor(resources.getColor(R.color.gray8))
            "토요일" -> binding.dayPickerSaturday.setTextColor(resources.getColor(R.color.gray8))
            "일요일" -> binding.dayPickerSunday.setTextColor(resources.getColor(R.color.gray8))
        }
    }

    fun setOnDaySetListener(listener: OnDaySetListener) {
        this.listener = listener
    }
}
