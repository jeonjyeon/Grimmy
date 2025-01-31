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
import java.util.Calendar

class HomeWeeklyFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentHomeWeeklyBinding
    private var calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeWeeklyBinding.inflate(inflater, container, false)

        // Initial calendar setup for the current week
        adjustToStartOfWeek()

        // Update the calendar for the current week
        updateCalendarWeek()

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

    // Updates the calendar to show the start of the current week
    private fun adjustToStartOfWeek() {
        // Calculate the day of the week for the first day of the month
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Adjust to the previous Sunday if the first day is not Sunday
        val delta = Calendar.SUNDAY - dayOfWeek
        calendar.add(Calendar.DAY_OF_MONTH, delta)
    }

    // Updates the weekly calendar view
    private fun updateCalendarWeek() {
        binding.weeklyCalendarGl.removeAllViews()
        for (i in 0 until 7) {
            val dayView = layoutInflater.inflate(R.layout.item_calendar_day, binding.weeklyCalendarGl, false)
            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = "${calendar.get(Calendar.DAY_OF_MONTH)}"

            binding.weeklyCalendarGl.addView(dayView)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendar.add(Calendar.DAY_OF_MONTH, -7) // Reset the calendar to the start of the week
    }

    // Changes the week based on the direction (-1 for previous, 1 for next)
    private fun changeWeek(direction: Int) {
        calendar.add(Calendar.DATE, direction * 7)
        updateCalendarWeek()
    }

    // Handles the date selected from the DatePickerDialog
    override fun onDateSelected(year: Int, month: Int) {
        // Set the calendar to the first day of the selected month
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Month is zero-based
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Set to the first day of the month

        // Adjust calendar to the Sunday of the week that contains the first day of the month
        adjustToStartOfWeek()

        // Update the weekly calendar view
        updateCalendarWeek()
    }
}