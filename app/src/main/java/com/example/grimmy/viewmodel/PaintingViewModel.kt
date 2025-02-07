package com.example.grimmy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grimmy.PaintingMonthlyPerformance
import com.example.grimmy.PaintingScore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class PaintingViewModel : ViewModel() {
    private val _weeklyScores = MutableLiveData<List<PaintingScore>>()
    val weeklyScores: LiveData<List<PaintingScore>> get() = _weeklyScores

    private val _monthlyPerformance = MutableLiveData<PaintingMonthlyPerformance>()
    val monthlyPerformance: LiveData<PaintingMonthlyPerformance> get() = _monthlyPerformance

    // 미리 설정된 성적 데이터 (3달치)
    private val preloadedScores = listOf(
        PaintingScore("2025-01-01", listOf(88f, 91f, 85f)),
        PaintingScore("2025-01-02", listOf()),
        PaintingScore("2025-01-03", listOf(72f)),
        PaintingScore("2025-01-04", listOf(85f, 92f)),
        PaintingScore("2025-01-05", listOf(88f, 91f, 85f)),
        PaintingScore("2025-01-06", listOf(79f, 80f)),
        PaintingScore("2025-01-07", listOf()),
        PaintingScore("2025-01-08", listOf(88f, 91f, 85f)),
        PaintingScore("2025-01-09", listOf(10f, 55f)),
        PaintingScore("2025-01-10", listOf(91f, 85f)),
        PaintingScore("2025-01-11", listOf(88f, 91f, 85f)),
        PaintingScore("2025-01-12", listOf(79f, 80f)),
        PaintingScore("2025-01-13", listOf()),
        PaintingScore("2025-01-14", listOf(23f)),
        PaintingScore("2025-01-15", listOf(10f, 55f)),
        PaintingScore("2025-01-16", listOf(77f)),
        PaintingScore("2025-01-17", listOf(65f)),
        PaintingScore("2025-01-18", listOf(89f)),
        PaintingScore("2025-01-19", listOf()),
        PaintingScore("2025-01-20", listOf()),
        PaintingScore("2025-01-21", listOf()),
        PaintingScore("2025-01-22", listOf(100f)),
        PaintingScore("2025-01-23", listOf(10f, 55f)),
        PaintingScore("2025-01-24", listOf(77f)),
        PaintingScore("2025-01-25", listOf(65f)),
        PaintingScore("2025-01-26", listOf(89f)),
        PaintingScore("2025-01-27", listOf()),
        PaintingScore("2025-01-28", listOf()),
        PaintingScore("2025-01-29", listOf(22f,50f)),
        PaintingScore("2025-01-30", listOf(15f)),
        PaintingScore("2025-01-31", listOf()),
        // 2025년 1월 다른 날짜들...

        PaintingScore("2025-02-01", listOf(80f)),
        PaintingScore("2025-02-02", listOf()),
        PaintingScore("2025-02-03", listOf(65f, 70f)),
        PaintingScore("2025-02-04", listOf(85f, 92f)),
        PaintingScore("2025-02-05", listOf(10f)),
        PaintingScore("2025-02-06", listOf(79f, 80f)),
        PaintingScore("2025-02-07", listOf()),
        PaintingScore("2025-02-08", listOf(91f, 85f)),
        PaintingScore("2025-02-09", listOf(10f, 55f)),
        PaintingScore("2025-02-10", listOf(70f)),
        PaintingScore("2025-02-11", listOf(88f, 91f, 85f)),
        PaintingScore("2025-02-12", listOf(79f, 80f)),
        PaintingScore("2025-02-13", listOf()),
        PaintingScore("2025-02-14", listOf(23f)),
        PaintingScore("2025-02-15", listOf(10f, 55f)),
        PaintingScore("2025-02-16", listOf(77f)),
        PaintingScore("2025-02-17", listOf(65f)),
        PaintingScore("2025-02-18", listOf(89f)),
        PaintingScore("2025-02-19", listOf()),
        PaintingScore("2025-02-20", listOf()),
        PaintingScore("2025-02-21", listOf()),
        PaintingScore("2025-02-22", listOf(100f)),
        PaintingScore("2025-02-23", listOf(10f, 55f)),
        PaintingScore("2025-02-24", listOf(77f)),
        PaintingScore("2025-02-25", listOf(65f)),
        PaintingScore("2025-02-26", listOf(89f)),
        PaintingScore("2025-02-27", listOf()),
        PaintingScore("2025-02-28", listOf(66f)),
        // 2025년 2월 다른 날짜들...
    )

    private var allDatesCache: List<String>? = null // 캐싱 변수 추가

    fun fetchWeeklyScores(dates: List<String>) {
        val scoresMap = preloadedScores.associateBy { it.date }

        val filledScores = dates.map { date ->
            scoresMap[date] ?: PaintingScore(date, emptyList()) // 데이터가 없으면 빈 리스트 추가
        }

        _weeklyScores.postValue(filledScores)
    }


    fun getAllDates(): List<String> {
        val allDates = mutableListOf<String>()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // 2000-2100, 2001-2101, 2002-2102 ... 이런 식으로 100년 범위 설정
        val startYear = 2000 - (2025 - currentYear)
        val endYear = startYear + 100

        for (year in startYear until endYear) {
            for (month in 0..11) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, 1)
                }

                val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                for (day in 1..lastDayOfMonth) {
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    allDates.add(formattedDate)
                }
            }
        }

        return allDates
    }



    /** 🔹 월간 성적 비교 데이터 */
    fun fetchMonthlyPerformance(year: Int, month: Int) {

        viewModelScope.launch {
            val lastMonthAvg = calculateMonthlyAverage(year, month - 1)
            val thisMonthAvg = calculateMonthlyAverage(year, month)

            _monthlyPerformance.postValue(PaintingMonthlyPerformance(lastMonthAvg, thisMonthAvg))
        }
    }

    /** 🔹 특정 월의 평균 점수 계산 */
    private fun calculateMonthlyAverage(year: Int, month: Int): Float {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // Calendar month is 0-based
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val allDays = (1..lastDayOfMonth).map { day -> "%04d-%02d-%02d".format(year, month, day) }
        val scoresMap = preloadedScores.associateBy { it.date }

        val monthScores = allDays.flatMap { date -> scoresMap[date]?.scores ?: emptyList() }

        return if (monthScores.isNotEmpty()) monthScores.average().toFloat() else 0f
    }
}
