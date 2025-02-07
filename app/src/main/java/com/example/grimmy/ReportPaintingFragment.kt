package com.example.grimmy

import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import com.example.grimmy.databinding.FragmentReportPaintingBinding
import com.example.grimmy.viewmodel.PaintingViewModel
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar
import java.util.Locale

class ReportPaintingFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {
    private lateinit var binding: FragmentReportPaintingBinding
    private var calendar = Calendar.getInstance()
    private val viewModel: PaintingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportPaintingBinding.inflate(inflater, container, false)

        // Initial calendar setup for the current week
        adjustToStartOfWeek()
        updateCalendarWeek()
        setupExamChart()
        setupMonthlyChart()

        // Set the default value of the date text view to the current month
        updateDateTextView(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1
        )

        // 주간 & 월간 성적 데이터 요청
        viewModel.fetchWeeklyScores(getCurrentWeekDates())
        viewModel.fetchMonthlyPerformance(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)

        // Set up the listeners for previous and next week buttons
        binding.paintingCalendarPreviousBtnIv.setOnClickListener { changeWeek(-1) }
        binding.paintingCalendarNextBtnIv.setOnClickListener { changeWeek(1) }

        // Set up the date picker dialog
        binding.paintingDatepickerLl.setOnClickListener {
            DatePickerDialogFragment().apply {
                listener = this@ReportPaintingFragment
            }.show(parentFragmentManager, "yearmonthPicker")
        }

        viewModel.weeklyScores.observe(viewLifecycleOwner) { updateExamChart(it) }
        viewModel.monthlyPerformance.observe(viewLifecycleOwner) { updateMonthlyChart(it) }
        return binding.root
    }
//    /** 📌 LiveData 옵저버 설정 */
//    private fun observeViewModel() {
//        viewModel.weeklyScores.observe(viewLifecycleOwner) { updateExamChart(it) }
//        viewModel.monthlyPerformance.observe(viewLifecycleOwner) { updateMonthlyChart(it) }
//    }
//
//    /** 📌 주간 & 월간 데이터 가져오기 */
//    private fun fetchData() {
//        viewModel.fetchWeeklyScores(getCurrentWeekDates())
//        viewModel.fetchMonthlyPerformance(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
//    }


    private fun adjustToStartOfWeek() {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -1)
        }
    }

    private fun updateCalendarWeek() {
        binding.paintingCalendarGl.removeAllViews()
        val weekDates = mutableListOf<String>()

        for (i in 0 until 7) {
            val dayView = layoutInflater.inflate(R.layout.item_calendar_day, binding.paintingCalendarGl, false)
            dayView.findViewById<TextView>(R.id.item_calendar_day_tv).text = "${calendar.get(Calendar.DAY_OF_MONTH)}"
            weekDates.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
            binding.paintingCalendarGl.addView(dayView)
            calendar.add(Calendar.DATE, 1)
        }
        calendar.add(Calendar.DATE, -7) // Reset the calendar to the start of the week
        viewModel.fetchWeeklyScores(weekDates)
    }

    private fun changeWeek(direction: Int) {
        calendar.add(Calendar.DATE, direction * 7)
        updateCalendarWeek()
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)

        // ✅ 주 변경 시 주간 & 월간 성적 다시 가져오기
        viewModel.fetchWeeklyScores(getCurrentWeekDates())
        viewModel.fetchMonthlyPerformance(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }


    /** 📌 해당 주차의 날짜 리스트 반환 */
    private fun getCurrentWeekDates(): List<String> {
        val dates = mutableListOf<String>()
        val tempCalendar = calendar.clone() as Calendar
        for (i in 0 until 7) {
            dates.add(
                String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    tempCalendar.get(Calendar.YEAR),
                    tempCalendar.get(Calendar.MONTH) + 1,
                    tempCalendar.get(Calendar.DAY_OF_MONTH)
                )
            )
            tempCalendar.add(Calendar.DATE, 1)
        }
        return dates
    }

    private fun updateDateTextView(year: Int, month: Int) {
        binding.paintingDatepickerBtnTv.text = String.format(Locale.getDefault(), "%d년 %d월", year, month)
    }

    override fun onDateSelected(year: Int, month: Int) {
        calendar.set(year, month - 1, 1)
        adjustToStartOfWeek()
        updateCalendarWeek()
        updateDateTextView(year, month)
//        updateMonthlyPerformance(year, month)

        // ✅ 월간 성적 데이터 다시 가져오기
        viewModel.fetchMonthlyPerformance(year, month)
    }


