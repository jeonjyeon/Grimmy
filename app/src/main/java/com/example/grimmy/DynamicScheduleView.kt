package com.example.grimmy

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.grimmy.utils.parseDayToIndex
import com.example.grimmy.utils.parseTimeToMinutes
import kotlin.math.ceil

data class ScheduleItem(
    val dayOfWeek: Int,      // 0 ~ 6 (월~일)
    val startTimeMin: Int,   // 시작 시간 (분 단위)
    val endTimeMin: Int,     // 종료 시간 (분 단위)
    val courseName: String,
    val color: Int           // 실제 색상 int를 저장
)

class DynamicScheduleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GridLayout(context, attrs) {

    // 기본 시간 범위: 8:00 ~ 14:00 (분 단위)
    private var minTimeMin = 8 * 60
    private var maxTimeMin = 14 * 60

    // 각 셀의 높이 (예: 75dp)
    private val cellHeight = dpToPx(75)

    enum class CardPosition {
        SINGLE, FIRST, MIDDLE, LAST
    }

    init {
        // 기본 열은 6 (0열: 시간, 1~5: 월~금)
        columnCount = 6
    }

    // --- 색상 관련 함수 ---
    private fun getColorForIndex(index: Int): Int {
        val baseColors = listOf(
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.happy),
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.lightening),
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.love),
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.xx),
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.sleepy),
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.sad),
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.tired),
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.stress),
            ContextCompat.getColor(this@DynamicScheduleView.context, R.color.angry)
        )
        val baseColor = baseColors[index % baseColors.size]
        val multiplier = 1f + (index / baseColors.size) * 0.1f
        return adjustColorBrightness(baseColor, multiplier)
    }

    private fun adjustColorBrightness(color: Int, multiplier: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * multiplier).toInt().coerceAtMost(255)
        val g = (Color.green(color) * multiplier).toInt().coerceAtMost(255)
        val b = (Color.blue(color) * multiplier).toInt().coerceAtMost(255)
        return Color.argb(a, r, g, b)
    }
    // --- 끝 색상 관련 함수 ---

    /**
     * 외부에서 ClassSchedule 리스트를 받아 내부 ScheduleItem으로 변환한 뒤,
     * 최대 요일 인덱스를 확인하여 totalColumns를 결정하고, 그리드를 구성합니다.
     */
    fun updateSchedule(classSchedules: List<ClassSchedule>) {
        // 종료 시간이 시작 시간보다 작으면 자정을 넘긴 것으로 처리
        val scheduleItems = classSchedules.mapIndexed { index, cs ->
            val startTime = parseTimeToMinutes(cs.startTime)
            var endTime = parseTimeToMinutes(cs.endTime)
            if (endTime < startTime) {
                endTime += 24 * 60  // 자정 넘김 처리
            }
            // parseDayToIndex가 음수를 반환하면 0으로 보정
            val dayIndex = parseDayToIndex(cs.day).coerceAtLeast(0)
            ScheduleItem(
                dayOfWeek = dayIndex,
                startTimeMin = startTime,
                endTimeMin = endTime,
                courseName = cs.className,
                color = getColorForIndex(index)  // 색상 리스트 순서대로 부여
            )
        }
        // 기본 시간 범위 초기화
        minTimeMin = 8 * 60
        maxTimeMin = 14 * 60

        if (scheduleItems.isNotEmpty()) {
            scheduleItems.forEach { item ->
                if (item.startTimeMin < minTimeMin) minTimeMin = item.startTimeMin
                if (item.endTimeMin > maxTimeMin) maxTimeMin = item.endTimeMin
            }
            // 최대 시간을 60분 단위로 올림 처리 (정각이면 그대로)
            maxTimeMin = if (maxTimeMin % 60 == 0) maxTimeMin else ((maxTimeMin + 59) / 60) * 60
        }
        val maxDayIndex = scheduleItems.maxByOrNull { it.dayOfWeek }?.dayOfWeek ?: 4
        val totalColumns = if (maxDayIndex < 5) 6 else (maxDayIndex + 2)
        columnCount = totalColumns

        buildBaseGrid(totalColumns)
        addScheduleItems(scheduleItems, totalColumns)
    }

    private fun buildBaseGrid(totalColumns: Int) {
        removeAllViews()
        val startHour = minTimeMin / 60
        val endHour = maxTimeMin / 60
        val totalRows = (endHour - startHour) + 1  // 종료 시간을 포함

        addView(createHeaderCell("", 0, 0, totalRows, totalColumns))
        val dayLabels = when (totalColumns) {
            6 -> listOf("월", "화", "수", "목", "금")
            7 -> listOf("월", "화", "수", "목", "금", "토")
            8 -> listOf("월", "화", "수", "목", "금", "토", "일")
            else -> listOf("월", "화", "수", "목", "금")
        }
        for (i in dayLabels.indices) {
            addView(createHeaderCell(dayLabels[i], 0, i + 1, totalRows, totalColumns))
        }
        for (hour in startHour until (endHour + 1)) {
            val currentRow = (hour - startHour) + 1
            addView(createTimeCell("$hour", currentRow, 0, totalRows, totalColumns))
            for (day in 0 until (totalColumns - 1)) {
                addView(createEmptyCell(currentRow, day + 1, totalRows, totalColumns))
            }
        }
    }

    private fun getCellBackground(row: Int, col: Int, totalRows: Int, totalColumns: Int): Int {
        return when {
            row == 0 && col == 0 -> R.drawable.bg_schedule_cell_top_left
            row == 0 && col == totalColumns - 1 -> R.drawable.bg_schedule_cell_top_right
            row == 0 -> R.drawable.bg_schedule_cell_top
            row == totalRows - 1 && col == 0 -> R.drawable.bg_schedule_cell_bottom_left
            row == totalRows - 1 && col == totalColumns - 1 -> R.drawable.bg_schedule_cell_bottom_right
            row == totalRows - 1 -> R.drawable.bg_schedule_cell_bottom
            col == 0 -> R.drawable.bg_schedule_cell_left
            col == totalColumns - 1 -> R.drawable.bg_schedule_cell_right
            else -> R.drawable.bg_schedule_cell
        }
    }

    private fun createHeaderCell(text: String, row: Int, col: Int, totalRows: Int, totalColumns: Int): TextView {
        return TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            typeface = ResourcesCompat.getFont(context, R.font.pretendard_medium)
            setBackgroundResource(getCellBackground(row, col, totalRows, totalColumns))
            minHeight = dpToPx(23)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(row, 1),
                GridLayout.spec(col, 1, if (col == 0) 0f else 1f)
            ).apply {
                width = if (col == 0) dpToPx(32) else 0
                height = dpToPx(23)
                setGravity(Gravity.FILL)
            }
        }
    }

    private fun createTimeCell(text: String, row: Int, col: Int, totalRows: Int, totalColumns: Int): TextView {
        return TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            typeface = ResourcesCompat.getFont(context, R.font.pretendard_regular)
            setBackgroundResource(getCellBackground(row, col, totalRows, totalColumns))
            minHeight = dpToPx(75)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(row, 1),
                GridLayout.spec(col, 1)
            ).apply {
                width = 0
                height = dpToPx(75)
                setGravity(Gravity.FILL)
            }
        }
    }

    private fun createEmptyCell(row: Int, col: Int, totalRows: Int, totalColumns: Int): View {
        return View(context).apply {
            setBackgroundResource(getCellBackground(row, col, totalRows, totalColumns))
            minimumHeight = dpToPx(75)
            layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(row, 1),
                GridLayout.spec(col, 1f)
            ).apply {
                width = 0
                height = dpToPx(75)
                setGravity(Gravity.FILL)
            }
        }
    }

    private fun addScheduleItems(scheduleItems: List<ScheduleItem>, totalColumns: Int) {
        scheduleItems.forEach { item ->
            val col = item.dayOfWeek + 1
            val duration = item.endTimeMin - item.startTimeMin
            val startRow = ((item.startTimeMin - minTimeMin) / 60) + 1
            var endRow = ((item.endTimeMin - minTimeMin) / 60) + 1
            // 수업 지속 시간이 정확 60분이면 단일 셀로 처리
            if (duration == 60) {
                endRow = startRow
            }
            val startOffsetRatio = (item.startTimeMin % 60) / 60f
            val endOffsetRatio = (item.endTimeMin % 60) / 60f

            if (startRow == endRow) {
                val topMargin = (startOffsetRatio * cellHeight).toInt()
                val viewHeight = ((item.endTimeMin - item.startTimeMin) * cellHeight) / 60
                val card = createScheduleCard(item, showText = true, position = CardPosition.SINGLE)
                val param = GridLayout.LayoutParams(
                    GridLayout.spec(startRow, 1),
                    GridLayout.spec(col, 1f)
                ).apply {
                    width = 0
                    height = viewHeight
                    setMargins(0, topMargin, 0, 0)
                    setGravity(Gravity.FILL)
                }
                card.layoutParams = param
                addView(card)
            } else {
                val totalMergedRows = endRow - startRow + 1
                val mergedHeight = totalMergedRows * cellHeight

                val container = FrameLayout(context)
                val containerParam = GridLayout.LayoutParams(
                    GridLayout.spec(startRow, totalMergedRows),
                    GridLayout.spec(col, 1f)
                ).apply {
                    width = 0
                    height = mergedHeight
                    setGravity(Gravity.FILL)
                }
                container.layoutParams = containerParam

                val actualViewHeight = (duration * cellHeight) / 60
                val innerTopMargin = (startOffsetRatio * cellHeight).toInt()

                val radiusValue = dpToPx(8).toFloat()
                val topLeft = radiusValue
                val topRight = radiusValue
                val bottomLeft = if (endOffsetRatio < 1f) radiusValue else 0f
                val bottomRight = if (endOffsetRatio < 1f) radiusValue else 0f
                val customRadii = floatArrayOf(topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft)

                val innerCard = createScheduleCardWithCustomRadii(item, showText = true, customRadii = customRadii)
                val innerParam = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    actualViewHeight
                ).apply {
                    topMargin = innerTopMargin
                }
                innerCard.layoutParams = innerParam

                container.addView(innerCard)
                addView(container)
            }
        }
    }

    private fun createScheduleCard(item: ScheduleItem, showText: Boolean, position: CardPosition): CardView {
        val card = CardView(context).apply { radius = 0f }
        val radiusValue = dpToPx(8).toFloat()
        val radii = when (position) {
            CardPosition.SINGLE -> floatArrayOf(radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue)
            CardPosition.FIRST -> floatArrayOf(radiusValue, radiusValue, radiusValue, radiusValue, 0f, 0f, 0f, 0f)
            CardPosition.MIDDLE -> floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
            CardPosition.LAST -> floatArrayOf(0f, 0f, 0f, 0f, radiusValue, radiusValue, radiusValue, radiusValue)
        }
        val drawable = GradientDrawable().apply {
            setColor(item.color) // item.color를 바로 사용
            cornerRadii = radii
        }
        card.background = drawable

        if (showText) {
            val durationMin = item.endTimeMin - item.startTimeMin
            val hours = durationMin / 60
            val minutes = durationMin % 60
            val durationText = when {
                hours > 0 && minutes > 0 -> "\n${hours}시간 ${minutes}분"
                hours > 0 -> "\n${hours}시간"
                else -> "\n${minutes}분"
            }
            val spannableString = SpannableString("${item.courseName}$durationText").apply {
                setSpan(AbsoluteSizeSpan(dpToPx(14)), 0, item.courseName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(AbsoluteSizeSpan(dpToPx(8)), item.courseName.length, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            card.addView(TextView(context).apply {
                text = spannableString
                typeface = ResourcesCompat.getFont(context, R.font.pretendard_regular)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                gravity = Gravity.LEFT
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            })
        }
        return card
    }

    private fun createScheduleCardWithCustomRadii(item: ScheduleItem, showText: Boolean, customRadii: FloatArray): CardView {
        val card = CardView(context).apply { radius = 0f }
        val drawable = GradientDrawable().apply {
            setColor(item.color)
            cornerRadii = customRadii
        }
        card.background = drawable

        if (showText) {
            val durationMin = item.endTimeMin - item.startTimeMin
            val hours = durationMin / 60
            val minutes = durationMin % 60
            val durationText = when {
                hours > 0 && minutes > 0 -> "\n${hours}시간 ${minutes}분"
                hours > 0 -> "\n${hours}시간"
                else -> "\n${minutes}분"
            }
            val spannableString = SpannableString("${item.courseName}$durationText").apply {
                setSpan(AbsoluteSizeSpan(dpToPx(14)), 0, item.courseName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(AbsoluteSizeSpan(dpToPx(8)), item.courseName.length, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            card.addView(TextView(context).apply {
                text = spannableString
                typeface = ResourcesCompat.getFont(context, R.font.pretendard_regular)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                gravity = Gravity.LEFT
                setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            })
        }
        return card
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }
}
