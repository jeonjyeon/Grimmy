package com.example.grimmy

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.example.grimmy.databinding.FragmentHomeWeeklyTestBinding
import java.util.Calendar


class HomeWeeklyTestFragment : Fragment() {

    // 뷰 바인딩 변수
    private var _binding: FragmentHomeWeeklyTestBinding? = null
    private val binding get() = _binding!!

    // 달력 구현을 위한 Calendar 변수 (현재 날짜 기준)
    private var calendar: Calendar = Calendar.getInstance()

    private var pageUpListener: OnPageUpListener? = null

    private lateinit var emotions: List<TestEmotion>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPageUpListener) {
            pageUpListener = context
        } else {
            throw RuntimeException("$context must implement OnPageUpListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeWeeklyTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // === SeekBar thumb 아래에 현재 progress 값을 텍스트로 표시 ===

        // 레이아웃이 완전히 배치된 후 thumb 위치를 업데이트
        binding.scoreSeekbar.post {
            updateProgressTextPosition()
        }

        binding.scoreSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // 텍스트에 progress 값 업데이트
                binding.progressText.text = progress.toString()
                // thumb의 현재 위치에 따라 progressText의 X좌표 재계산
                seekBar?.let { updateProgressTextPosition() }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { /* 필요 시 구현 */ }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { /* 필요 시 구현 */ }
        })

        // === 달력 기능 구현 ===

        // 1. 현재 날짜 기준으로 주의 시작(일요일)으로 조정
        adjustToStartOfWeek()
        // 2. GridLayout에 현재 주(7일)의 날짜를 표시
        updateCalendarWeek()
        // 3. 상단 날짜 텍스트 업데이트 (예: "2025년 2월")
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)

        // 4. 이전/다음 주 버튼 클릭 리스너 설정
        binding.testCalendarPreviousBtnIv.setOnClickListener { changeWeek(-1) }
        binding.testCalendarNextBtnIv.setOnClickListener { changeWeek(1) }

        binding.testToggleBtnIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.home_frame, HomeWeeklyFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.testPageUpBtnIv.setOnClickListener {
            pageUpListener?.onPageUpClicked()
        }

        setupEmotionClickListeners()

        binding.testTimeTakenTimeTv.setOnClickListener {
            val timePickerFragment = TakenTimeDialogFragment().apply {
                // TextView에 표시된 현재 시간을 파싱하여 초기값 전달 (없으면 0)
                val currentText = binding.testTimeTakenTimeTv.text.toString()
                val initialHours = currentText.substringBefore("시간").trim().toIntOrNull() ?: 0
                val initialMinutes = currentText.substringAfter("시간").substringBefore("분").trim().toIntOrNull() ?: 0
                arguments = Bundle().apply {
                    putInt("initialHours", initialHours)
                    putInt("initialMinutes", initialMinutes)
                }
                listener = object : TakenTimeDialogFragment.OnTimeSelectedListener {
                    override fun onTimeSelected(hours: Int, minutes: Int) {
                        binding.testTimeTakenTimeTv.text = String.format("%02d시간 %02d분", hours, minutes)
                    }
                }
            }
            timePickerFragment.show(parentFragmentManager, "takenTimePicker")
        }

        binding.testScoreTv.setOnClickListener {
            val scorePicker = ScorePickerDialogFragment().apply {
                // TextView에 표시된 현재 점수를 파싱하여 초기값 전달 (없으면 0으로)
                val currentText = binding.testScoreTv.text.toString()
                // "50 점"과 같은 형식으로 되어 있다면 숫자만 추출
                val initialScore = currentText.substringBefore(" ").toIntOrNull() ?: 0
                arguments = Bundle().apply {
                    putInt("initialScore", initialScore)
                }
                listener = object : ScorePickerDialogFragment.OnScoreSelectedListener {
                    override fun onScoreSelected(score: Int) {
                        binding.testScoreTv.text = String.format("%d 점", score)
                    }
                }
            }
            scorePicker.show(parentFragmentManager, "scorePicker")
        }

    }

    /**
     * SeekBar의 thumb 위치에 따라 progressText의 X 좌표를 업데이트하는 메서드
     * (thumb 아래에 현재 값이 중앙 정렬되도록 함)
     */
    private fun updateProgressTextPosition() {
        val seekBar = binding.scoreSeekbar
        val progressText = binding.progressText

        val seekBarWidth = seekBar.width
        val thumbOffset = seekBar.thumbOffset
        val fraction = seekBar.progress.toFloat() / seekBar.max.toFloat()
        val newX = seekBar.x + thumbOffset + fraction * (seekBarWidth - 2 * thumbOffset) - progressText.width / 2f
        progressText.x = newX
    }

    // ===== 달력 관련 메서드 =====

    /**
     * 달력을 현재 주의 시작(일요일)으로 조정하는 메서드
     * (현재 calendar 값이 일요일이 될 때까지 하루씩 감소)
     */
    private fun adjustToStartOfWeek() {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -1)
        }
    }

    /**
     * GridLayout에 현재 주(7일)의 날짜를 표시하는 메서드
     * - 오늘 날짜와 일치하면 item_calendar_today 레이아웃, 그 외는 item_calendar_day 레이아웃 사용
     */
    private fun updateCalendarWeek() {
        binding.testCalendarGl.removeAllViews()

        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH)
        val currentDate = today.get(Calendar.DAY_OF_MONTH)

        // 이번 주의 시작 날짜를 복제
        val thisWeekStart = calendar.clone() as Calendar

        for (i in 0 until 7) {
            val isToday = calendar.get(Calendar.YEAR) == currentYear &&
                    calendar.get(Calendar.MONTH) == currentMonth &&
                    calendar.get(Calendar.DAY_OF_MONTH) == currentDate

            val dayView = if (isToday) {
                layoutInflater.inflate(R.layout.item_calendar_today, binding.testCalendarGl, false)
            } else {
                layoutInflater.inflate(R.layout.item_calendar_day, binding.testCalendarGl, false)
            }

            // 날짜 항목 내부의 TextView (id: item_calendar_day_tv)에 날짜 숫자 설정
            val dayTextView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            dayTextView.text = "${calendar.get(Calendar.DAY_OF_MONTH)}"

            binding.testCalendarGl.addView(dayView)
            // 다음 날짜로 이동
            calendar.add(Calendar.DATE, 1)
        }
        // GridLayout 업데이트 후 calendar를 이번 주의 시작 날짜로 재설정
        calendar.set(thisWeekStart.get(Calendar.YEAR), thisWeekStart.get(Calendar.MONTH), thisWeekStart.get(Calendar.DAY_OF_MONTH))
    }

    /**
     * 주를 변경하는 메서드
     * @param direction -1이면 이전 주, 1이면 다음 주
     */
    private fun changeWeek(direction: Int) {
        calendar.add(Calendar.DATE, direction * 7)
        updateCalendarWeek()
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    /**
     * 상단의 날짜 텍스트(예: "2025년 2월")를 업데이트하는 메서드
     */
    private fun updateDateTextView(year: Int, month: Int) {
        binding.testDatepickerBtnTv.text = String.format("%d년 %d월", year, month)
    }

    private fun setupEmotionClickListeners() {
        // 감정 ImageView들이 binding에 포함되어 있다고 가정합니다.
        // 각 감정에 대해 활성 이미지와 비활성 이미지 리소스를 지정하세요.
        emotions = listOf(
            TestEmotion(binding.testEmotionLoveIv, R.drawable.img_emotion_love, R.drawable.img_emotion_love_off),
            TestEmotion(binding.testEmotionSadIv, R.drawable.img_emotion_sad, R.drawable.img_emotion_sad_off),
            TestEmotion(binding.testEmotionLighteningIv, R.drawable.img_emotion_lightening, R.drawable.img_emotion_lightening_off),
            TestEmotion(binding.testEmotionSleepyIv, R.drawable.img_emotion_sleepy, R.drawable.img_emotion_sleepy_off),
            TestEmotion(binding.testEmotionHappyIv, R.drawable.img_emotion_happy, R.drawable.img_emotion_happy_off),
            TestEmotion(binding.testEmotionAngryIv, R.drawable.img_emotion_angry, R.drawable.img_emotion_angry_off),
            TestEmotion(binding.testEmotionTiredIv, R.drawable.img_emotion_tired, R.drawable.img_emotion_tired_off),
            TestEmotion(binding.testEmotionXxIv, R.drawable.img_emotion_xx, R.drawable.img_emotion_xx_off),
            TestEmotion(binding.testEmotionStressIv, R.drawable.img_emotion_stress, R.drawable.img_emotion_stress_off)
        )
        emotions.forEach { emotion ->
            emotion.view.setOnClickListener {
                selectEmotion(emotion)
            }
        }
    }

    /**
     * 선택한 감정(Emotion)만 활성 이미지로, 나머지는 비활성 이미지로 변경합니다.
     */
    private fun selectEmotion(selectedEmotion: TestEmotion) {
        for (emotion in emotions) {
            if (emotion == selectedEmotion) {
                emotion.view.setImageResource(emotion.activeRes)
            } else {
                emotion.view.setImageResource(emotion.disabledRes)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}