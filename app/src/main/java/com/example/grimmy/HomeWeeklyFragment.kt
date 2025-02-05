package com.example.grimmy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.grimmy.databinding.FragmentHomeWeeklyBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeWeeklyFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentHomeWeeklyBinding
    private var calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeWeeklyBinding.inflate(inflater, container, false)

        // Initial calendar setup for the current week
        adjustToStartOfWeek()
        updateCalendarWeek()

        // Set the default value of the date text view to the current month
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1) // +1 because Calendar.MONTH is 0-based

        // Set up the listeners for previous and next week buttons
        binding.weeklyCalendarPreviousBtnIv.setOnClickListener { changeWeek(-1) }
        binding.weeklyCalendarNextBtnIv.setOnClickListener { changeWeek(1) }

        // Set up the date picker dialog
        binding.weeklyDatepickerLl.setOnClickListener {
            val pickerFragment = DatePickerDialogFragment().apply {
                listener = this@HomeWeeklyFragment
            }
            pickerFragment.show(parentFragmentManager, "yearmonthPicker")
        }

        return binding.root
    }

    private fun adjustToStartOfWeek() {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -1)
        }
    }

    private fun updateCalendarWeek() {
        binding.weeklyCalendarGl.removeAllViews()

        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH)
        val currentDate = today.get(Calendar.DAY_OF_MONTH)

        // Identify if the week includes today
        val thisWeekStart = calendar.clone() as Calendar
        val thisWeekEnd = (calendar.clone() as Calendar).apply {
            add(Calendar.DATE, 6)
        }

        for (i in 0 until 7) {
            val isToday = calendar.get(Calendar.YEAR) == currentYear &&
                    calendar.get(Calendar.MONTH) == currentMonth &&
                    calendar.get(Calendar.DAY_OF_MONTH) == currentDate

            val dayView = layoutInflater.inflate(
                if (isToday) R.layout.item_calendar_today else R.layout.item_calendar_day,
                binding.weeklyCalendarGl, false
            )

            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = "${calendar.get(Calendar.DAY_OF_MONTH)}"

            binding.weeklyCalendarGl.addView(dayView)
            calendar.add(Calendar.DATE, 1)
        }

        // Reset the calendar to the start of the week after filling the view
        calendar.set(thisWeekStart.get(Calendar.YEAR), thisWeekStart.get(Calendar.MONTH), thisWeekStart.get(Calendar.DAY_OF_MONTH))
    }

    private fun changeWeek(direction: Int) {
        calendar.add(Calendar.DATE, direction * 7)
        updateCalendarWeek()
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1) // Update the text view with the new month
    }

    private fun updateDateTextView(year: Int, month: Int) {
        binding.weeklyDatepickerBtnTv.text = String.format("%d년 %d월", year, month)
    }

    override fun onDateSelected(year: Int, month: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        adjustToStartOfWeek()
        updateCalendarWeek()
        updateDateTextView(year, month)
    }
}