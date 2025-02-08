package com.example.grimmy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.grimmy.databinding.FragmentHomeWeeklyBinding
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeWeeklyFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentHomeWeeklyBinding
    private var calendar = Calendar.getInstance()

    companion object {
        const val REQUEST_CODE_CUSTOM_GALLERY = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeWeeklyBinding.inflate(inflater, container, false)

        // Initial calendar setup for the current week
        adjustToStartOfWeek()
        updateCalendarWeek()

        // Set the default value of the date text view to the current month
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1) // Calendar.MONTH는 0부터 시작

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

        // 기존: 전체 박스 클릭 시 CustomGalleryActivity 호출
        binding.weeklyTodayDrawingBoxCl.setOnClickListener {
            val intent = Intent(activity, CustomGalleryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CUSTOM_GALLERY)
        }

        // 추가: plus 버튼 클릭 시에도 CustomGalleryActivity 호출
        binding.weeklyTodayDrawingPlusBtnIv.setOnClickListener {
            val intent = Intent(activity, CustomGalleryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CUSTOM_GALLERY)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CUSTOM_GALLERY && resultCode == Activity.RESULT_OK) {
            val selectedImages = data?.getParcelableArrayListExtra<Uri>("selectedImages")
            if (!selectedImages.isNullOrEmpty()) {
                // 업데이트: ViewPager2와 인디케이터 설정
                setupDrawingViewPager(selectedImages)
            }
        }
    }

    private fun setupDrawingViewPager(selectedImages: List<Uri>) {
        // weekly_today_drawing_box_cl 내부의 ViewPager2, 인디케이터, 그리고 placeholder 가져오기
        val viewPager = binding.weeklyTodayDrawingBoxCl.findViewById<ViewPager2>(R.id.drawing_viewpager)
        val dotsIndicator = binding.weeklyTodayDrawingBoxCl.findViewById<DotsIndicator>(R.id.weekly_dot_indicator_di)
        val placeholder = binding.weeklyTodayDrawingBoxCl.findViewById<View>(R.id.weekly_placeholder_ll)

        // ViewPager2 어댑터 설정
        val adapter = DrawingPagerAdapter(selectedImages)
        viewPager.adapter = adapter

        // 인디케이터와 ViewPager2 연결
        dotsIndicator.setViewPager2(viewPager)

        // 이미지가 선택되었으므로 placeholder 감추고 ViewPager2와 인디케이터 보이기
        viewPager.visibility = View.VISIBLE
        dotsIndicator.visibility = View.VISIBLE
        placeholder.visibility = View.GONE
    }

    // --- 기존 캘린더 관련 메소드들 ---
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

        calendar.set(thisWeekStart.get(Calendar.YEAR), thisWeekStart.get(Calendar.MONTH), thisWeekStart.get(Calendar.DAY_OF_MONTH))
    }

    private fun changeWeek(direction: Int) {
        calendar.add(Calendar.DATE, direction * 7)
        updateCalendarWeek()
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
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

    // ViewPager2 어댑터: 선택된 그림들을 보여줌
    class DrawingPagerAdapter(private val images: List<Uri>) : RecyclerView.Adapter<DrawingPagerAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drawing_page, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.context)
                .load(images[position])
                .into(holder.imageView)
        }

        override fun getItemCount(): Int = images.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.drawing_page_iv)
        }
    }
}