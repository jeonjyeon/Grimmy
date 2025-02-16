package com.example.grimmy

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

    private lateinit var binding: FragmentGoalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoalBinding.inflate(inflater, container, false)

        // ✅ SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.goalProfileUsernameTv.text = nickname ?: "사용자"

        // 각 차트의 progressColor를 개별적으로 설정
        setupPieChart(binding.goalMonthGraphPc, 70f, R.color.main_color, 85f) // 이번 달 목표 70%
        setupPieChart(binding.goalWeekGraphPc, 60f, R.color.graph_color, 80f) // 이번 주 목표 60%

        binding.goalNotifyIconIv.setOnClickListener{
            // AlarmFragment로 전환
            val alarmFragment = AlarmFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.goal_notify_frame, alarmFragment) // fragment_container는 프래그먼트를 표시할 컨테이너의 ID입니다.
                .addToBackStack(null) // 뒤로 가기 스택에 추가
                .commit()
        }

        return binding.root
    }

    // ✅ 저장된 닉네임 가져오기
    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }

    private fun setupPieChart(pieChart: PieChart, percentage: Float, colorResId: Int, holeRadius: Float) {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            this.holeRadius = holeRadius
            transparentCircleRadius = 0f  // 투명한 원을 제거합니다.
            setHoleColor(Color.BLACK)  // 구멍의 색상을 검은색으로 설정합니다.
            setUsePercentValues(true)
            rotationAngle = 270f
            legend.isEnabled = false
            setTouchEnabled(false)
            setDrawCenterText(false)
        }

        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(percentage))
            add(PieEntry(100f - percentage))
        }

        val dataSet = PieDataSet(entries, "").apply {
            val progressColor = ContextCompat.getColor(requireContext(), colorResId)
            val remainColor = ContextCompat.getColor(requireContext(), R.color.bg_black2)

            setColors(listOf(progressColor, remainColor))
        }

        val data = PieData(dataSet).apply {
            setValueTextSize(0f) // 값 텍스트 숨기기
        }

        pieChart.data = data
        pieChart.invalidate() // 차트 갱신
    }
}