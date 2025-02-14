package com.example.grimmy

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grimmy.databinding.FragmentReportEmotionBinding
import com.example.grimmy.viewmodel.EmotionViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ReportEmotionFragment : Fragment() {
    private lateinit var binding: FragmentReportEmotionBinding
    private val viewModel: EmotionViewModel by activityViewModels()
    private lateinit var emotionAdapter: EmotionAdapter

    private val emotionIcons = mapOf(
        "기쁨" to R.drawable.img_emotion_happy,
        "분노" to R.drawable.img_emotion_angry,
        "슬픔" to R.drawable.img_emotion_sad,
        "집중" to R.drawable.img_emotion_lightening,
        "사랑" to R.drawable.img_emotion_love,
        "피곤" to R.drawable.img_emotion_sleepy
    )

    private val emotionColors = mapOf(
        "기쁨" to R.color.happy,
        "분노" to R.color.angry,
        "슬픔" to R.color.sad,
        "집중" to R.color.lightening,
        "사랑" to R.color.love,
        "피곤" to R.color.sleepy
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportEmotionBinding.inflate(inflater, container, false)


        // ✅ SharedPreferences에서 닉네임 불러오기
        val nickname = getNickname()
        binding.emotionProfileUsernameTv.text = nickname ?: "사용자"
        binding.emotionProfileUsername2Tv.text = nickname ?: "사용자"
        binding.emotionProfileUsername3Tv.text = nickname ?: "사용자"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart()
        setupRecyclerView()

        viewModel.emotionData.observe(viewLifecycleOwner, Observer { emotions ->
            // 데이터가 없거나 "데이터 없음"인 경우 처리
            if (emotions.isEmpty() || (emotions.size == 1 && emotions[0].name == "데이터 없음")) {
                // ✅ 회색 원만 표시 & 감정 목록 숨김
                binding.emotionMonthGraphPc.visibility = View.VISIBLE
                binding.emotionMonthPercentRv.visibility = View.GONE
                updatePieChart(emotions, isDataEmpty = true)
            } else {
                // ✅ 감정 데이터가 있고, value가 0이 아닌 경우 차트 + 목록 표시
                binding.emotionMonthGraphPc.visibility = View.VISIBLE
                binding.emotionMonthPercentRv.visibility = View.VISIBLE
                updatePieChart(emotions, isDataEmpty = false)
                updateEmotionList(emotions)
            }
        })
        // 서버에서 감정 데이터를 가져오는 함수 호출
        viewModel.fetchEmotionData()
    }

    // ✅ 저장된 닉네임 가져오기
    private fun getNickname(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }

    private fun setupRecyclerView() {
        emotionAdapter = EmotionAdapter(emptyList())
        binding.emotionMonthPercentRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = emotionAdapter
        }
    }

    private fun setupPieChart() {
        val pieChart = binding.emotionMonthGraphPc
        pieChart.apply {
            description.isEnabled = false // 차트의 설명 텍스트를 비활성화
            transparentCircleRadius = 0f  // 투명한 원 제거
            holeRadius = 60f // 구멍의 반지름을 60f로 설정
            rotationAngle = 180f
            legend.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            setHoleColor(ContextCompat.getColor(requireContext(), R.color.darker))
            setTouchEnabled(false) // 터치 이벤트 비활성화
        }
    }

    private fun updatePieChart(entries: List<Emotion>, isDataEmpty: Boolean) {
        if (entries.isEmpty()) return

        // 감정 데이터가 없을 때 회색 원 표시
        val colorList = if (isDataEmpty) {
            listOf(ContextCompat.getColor(requireContext(), R.color.bg_black2)) // 데이터 없음 색상
        } else {

            // 각 감정에 맞는 색상 가져오기
            entries.map { entry ->
                emotionColors[entry.name]?.let { colorRes ->
                    ContextCompat.getColor(requireContext(), colorRes)
                } ?: ContextCompat.getColor(requireContext(), R.color.bg_black2) // 기본 색상
            }
        }

        // 감정 데이터를 PieEntry로 변환
        val pieEntries = entries.map { emotion ->
            PieEntry(emotion.value, emotion.name)
        }

        val dataSet = PieDataSet(pieEntries, "").apply {
            sliceSpace = 0f // 칸 간격
            valueTextSize = 0f // 그래프에 표시되는 값 없앰.
            colors = colorList// 색상 설정
        }
        // PieData 생성 및 차트에 설정
        val data = PieData(dataSet)
        binding.emotionMonthGraphPc.data = data

        // 가장 큰 값을 가진 조각의 인덱스 찾기
        val maxIndex = entries.indexOfFirst { it.value == entries.maxOf { entry -> entry.value } }

        // 가장 큰 값을 가진 조각 강조
        if (maxIndex != -1) {
            // 강조 효과 적용
            binding.emotionMonthGraphPc.highlightValue(maxIndex.toFloat(), 0) // 인덱스와 데이터셋 인덱스 설정
        }

        binding.emotionMonthGraphPc.invalidate() // 차트 갱신

    }

    private fun updateEmotionList(entries: List<Emotion>) {

        val emotions = entries.map {
            Emotion(it.name, it.value, emotionIcons[it.name]?: R.drawable.img_default_profile)
        }

        emotionAdapter.updateData(emotions)
    }

}