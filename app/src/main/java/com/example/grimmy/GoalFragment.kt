package com.example.grimmy

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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

        // SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.goalProfileUsernameTv.text = nickname ?: "사용자"

        // 차트 초기 설정 (초기값은 미리 설정해둔 70%/60%)
        setupPieChart(binding.goalMonthGraphPc, 70f, ContextCompat.getColor(requireContext(), R.color.main_color), 85f)
        setupPieChart(binding.goalWeekGraphPc, 60f, ContextCompat.getColor(requireContext(), R.color.graph_color), 80f)

        binding.goalNotifyIconIv.setOnClickListener{
            val alarmFragment = AlarmFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.goal_notify_frame, alarmFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.goalMonthExtraBtnIv.setOnClickListener {
            addBigBox() // 월 컨테이너에 big_box 추가
            updateMonthDonutChart() // 추가 후 업데이트
        }

        binding.goalWeekExtraBtnIv.setOnClickListener {
            addWeekBigBox() // 주 컨테이너에 big_box 추가
            updateWeekDonutChart()
        }

        // 기본으로 한 개씩 추가
        addBigBox()
        addWeekBigBox()
        updateMonthDonutChart()
        updateWeekDonutChart()

        // Material 섹션의 plus 버튼 설정 (material 체크박스 추가)
        setupMaterialAddition()

        return binding.root
    }

    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }

    private fun setupPieChart(pieChart: PieChart, percentage: Float, progressColor: Int, holeRadius: Float) {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            this.holeRadius = holeRadius
            transparentCircleRadius = 0f
            setHoleColor(Color.BLACK)
            setUsePercentValues(true)
            rotationAngle = 270f
            legend.isEnabled = false
            setTouchEnabled(false)
            setDrawCenterText(false)
        }
        updatePieChart(pieChart, percentage, progressColor)
    }

    private fun updatePieChart(pieChart: PieChart, percentage: Float, progressColor: Int) {
        val remainColor = ContextCompat.getColor(requireContext(), R.color.bg_black2)
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(percentage))
            add(PieEntry(100f - percentage))
        }
        val dataSet = PieDataSet(entries, "").apply {
            setColors(listOf(progressColor, remainColor))
            valueTextSize = 0f
        }
        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun addBigBox() {
        // 월용 big_box
        val bigBoxView = layoutInflater.inflate(R.layout.item_big_box, binding.goalMonthBigBoxContainer, false)
        val addEditTextIv = bigBoxView.findViewById<View>(R.id.goal_item_add_edittext_iv)
        val edittextContainer = bigBoxView.findViewById<ViewGroup>(R.id.big_box_edittext_container)

        addEditTextIv.setOnClickListener {
            addEditTextBox(edittextContainer)
            updateMonthDonutChart() // 추가 후 업데이트
        }
        // 기본으로 한 개의 edittext_box 추가
        addEditTextBox(edittextContainer)
        binding.goalMonthBigBoxContainer.addView(bigBoxView)
    }

    private fun addWeekBigBox() {
        // 주용 big_box
        val weekBigBoxView = layoutInflater.inflate(R.layout.item_big_box, binding.goalWeekBigBoxContainer, false)
        val addWeekEditTextIv = weekBigBoxView.findViewById<View>(R.id.goal_item_add_edittext_iv)
        val edittextWeekContainer = weekBigBoxView.findViewById<ViewGroup>(R.id.big_box_edittext_container)

        addWeekEditTextIv.setOnClickListener {
            addWeekEditTextBox(edittextWeekContainer)
            updateWeekDonutChart()
        }
        // 기본으로 한 개의 edittext_box 추가
        addWeekEditTextBox(edittextWeekContainer)
        binding.goalWeekBigBoxContainer.addView(weekBigBoxView)
    }

    // 월용 edittext_box 추가 함수
    private fun addEditTextBox(container: ViewGroup) {
        val editTextBoxView = layoutInflater.inflate(R.layout.item_goal_edittext, container, false)
        val checkboxIv = editTextBoxView.findViewById<ImageView>(R.id.goal_month_checkbox_iv)
        val editTextEt = editTextBoxView.findViewById<EditText>(R.id.goal_month_edittext_et)

        // 기본 체크 상태 false
        checkboxIv.tag = false

        checkboxIv.setOnClickListener {
            if (editTextEt.text.toString().isNotEmpty()) {
                val isChecked = checkboxIv.tag as Boolean
                if (!isChecked) {
                    // 체크: 이미지 변경, 스트라이크스루 효과, 텍스트 색상 변경
                    checkboxIv.setImageResource(R.drawable.img_checkbox_on)
                    editTextEt.paintFlags = editTextEt.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    editTextEt.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_black2))
                    checkboxIv.tag = true
                } else {
                    // 체크 해제: 원래 이미지, 효과 제거, 텍스트 색상 복원
                    checkboxIv.setImageResource(R.drawable.img_checkbox_off)
                    editTextEt.paintFlags = editTextEt.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    editTextEt.setTextColor(ContextCompat.getColor(requireContext(), R.color.font4))
                    checkboxIv.tag = false
                }
                // 재정렬: 체크된 항목을 하단으로 이동
                reorderContainer(container, isWeek = false)
                updateMonthDonutChart()
            }
        }
        container.addView(editTextBoxView)
    }

    // 주용 edittext_box 추가 함수
    private fun addWeekEditTextBox(container: ViewGroup) {
        val editTextBoxView = layoutInflater.inflate(R.layout.item_goal_week_edittext, container, false)
        val checkboxIv = editTextBoxView.findViewById<ImageView>(R.id.goal_week_checkbox_iv)
        val editTextEt = editTextBoxView.findViewById<EditText>(R.id.goal_week_edittext_et)

        checkboxIv.tag = false

        checkboxIv.setOnClickListener {
            if (editTextEt.text.toString().isNotEmpty()) {
                val isChecked = checkboxIv.tag as Boolean
                if (!isChecked) {
                    checkboxIv.setImageResource(R.drawable.img_checkbox_on)
                    editTextEt.paintFlags = editTextEt.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    editTextEt.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_black2))
                    checkboxIv.tag = true
                } else {
                    checkboxIv.setImageResource(R.drawable.img_checkbox_off)
                    editTextEt.paintFlags = editTextEt.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    editTextEt.setTextColor(ContextCompat.getColor(requireContext(), R.color.font4))
                    checkboxIv.tag = false
                }
                reorderContainer(container, isWeek = true)
                updateWeekDonutChart()
            }
        }
        container.addView(editTextBoxView)
    }

    private fun setupMaterialAddition() {
        // material 섹션의 plus 버튼과 컨테이너는 fragment_goal.xml에 정의되어 있다고 가정
        val addMaterialBtn = binding.root.findViewById<ImageView>(R.id.goal_material_add_edittext_iv)
        val materialContainer = binding.root.findViewById<ViewGroup>(R.id.goal_material_edittext_container)
        addMaterialBtn.setOnClickListener {
            addMaterialEditTextBox(materialContainer)
            // 필요시 material 도넛 차트 업데이트 호출
        }

        addMaterialEditTextBox(materialContainer)
    }

    private fun addMaterialEditTextBox(container: ViewGroup) {
        val materialView = layoutInflater.inflate(R.layout.item_material_checkbox, container, false)
        val checkboxIv = materialView.findViewById<ImageView>(R.id.goal_material_checkbox_iv)
        val editTextEt = materialView.findViewById<EditText>(R.id.goal_material_edittext_et)
        checkboxIv.tag = false

        checkboxIv.setOnClickListener {
            if (editTextEt.text.toString().isNotEmpty()) {
                val isChecked = checkboxIv.tag as Boolean
                if (!isChecked) {
                    checkboxIv.setImageResource(R.drawable.img_checkbox_on)
                    editTextEt.paintFlags = editTextEt.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    editTextEt.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_black2))
                    checkboxIv.tag = true
                } else {
                    checkboxIv.setImageResource(R.drawable.img_checkbox_off)
                    editTextEt.paintFlags = editTextEt.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    editTextEt.setTextColor(ContextCompat.getColor(requireContext(), R.color.font4))
                    checkboxIv.tag = false
                }
                // 재정렬: material 컨테이너 내의 항목들을 체크 상태에 따라 정렬
                reorderMaterialContainer(container)
            }
        }
        container.addView(materialView)
    }

    /**
     * material 컨테이너 내 항목들을 재정렬하는 함수.
     * EditText에 텍스트가 있는 항목 중 체크되지 않은 항목은 위로, 체크된 항목은 아래로 배치합니다.
     */
    private fun reorderMaterialContainer(container: ViewGroup) {
        val uncheckedViews = mutableListOf<View>()
        val checkedViews = mutableListOf<View>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            val checkbox: ImageView? = child.findViewById(R.id.goal_material_checkbox_iv)
            val editText: EditText? = child.findViewById(R.id.goal_material_edittext_et)
            if (editText != null && editText.text.toString().isNotEmpty()) {
                if (checkbox?.tag as? Boolean == true) {
                    checkedViews.add(child)
                } else {
                    uncheckedViews.add(child)
                }
            } else {
                // 텍스트가 없으면 그대로 unchecked로 처리
                uncheckedViews.add(child)
            }
        }
        container.removeAllViews()
        uncheckedViews.forEach { container.addView(it) }
        checkedViews.forEach { container.addView(it) }
    }

    private fun reorderContainer(container: ViewGroup, isWeek: Boolean) {
        val uncheckedViews = mutableListOf<View>()
        val checkedViews = mutableListOf<View>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            val checkbox: ImageView? = if (isWeek) {
                child.findViewById(R.id.goal_week_checkbox_iv)
            } else {
                child.findViewById(R.id.goal_month_checkbox_iv)
            }
            // 해당 항목의 EditText에 텍스트가 있는지 먼저 확인
            val editText: EditText? = if (isWeek) {
                child.findViewById(R.id.goal_week_edittext_et)
            } else {
                child.findViewById(R.id.goal_month_edittext_et)
            }
            if (editText != null && editText.text.toString().isNotEmpty()) {
                if (checkbox?.tag as? Boolean == true) {
                    checkedViews.add(child)
                } else {
                    uncheckedViews.add(child)
                }
            } else {
                // 텍스트가 없는 항목은 그대로 유지
                uncheckedViews.add(child)
            }
        }
        container.removeAllViews()
        // 먼저 체크되지 않은 항목 추가, 이후 체크된 항목 추가
        for (view in uncheckedViews) {
            container.addView(view)
        }
        for (view in checkedViews) {
            container.addView(view)
        }
    }

    // 월 도넛 차트 업데이트: EditText에 텍스트가 있는 항목만 계산
    private fun updateMonthDonutChart() {
        var total = 0
        var checked = 0
        for (i in 0 until binding.goalMonthBigBoxContainer.childCount) {
            val bigBox = binding.goalMonthBigBoxContainer.getChildAt(i)
            val container = bigBox.findViewById<ViewGroup>(R.id.big_box_edittext_container)
            for (j in 0 until container.childCount) {
                val item = container.getChildAt(j)
                val editText = item.findViewById<EditText>(R.id.goal_month_edittext_et)
                // 텍스트가 입력된 항목만 카운트
                if (editText != null && editText.text.toString().isNotEmpty()) {
                    total++
                    val checkbox = item.findViewById<ImageView>(R.id.goal_month_checkbox_iv)
                    val isChecked = checkbox?.tag as? Boolean ?: false
                    if (isChecked) {
                        checked++
                    }
                }
            }
        }
        val percentage = if (total > 0) (checked * 100f / total) else 0f
        updatePieChart(binding.goalMonthGraphPc, percentage, ContextCompat.getColor(requireContext(), R.color.main_color))
        binding.goalMonthPercentTv.text = "${percentage.toInt()}%"
    }

    // 주 도넛 차트 업데이트: EditText에 텍스트가 있는 항목만 계산
    private fun updateWeekDonutChart() {
        var total = 0
        var checked = 0
        for (i in 0 until binding.goalWeekBigBoxContainer.childCount) {
            val bigBox = binding.goalWeekBigBoxContainer.getChildAt(i)
            val container = bigBox.findViewById<ViewGroup>(R.id.big_box_edittext_container)
            for (j in 0 until container.childCount) {
                val item = container.getChildAt(j)
                val editText = item.findViewById<EditText>(R.id.goal_week_edittext_et)
                if (editText != null && editText.text.toString().isNotEmpty()) {
                    total++
                    val checkbox = item.findViewById<ImageView>(R.id.goal_week_checkbox_iv)
                    val isChecked = checkbox?.tag as? Boolean ?: false
                    if (isChecked) {
                        checked++
                    }
                }
            }
        }
        val percentage = if (total > 0) (checked * 100f / total) else 0f
        updatePieChart(binding.goalWeekGraphPc, percentage, ContextCompat.getColor(requireContext(), R.color.graph_color))
        binding.goalWeekPercentTv.text = "${percentage.toInt()}%"
    }
}