//    private fun updateMonthlyPerformance(year: Int, month: Int) {
//        viewModel.fetchMonthlyPerformance(year, month)
//    }

    private fun setupExamChart() {
        binding.paintingScoreGraphBc.apply {
            description.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 100f
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.isEnabled = false
            setTouchEnabled(false) // 터치 기능 비활성화

            // 줌 비활성화 설정
            setScaleEnabled(false) // 줌 기능을 비활성화
            isDoubleTapToZoomEnabled = false // 더블탭으로 줌 기능 비활성화
            setPinchZoom(false) // 핀치 제스처로 줌 비활성화

            // RoundBarChartRender 설정
            val render = CustomBarChartRender(this, animator, viewPortHandler)
            render.setRadius(15)
            this.renderer = render
        }
    }


    private fun updateExamChart(scores: List<PaintingScore>) {
        val entriesList = mutableListOf<MutableList<BarEntry>>()
        val maxGroupSize = 3  // 하루 최대 시험 개수
        val totalDays = scores.size  // 전체 날짜 개수
        val dailyExamCounts = scores.map { it.scores.size }
        val maxExamCount = dailyExamCounts.maxOrNull() ?: 1  // 가장 많은 시험 개수 찾기
        val barWidth = 0.7f / maxExamCount  // 바 너비 조정

        // 바 그룹 내 리스트 초기화
        for (i in 0 until maxGroupSize) {
            entriesList.add(mutableListOf())
        }

        // ✅ 각 날짜별 시험 점수 추가 (X축을 요일 중앙에 정렬)
        for (index in 0 until totalDays) {
            val scoreData = scores.getOrNull(index) // 해당 날짜 데이터
            val groupCenter = index.toFloat()  // 각 그룹의 중심 위치

            // 데이터가 있는 경우
            if (scoreData != null && scoreData.scores.isNotEmpty()) {
                val scoreCount = scoreData.scores.size // 현재 데이터의 점수 개수
                val adjustedMaxGroupSize = if (scoreCount > maxGroupSize) maxGroupSize else scoreCount // 실제 점수 개수로 조정

                scoreData.scores.forEachIndexed { i, score ->
                    if (i < adjustedMaxGroupSize) {
                        // 바의 X축 위치를 중앙 정렬
                        val xPosition = groupCenter + (i - (adjustedMaxGroupSize - 1) / 2f) * (barWidth + 0.05f)
                        entriesList[i].add(BarEntry(xPosition, score))
                    }
                }
            } else {
                    // 각 그룹의 중앙에 위치
                    entriesList[0].add(BarEntry(groupCenter, 0f))
            }
        }

        // ✅ 색상 설정
        val fixedColor = ContextCompat.getColor(requireContext(), R.color.main_color)
        val shadowColor = ContextCompat.getColor(requireContext(), R.color.bg_black)

        val shadowColorWithAlpha = Color.argb(76, Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor))


        val dataSets = entriesList.mapIndexed { i, entries ->
            BarDataSet(entries, "시험 ${i + 1}").apply {
                color = fixedColor
                setBarShadowColor(shadowColorWithAlpha) // 그림자 추가
                valueTextSize = 0f
            }
        }

        // ✅ BarData 설정
        val barData = BarData(*dataSets.toTypedArray()).apply {
            this.barWidth = barWidth
        }

        // ✅ 차트 적용
        binding.paintingScoreGraphBc.apply {
            data = barData
            setDrawBarShadow(true)

//            // ✅ X축 설정 (그래프 넓이 확장)
//            xAxis.axisMinimum = -0.5f  // 🔹 왼쪽 끝 확장
            xAxis.axisMaximum = totalDays.toFloat() - 0.45f  // 🔹 오른쪽 끝 확장
            xAxis.setCenterAxisLabels(true)  // 🔹 X축 중앙 정렬xAxis.granularity = 1f  // 🔹 하루 단위로 간격 유지
            xAxis.granularity = 1f  // 🔹 하루 단위로 간격 유지
//            xAxis.position = XAxis.XAxisPosition.BOTTOM

            invalidate()
        }
    }

    /** 📌 월 비교 차트 기본 설정 */
    private fun setupMonthlyChart() {
        binding.paintingScoreComparisonGraphBc.apply {
            description.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 100f
            axisLeft.granularity = 10f
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false

            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.isEnabled = false

            // X축의 간격 조정
            xAxis.labelCount = 2 // 레이블 수 조정
            xAxis.axisMinimum = -0.5f // 시작 위치 조정
            xAxis.axisMaximum = 1.5f // 끝 위치 조정

            setTouchEnabled(false) // 터치 기능 비활성화

            // 줌 비활성화 설정
            setScaleEnabled(false) // 줌 기능을 비활성화
            isDoubleTapToZoomEnabled = false // 더블탭으로 줌 기능 비활성화
            setPinchZoom(false) // 핀치 제스처로 줌 비활성화

            // RoundBarChartRender 설정
            val render = CustomBarChartRender2(this, animator, viewPortHandler)
            render.setLeftRadius(16f) // 왼쪽 둥글기 설정
            render.setRightRadius(16f) // 오른쪽 둥글기 설정
            this.renderer = render
        }
    }

    private fun updateMonthlyChart(data: PaintingMonthlyPerformance) {
        val entries = listOf(
            BarEntry(0f, data.lastMonthAvg),
            BarEntry(1f, data.thisMonthAvg)
        )

        val dataSet = BarDataSet(entries, "월 비교").apply {
            colors = listOf(Color.parseColor("#4B4B4B"), Color.parseColor("#FF6E1D"))
            valueTextSize = 0f
        }

        binding.paintingScoreComparisonGraphBc.data = BarData(dataSet)
        binding.paintingScoreComparisonGraphBc.invalidate()



        val percentageChange = if (data.lastMonthAvg != 0f) {
            ((data.thisMonthAvg - data.lastMonthAvg) / data.lastMonthAvg) * 100
        } else {
            if (data.lastMonthAvg == 0f && data.thisMonthAvg > 0f) {
                100f // 이전 달에 기록이 없으면, 이번 달이 첫 기록이므로 100% 증가한 것으로 간주
            } else {
                0f // 데이터가 없거나 이번 달도 0이면 변동 없음
            }
        }

        binding.paintingScorePercentTv.text = percentageChange?.let {
            String.format(
                Locale.getDefault(),
                if (it >= 0) "%.1f%% 올랐어요" else "%.1f%% 떨어졌어요",
                kotlin.math.abs(it)
            )
        }
    }
}
