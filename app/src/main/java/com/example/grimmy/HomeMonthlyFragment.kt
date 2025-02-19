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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.grimmy.Retrofit.Response.MonthlyRecordGetResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentHomeMonthlyBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeMonthlyFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentHomeMonthlyBinding

    private var monthlyRecordsMap: Map<String, String> = emptyMap()

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

        loadMonthlyRecords(currentYear, currentMonth + 1)

        return binding.root
    }

    override fun onDateSelected(year: Int, month: Int) {
        updateCalendar(year, month)
        loadMonthlyRecords(year, month)
    }

    private fun updateCalendar(year: Int, month: Int) {
        binding.monthlyDatepickerBtnTv.text = "${year}년 ${month}월"
        val gridLayout = binding.monthlyCalendarGl
        gridLayout.removeAllViews()

        // 오늘 날짜 (비교용)
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(todayCal.time)

        // 해당 달의 1일로 달력 설정
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1  // 0-based index
        val maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 전체 화면 너비에서 GridLayout의 양옆 패딩(20dp씩)을 제외한 가용 너비 계산
        val screenWidth = resources.displayMetrics.widthPixels
        val paddingPx = (20 * resources.displayMetrics.density * 2).toInt()  // 20dp 양쪽
        val availableWidth = screenWidth - paddingPx
        val cellWidth = availableWidth / 7

        // 빈 칸 채우기 (첫날 앞에 들어갈 자리)
        for (i in 0 until firstDayOfWeek) {
            val placeholder = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, gridLayout, false)
            placeholder.visibility = View.INVISIBLE
            val params = GridLayout.LayoutParams().apply {
                width = cellWidth
                height = GridLayout.LayoutParams.WRAP_CONTENT
                bottomMargin = (22 * resources.displayMetrics.density).toInt()
            }
            placeholder.layoutParams = params
            gridLayout.addView(placeholder)
        }

        // 실제 날짜 셀 생성
        for (day in 1..maxDayOfMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val currentDateStr = sdf.format(calendar.time)
            val layoutRes = if (currentDateStr == todayStr) R.layout.item_calendar_today else R.layout.item_calendar_day
            val dayView = LayoutInflater.from(context).inflate(layoutRes, gridLayout, false) as ConstraintLayout

            // 날짜 텍스트 설정
            val dayTextView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            dayTextView.text = day.toString()

            // 기본 이미지 설정 (default image)
            val recordImageView = dayView.findViewById<ImageView>(R.id.item_calendar_day_img_iv)
//            recordImageView.setImageResource(R.drawable.bg_circle_box_color)

            // 월별 기록 데이터가 있다면 해당 날짜의 displayImage 값을 사용하여 이미지 로드
            val displayImage = monthlyRecordsMap[currentDateStr]
            if (!displayImage.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(displayImage)
                    .into(recordImageView)
                recordImageView.visibility = View.VISIBLE
            } else {
                recordImageView.visibility = View.VISIBLE
            }

            // 각 날짜 셀의 LayoutParams에 동적으로 계산한 cellWidth 및 bottomMargin 적용
            val params = GridLayout.LayoutParams().apply {
                width = cellWidth
                height = GridLayout.LayoutParams.WRAP_CONTENT
                bottomMargin = (22 * resources.displayMetrics.density).toInt() // 22dp margin 하단 추가
            }
            dayView.layoutParams = params

            gridLayout.addView(dayView)
        }
    }

    /**
     * 월별 기록 데이터를 API를 통해 로드하여 monthlyRecordsMap에 저장한 후, 달력 UI를 갱신합니다.
     */
    private fun loadMonthlyRecords(year: Int, month: Int) {
        RetrofitClient.service.getMonthlyRecord(userId = 1, year = year, month = month)
            .enqueue(object : Callback<List<MonthlyRecordGetResponse>> {
                override fun onResponse(
                    call: Call<List<MonthlyRecordGetResponse>>,
                    response: Response<List<MonthlyRecordGetResponse>>
                ) {
                    if (response.isSuccessful) {
                        val records = response.body() ?: emptyList()
                        val map = mutableMapOf<String, String>()
                        // 각 기록을 map에 저장 (키: date, 값: displayImage)
                        for (record in records) {
                            map[record.date] = record.displayImage
                        }
                        monthlyRecordsMap = map
                        // 달력 UI를 다시 갱신하여 이미지가 반영되도록 함
                        updateCalendar(year, month)
                    } else {
                        Toast.makeText(requireContext(), "월별 기록 조회 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<MonthlyRecordGetResponse>>, t: Throwable) {
                    Log.d("HomeMonthlyFragment", "record fetch error: ${t.message}")
                    if (!isAdded) return  // 부착되어 있지 않으면 아무 작업도 하지 않음
                    Toast.makeText(requireContext(), "[record date] 기록 조회 에러", Toast.LENGTH_SHORT).show()
                }
            })
    }

}