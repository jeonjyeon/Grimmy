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
    val colorResId: Int      // 실제 색상 int를 저장
//    val color: Int      // 실제 색상 int를 저장
)

/**
// * 색상 리스트에서 순서대로 색상을 부여하고, 리스트보다 인덱스가 클 경우 약간의 밝기 조절로 변형된 색상을 반환합니다.
// */
//private fun getColorForIndex(index: Int): Int {
//    // baseColors: 원하는 기본 색상을 미리 정의 (R.color.xxx 대신 실제 리소스 id를 사용)
//    val baseColors = listOf(
//        ContextCompat.getColor(context, R.color.lightening),
//        ContextCompat.getColor(context, R.color.happy),
//        ContextCompat.getColor(context, R.color.love),
//        ContextCompat.getColor(context, R.color.xx),
//        ContextCompat.getColor(context, R.color.sleepy),
//        ContextCompat.getColor(context, R.color.sad),
//        ContextCompat.getColor(context, R.color.tired),
//        ContextCompat.getColor(context, R.color.stress),
//        ContextCompat.getColor(context, R.color.angry)
//    )
//    // 우선 기본 색상 선택
//    val baseColor = baseColors[index % baseColors.size]
//    // 만약 인덱스가 baseColors.size보다 크다면, 조정 계수를 계산합니다.
//    val multiplier = 1f + (index / baseColors.size) * 0.1f
//    return adjustColorBrightness(baseColor, multiplier)
//}
//
//// 색상의 밝기를 조절하는 함수. multiplier가 1.0이면 그대로, 1.1이면 약간 더 밝게.
//private fun adjustColorBrightness(color: Int, multiplier: Float): Int {
//    val a = Color.alpha(color)
//    val r = (Color.red(color) * multiplier).toInt().coerceAtMost(255)
//    val g = (Color.green(color) * multiplier).toInt().coerceAtMost(255)
//    val b = (Color.blue(color) * multiplier).toInt().coerceAtMost(255)
//    return Color.argb(a, r, g, b)
//}

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

    /**
     * 외부에서 ClassSchedule 리스트를 받아 내부 ScheduleItem으로 변환한 뒤,
     * 최대 요일 인덱스를 확인하여 totalColumns를 결정하고, 그리드를 구성합니다.
     */
    fun updateSchedule(classSchedules: List<ClassSchedule>) {
        // 여기서 종료 시간이 시작 시간보다 작으면 자정을 넘긴 것으로 처리
        val scheduleItems = classSchedules.map { cs ->
            val startTime = parseTimeToMinutes(cs.startTime)
            var endTime = parseTimeToMinutes(cs.endTime)
            if (endTime < startTime) {
                endTime += 24 * 60  // 자정을 넘겼으므로 24시간(1440분) 추가
            }
            // parseDayToIndex가 음수를 반환하면 0으로 보정
            val dayIndex = parseDayToIndex(cs.day).coerceAtLeast(0)
            ScheduleItem(
                dayOfWeek = dayIndex,
                startTimeMin = startTime,
                endTimeMin = endTime,
                courseName = cs.className,
                colorResId = android.R.color.holo_blue_light
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
            // 최대 시간을 60분 단위로 올림 처리
            maxTimeMin = if (maxTimeMin % 60 == 0) maxTimeMin else ((maxTimeMin + 59) / 60) * 60
        }
        // 기본은 월~금(인덱스 0~4); 토(5)나 일(6)가 있으면 열을 확장
        val maxDayIndex = scheduleItems.maxByOrNull { it.dayOfWeek }?.dayOfWeek ?: 4
        // 0열은 시간, 나머지는 (if maxDayIndex < 5 then 5, else maxDayIndex+1)
        val totalColumns = if (maxDayIndex < 5) 6 else (maxDayIndex + 2)
        columnCount = totalColumns

        buildBaseGrid(totalColumns)
        addScheduleItems(scheduleItems, totalColumns)
    }

    /**
     * 기본 그리드를 구성합니다.
     * totalColumns: 전체 열 수 (0열: 시간, 1~(totalColumns-1): 요일)
     */
    private fun buildBaseGrid(totalColumns: Int) {
        removeAllViews()
        val startHour = minTimeMin / 60
        val endHour = maxTimeMin / 60
        // 총 행 수: 헤더 1행 + 시간 행; 종료 시간을 포함하기 위해 +1
        val totalRows = (endHour - startHour) + 1

        // 헤더 행: row=0, col=0은 빈 칸
        addView(createHeaderCell("", 0, 0, totalRows, totalColumns))
        // 요일 헤더
        val dayLabels = when (totalColumns) {
            6 -> listOf("월", "화", "수", "목", "금")
            7 -> listOf("월", "화", "수", "목", "금", "토")
            8 -> listOf("월", "화", "수", "목", "금", "토", "일")
            else -> listOf("월", "화", "수", "목", "금")
        }
        for (i in dayLabels.indices) {
            addView(createHeaderCell(dayLabels[i], 0, i + 1, totalRows, totalColumns))
        }
        // 시간 행 (row 1부터) - 종료 시간을 포함하도록 endHour + 1까지 반복
        for (hour in startHour until endHour) {
            val currentRow = (hour - startHour) + 1
            addView(createTimeCell("$hour", currentRow, 0, totalRows, totalColumns))
            for (day in 0 until (totalColumns - 1)) {
                addView(createEmptyCell(currentRow, day + 1, totalRows, totalColumns))
            }
        }
    }

    /**
     * 셀 배경 drawable 반환 (res/drawable에 미리 정의)
     */
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

    /**
     * addScheduleItems:
     * - 단일 셀에 있는 경우: 기존처럼 상단 오프셋과 분 비율에 따른 높이 적용.
     * - 여러 셀에 걸치는 경우: 병합된 컨테이너(FrameLayout)를 GridLayout에 추가하고,
     *   내부에 실제 시간 비율대로 높이와 오프셋을 반영하는 카드(innerCard)를 배치.
     */
    private fun addScheduleItems(scheduleItems: List<ScheduleItem>, totalColumns: Int) {
        scheduleItems.forEach { item ->
            val col = item.dayOfWeek + 1
            val duration = item.endTimeMin - item.startTimeMin
            val startRow = ((item.startTimeMin - minTimeMin) / 60) + 1
            var endRow = ((item.endTimeMin - minTimeMin) / 60) + 1

            // 만약 수업 지속 시간이 정확 60분이면 단일 셀로 취급
            if (duration == 60) {
                endRow = startRow
            }

            val startOffsetRatio = (item.startTimeMin % 60) / 60f
            val endOffsetRatio = (item.endTimeMin % 60) / 60f

            if (startRow == endRow) {
                // 단일 셀인 경우
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
                // 여러 셀에 걸치는 경우 - 병합 컨테이너 사용
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

                // 실제 수업 지속 시간에 따른 높이 (분 비율 반영)
                val actualDurationMin = item.endTimeMin - item.startTimeMin
                val actualViewHeight = (actualDurationMin * cellHeight) / 60
                // 내부 카드의 topMargin: 첫 셀에서 시작 오프셋 반영
                val innerTopMargin = (startOffsetRatio * cellHeight).toInt()

                // customRadii: 병합된 경우, 내부 카드의 상단은 항상 둥글게, 하단은
                // 종료 오프셋이 있는 경우에만 둥글게 처리.
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

    /**
     * 단일 셀용 카드 생성 (여러 셀에 걸치는 경우는 별도 병합 컨테이너 사용)
     */
    private fun createScheduleCard(item: ScheduleItem, showText: Boolean, position: CardPosition): CardView {
        val card = CardView(context).apply {
            radius = 0f
        }
        val radiusValue = dpToPx(8).toFloat()
        val radii = when (position) {
            CardPosition.SINGLE -> floatArrayOf(radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue, radiusValue)
            CardPosition.FIRST -> floatArrayOf(radiusValue, radiusValue, radiusValue, radiusValue, 0f, 0f, 0f, 0f)
            CardPosition.MIDDLE -> floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
            CardPosition.LAST -> floatArrayOf(0f, 0f, 0f, 0f, radiusValue, radiusValue, radiusValue, radiusValue)
        }
        val drawable = GradientDrawable().apply {
            setColor(ContextCompat.getColor(context, item.colorResId))
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

    /**
     * 내부 카드 생성용: customRadii 배열을 받아 둥근 모서리를 적용합니다.
     */
    private fun createScheduleCardWithCustomRadii(item: ScheduleItem, showText: Boolean, customRadii: FloatArray): CardView {
        val card = CardView(context).apply {
            radius = 0f
        }
        val drawable = GradientDrawable().apply {
            setColor(ContextCompat.getColor(context, item.colorResId))
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
