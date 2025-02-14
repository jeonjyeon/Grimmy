package com.example.grimmy

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.grimmy.utils.parseDayToIndex
import com.example.grimmy.utils.parseTimeToMinutes
import kotlin.math.ceil

// 내부적으로 사용하는 스케줄 데이터 모델 (요일: 0=월, 1=화, …, 4=금)
data class ScheduleItem(
    val dayOfWeek: Int,      // 0 ~ 4 (월~금)
    val startTimeMin: Int,   // 시작 시간 (분 단위)
    val endTimeMin: Int,     // 종료 시간 (분 단위)
    val courseName: String,
    val colorResId: Int      // 배경 색상 리소스 ID
)

class DynamicScheduleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GridLayout(context, attrs) {

    private var minTimeMin = 8 * 60
    private var maxTimeMin = 14 * 60
    private val cellHeight = 150

    init {
        columnCount = 6
    }

    fun updateSchedule(classSchedules: List<ClassSchedule>) {
        val scheduleItems = classSchedules.map { cs ->
            ScheduleItem(
                dayOfWeek = parseDayToIndex(cs.day),
                startTimeMin = parseTimeToMinutes(cs.startTime),
                endTimeMin = parseTimeToMinutes(cs.endTime),
                courseName = cs.className,
                colorResId = android.R.color.holo_blue_light // ✅ 색상 변경 가능
            )
        }
        minTimeMin = 8 * 60
        maxTimeMin = 14 * 60

        if (scheduleItems.isNotEmpty()) {
            scheduleItems.forEach { item ->
                if (item.startTimeMin < minTimeMin) minTimeMin = item.startTimeMin
                if (item.endTimeMin > maxTimeMin) maxTimeMin = item.endTimeMin
            }
            maxTimeMin = ((maxTimeMin + 59) / 60) * 60
        }

        buildBaseGrid()
        addScheduleItems(scheduleItems)
    }

    private fun buildBaseGrid() {
        removeAllViews()

        addView(createHeaderCell("", 0, 0))
        val dayLabels = listOf("월", "화", "수", "목", "금")
        for (i in 0 until 5) {
            addView(createHeaderCell(dayLabels[i], 0, i + 1))
        }

        val startHour = minTimeMin / 60
        val endHour = maxTimeMin / 60
        for (hour in startHour until endHour) {
            val currentRow = (hour - startHour) + 1
            addView(createTimeCell("${hour}:00", currentRow, 0))
            for (day in 0 until 5) {
                addView(createEmptyCell(currentRow, day + 1))
            }
        }
    }

    private fun createHeaderCell(text: String, row: Int, col: Int): TextView {
        return TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            setBackgroundResource(R.drawable.bg_schedule_cell)
        }
    }

    private fun createTimeCell(text: String, row: Int, col: Int): TextView {
        return TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            setBackgroundResource(R.drawable.bg_schedule_cell)
        }
    }

    private fun createEmptyCell(row: Int, col: Int): View {
        return View(context).apply {
            setBackgroundResource(R.drawable.bg_schedule_cell)
        }
    }

    private fun addScheduleItems(scheduleItems: List<ScheduleItem>) {
        scheduleItems.forEach { item ->
            var startRow = ((item.startTimeMin - minTimeMin) / 60) + 1
            var rowSpan = ceil((item.endTimeMin - item.startTimeMin).toDouble() / 60).toInt()

            // ✅ `rowSpan` 값이 1보다 작으면 최소 1로 설정
            if (rowSpan < 1) {
                rowSpan = 1
            }

            // ✅ `startRow`가 음수가 되지 않도록 보정
            if (startRow < 0) {
                startRow = 0
            }

            val col = item.dayOfWeek + 1

            val card = CardView(context).apply {
                radius = 20f
                setCardBackgroundColor(ContextCompat.getColor(context, item.colorResId))
                addView(TextView(context).apply {
                    text = item.courseName
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    gravity = Gravity.CENTER
                })
            }

            val param = GridLayout.LayoutParams(
                GridLayout.spec(startRow, rowSpan),
                GridLayout.spec(col, 1)
            ).apply {
                width = 0
                height = rowSpan * cellHeight
                setGravity(Gravity.FILL)
            }
            card.layoutParams = param
            addView(card)
        }
    }
}
