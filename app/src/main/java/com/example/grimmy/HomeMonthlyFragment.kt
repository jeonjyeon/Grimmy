package com.example.grimmy

import android.app.DatePickerDialog
    import android.content.Context
    import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.grimmy.databinding.FragmentHomeMonthlyBinding
    import java.util.Calendar

class HomeMonthlyFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentHomeMonthlyBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHomeMonthlyBinding.inflate(inflater, container, false)

        // 초기 날짜 설정
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)  // 0-based index
        updateCalendar(currentYear, currentMonth + 1)  // +1 for display

        binding.monthlyDatepickerLl.setOnClickListener {
            val pickerFragment = DatePickerDialogFragment().apply {
                listener = this@HomeMonthlyFragment
            }
            pickerFragment.show(parentFragmentManager, "yearmonthPicker")
        }

        return binding.root
    }

    override fun onDateSelected(year: Int, month: Int) {
        updateCalendar(year, month)
    }

    private fun updateCalendar(year: Int, month: Int) {
        val gridLayout = binding.monthlyCalendarGl
        gridLayout.removeAllViews()  // Clear existing views

        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH)
        val currentDate = today.get(Calendar.DAY_OF_MONTH)

        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)  // Set to first day of the selected month

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Fill space before the first day
        for (i in 0 until firstDayOfWeek) {
            val space = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, gridLayout, false)
            space.visibility = View.INVISIBLE  // Invisible placeholders
            gridLayout.addView(space)
        }

        // Fill actual days of the month
        for (day in 1..maxDayOfMonth) {
            val isToday = year == currentYear && month - 1 == currentMonth && day == currentDate
            val dayView = if (isToday) {
                LayoutInflater.from(context).inflate(R.layout.item_calendar_today, gridLayout, false)
            } else {
                LayoutInflater.from(context).inflate(R.layout.item_calendar_day, gridLayout, false)
            } as ConstraintLayout

            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = day.toString()
            gridLayout.addView(dayView)

            if (isToday) {
                Log.d("UpdateCalendar", "Today's special layout applied: $day")
            }
        }

        // Update the datepicker button text
        binding.monthlyDatepickerBtnTv.text = "${year}년 ${month}월"
    }
}