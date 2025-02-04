package com.example.grimmy

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.grimmy.databinding.DialogAlarmDatePickerBinding
import com.example.grimmy.viewmodel.TimeViewModel
import java.util.Calendar

class AlarmDatePickerDialogFragment : DialogFragment(),
    DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: DialogAlarmDatePickerBinding
    private val viewModel: TimeViewModel by activityViewModels()

    var listener: OnDateSelectedListener? = null
    private var selectedDayView: View? = null // 선택된 날짜의 뷰


    interface OnDateSelectedListener {
        fun onDateSelected(year: Int, month: Int, day: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAlarmDatePickerBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 초기 날짜 설정
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1  // 1-based index

        binding.alarmDateDatepickerLl.setOnClickListener{
            val pickerFragment= DatePickerDialogFragment().apply {
                listener = this@AlarmDatePickerDialogFragment
            }
            pickerFragment.show(parentFragmentManager, "yearmonthPicker")
        }

        updateCalendar(currentYear, currentMonth)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel의 LiveData를 관찰하여 시간이 변경될 때마다 UI 업데이트
        viewModel.materialReminderTime.observe(viewLifecycleOwner) { reminderTime ->
            reminderTime?.let {
                val hour = it.hour // ReminderTime에서 hour 가져오기
                val minute = it.minute // ReminderTime에서 minute 가져오기

                // 시간 표시
                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = String.format("%02d", if (hour % 12 == 0) 12 else hour % 12)
                val displayMinute = String.format("%02d", minute)

                binding.alarmDateTimeTv.text = "$displayHour : $displayMinute"

                // AM/PM 텍스트 뷰 배경 변경
                if (amPm == "AM") {
                    binding.alarmDateAmTv.setBackgroundResource(R.drawable.bg_cancel_btn) // AM 배경
                    binding.alarmDatePmTv.setBackgroundResource(R.color.transparent) // PM 배경 투명
                } else {
                    binding.alarmDatePmTv.setBackgroundResource(R.drawable.bg_cancel_btn) // PM 배경
                    binding.alarmDateAmTv.setBackgroundResource(R.color.transparent) // AM 배경 투명
                }
            }
        }
    }



    private fun updateCalendar(year: Int, month: Int) {
        val gridLayout = binding.alarmDateCalendarGl
        gridLayout.removeAllViews()  // Clear existing views

        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)  // Set to first day of the selected month

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Fill space before the first day
        for (i in 0 until firstDayOfWeek) {
            val space = LayoutInflater.from(context).inflate(R.layout.item_alarm_calendar_date, gridLayout, false)
            space.visibility = View.INVISIBLE  // Invisible placeholders
            gridLayout.addView(space)
        }

        // Fill actual days of the month
        for (day in 1..maxDayOfMonth) {
            val dayView = LayoutInflater.from(context).inflate(R.layout.item_alarm_calendar_date, gridLayout, false) as ViewGroup
            val textView = dayView.findViewById<TextView>(R.id.item_alarm_calendar_day_tv)
            textView.text = day.toString()

            // 날짜 클릭 이벤트 추가
            dayView.setOnClickListener {
                // 이전에 선택된 날짜가 있으면 초기화
                selectedDayView?.let { selected ->
                    // 이전 선택된 원의 색상 초기화
                    selected.findViewById<View>(R.id.item_alarm_calendar_date_img_iv)?.setBackgroundResource(R.drawable.bg_circle_box_color) // 원 색상 초기화
                    selected.findViewById<TextView>(R.id.item_alarm_calendar_day_tv)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color)) // 텍스트 색상 초기화
                }

                // 새 선택된 날짜 강조 표시
                dayView.findViewById<View>(R.id.item_alarm_calendar_date_img_iv)?.setBackgroundResource(R.drawable.bg_circle_select)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // 텍스트 색상 변경

                // 선택된 날짜 저장
                selectedDayView = dayView

                listener?.onDateSelected(year, month, day)  // 인터페이스 호출

                // 다이얼로그를 잠시 후에 닫기
                Handler(Looper.getMainLooper()).postDelayed({
                    dismiss()  // 다이얼로그 닫기
                }, 200)  // 200ms 후에 닫기
            }

            gridLayout.addView(dayView)
        }

        // Update the datepicker button text
        binding.alarmDateDatepickerBtnTv.text = "${year}년 ${month}월"
    }


    override fun onDateSelected(year: Int, month: Int) {
        updateCalendar(year, month)
    }
}
