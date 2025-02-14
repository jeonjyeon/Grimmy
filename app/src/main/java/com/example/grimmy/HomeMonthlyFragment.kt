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

        // 달력 날짜 계산: 해당 달의 1일로 설정
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1  // 0-based index
        val maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 빈 칸 채우기 (첫날 앞에 들어갈 자리)
        for (i in 0 until firstDayOfWeek) {
            val placeholder = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, gridLayout, false)
            placeholder.visibility = View.INVISIBLE
            gridLayout.addView(placeholder)
        }

        // 실제 날짜 셀 생성
        for (day in 1..maxDayOfMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val currentDateStr = sdf.format(calendar.time)

            // 기본적으로 오늘 날짜는 항상 item_calendar_today, 선택되지 않은 날은 item_calendar_day
            // (원하는 경우 선택된 날짜에 대해 item_calendar_selected 디자인 적용 가능)
            val layoutRes = if (currentDateStr == todayStr) R.layout.item_calendar_today else R.layout.item_calendar_day

            val dayView = LayoutInflater.from(context).inflate(layoutRes, gridLayout, false)
            // 날짜 텍스트 설정
            val dayTextView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            dayTextView.text = day.toString()

            // 기본 이미지를 설정 (default image)
            val recordImageView = dayView.findViewById<ImageView>(R.id.item_calendar_day_img_iv)
            recordImageView.setImageResource(R.drawable.img_default_profile_dark)

            // 월별 기록 데이터가 있다면 해당 날짜의 displayImage 값을 사용하여 이미지 로드
            val displayImage = monthlyRecordsMap[currentDateStr]
            if (!displayImage.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(displayImage) // displayImage가 URI 문자열이라고 가정
                    .into(recordImageView)
                recordImageView.visibility = View.VISIBLE
            } else {
                // 데이터가 없으면 기본 이미지가 유지되거나, 필요에 따라 visibility를 GONE으로 처리할 수 있음
                recordImageView.visibility = View.VISIBLE
            }

            gridLayout.addView(dayView)
        }

        // 달력 상단 날짜 텍스트 업데이트 (예: "2024년 2월")
        binding.monthlyDatepickerBtnTv.text = "${year}년 ${month}월"
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
                    if (!isAdded) return // ✅ 프래그먼트가 활성 상태인지 확인

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
                    if (!isAdded) return // ✅ 프래그먼트가 활성 상태인지 확인

                    Toast.makeText(requireContext(), "월별 기록 조회 에러", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Helper: 현재 날짜를 지정한 형식으로 반환 (한국 표준시 적용)
    private fun getCurrentDate(format: String): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(Date())
    }
}