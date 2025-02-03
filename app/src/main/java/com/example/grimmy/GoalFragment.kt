package com.example.grimmy

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.grimmy.databinding.FragmentGoalBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ColorTemplate.COLORFUL_COLORS

class GoalFragment : Fragment() {

    private lateinit var binding : FragmentGoalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGoalBinding.inflate(inflater,container,false)

        // Initialize the PieChart
        initPieChart()


        return binding.root
    }

    private fun initPieChart() {
        val pieChart = binding.goalGraphPc
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.BLACK)
        pieChart.setTransparentCircleColor(Color.BLACK)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.setCenterTextSize(20f)
        pieChart.description.isEnabled = false

        // 레전드 설정
        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.isEnabled = true

        // 데이터 설정
        setPieChartData(pieChart)
    }

    private fun setPieChartData(pieChart: PieChart) {
        val entries = listOf(
            PieEntry(70f, "이번 달 목표"),
            PieEntry(60f, "이번 주 목표")
        )
        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.setColors(listOf(Color.parseColor("#FFA726"), Color.parseColor("#66BB6A")))
        dataSet.valueLinePart1OffsetPercentage = 80f // Value lines to outside
        dataSet.valueLinePart1Length = 0.5f
        dataSet.valueLinePart2Length = 0.5f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueTextSize(16f)
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data
        pieChart.centerText = "이번 달 목표\n70%\n이번 주 목표\n60%"
        pieChart.invalidate() // refresh the chart
    }

}