package com.example.grimmy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.grimmy.databinding.FragmentReportPaintingBinding
import java.util.Calendar

class ReportPaintingFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {
    private lateinit var binding: FragmentReportPaintingBinding
    private var calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportPaintingBinding.inflate(inflater, container, false)

        // Initial calendar setup for the current week
        adjustToStartOfWeek()
        updateCalendarWeek()

        // Set the default value of the date text view to the current month
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1) // +1 because Calendar.MONTH is 0-based

        // Set up the listeners for previous and next week buttons
        binding.paintingCalendarPreviousBtnIv.setOnClickListener { changeWeek(-1) }
        binding.paintingCalendarNextBtnIv.setOnClickListener { changeWeek(1) }

        // Set up the date picker dialog
        binding.paintingDatepickerLl.setOnClickListener {
            val pickerFragment = DatePickerDialogFragment().apply {
                listener = this@ReportPaintingFragment
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
        binding.paintingCalendarGl.removeAllViews()
        for (i in 0 until 7) {
            val dayView = layoutInflater.inflate(R.layout.item_calendar_day, binding.paintingCalendarGl, false)
            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = "${calendar.get(Calendar.DAY_OF_MONTH)}"

            binding.paintingCalendarGl.addView(dayView)
            calendar.add(Calendar.DATE, 1)
        }
        calendar.add(Calendar.DATE, -7)  // Reset the calendar to the start of the week
    }

    private fun changeWeek(direction: Int) {
        calendar.add(Calendar.DATE, direction * 7)
        updateCalendarWeek()
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1) // Update the text view with the new month
    }

    private fun updateDateTextView(year: Int, month: Int) {
        binding.paintingDatepickerBtnTv.text = String.format("%d년 %d월", year, month)
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