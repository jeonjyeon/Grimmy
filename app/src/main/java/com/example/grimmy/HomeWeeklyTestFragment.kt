package com.example.grimmy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.grimmy.HomeWeeklyFragment.Companion.REQUEST_CODE_CUSTOM_GALLERY
import com.example.grimmy.Retrofit.Request.TestRecordSaveRequest
import com.example.grimmy.Retrofit.Response.TestCommentGetResponse
import com.example.grimmy.Retrofit.Response.TestRecordGetResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentHomeWeeklyTestBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class HomeWeeklyTestFragment : Fragment() {

    // 뷰 바인딩 변수
    private var _binding: FragmentHomeWeeklyTestBinding? = null
    private val binding get() = _binding!!

    // 달력 구현을 위한 Calendar 변수 (현재 날짜 기준)
    private var calendar: Calendar = Calendar.getInstance()

    private var pageUpListener: OnPageUpListener? = null

    private lateinit var emotions: List<TestEmotion>
    private var selectedMood: String = ""
    // 선택된 이미지 URI (CustomGalleryActivity에서 전달받은 값)
    private var selectedImageUri: Uri? = null
    private var savedCreatedAt: String? = null

    // 현재 선택된 날짜를 "yyyy-MM-dd" 형식으로 저장 (초기값은 오늘)
    private var currentSelectedDate: String = getCurrentDate("yyyy-MM-dd")

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

        // === [그림 및 코멘트 기능 추가] ===
        // testTodayDrawingBoxCl: 이미지(및 코멘트 오버레이)를 보여줄 컨테이너 (레이아웃에 정의되어 있어야 함)
        binding.testTodayDrawingBoxCl.setOnClickListener {
            // CustomGalleryActivity를 호출하여 이미지를 선택 (선택된 이미지들은 onActivityResult에서 처리)
            val intent = Intent(activity, CustomGalleryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CUSTOM_GALLERY)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CUSTOM_GALLERY && resultCode == Activity.RESULT_OK) {
            val selectedImages = data?.getParcelableArrayListExtra<Uri>("selectedImages")
            if (!selectedImages.isNullOrEmpty()) {
                // 선택된 이미지 중 첫 번째 이미지를 저장 (필요에 따라 여러 이미지를 처리할 수도 있음)
                selectedImageUri = selectedImages.first()
                setupDrawingViewPager(selectedImages)
            }
        }
    }


    private fun setupDrawingViewPager(selectedImages: List<Uri>) {
        val viewPager = binding.testTodayDrawingBoxCl.findViewById<ViewPager2>(R.id.drawing_viewpager)
        // 예시로 dailyId는 1 (실제 사용시 해당 테스트 record id 사용)
        val dailyId = 1
        val adapter = TestDrawingPagerAdapter(selectedImages, dailyId)
        viewPager.adapter = adapter
        viewPager.visibility = View.VISIBLE

        // 서버에서 테스트용 코멘트 조회 (GET API)
        loadTestComments(dailyId, adapter)
    }

    private fun loadTestComments(dailyId: Int, adapter: TestDrawingPagerAdapter) {
        RetrofitClient.service.getTestComment(dailyId).enqueue(object : Callback<List<TestCommentGetResponse>> {
            override fun onResponse(
                call: Call<List<TestCommentGetResponse>>,
                response: Response<List<TestCommentGetResponse>>
            ) {
                if (response.isSuccessful) {
                    val commentResponses = response.body() ?: emptyList()
                    val commentList = commentResponses.map { resp ->
                        TestDrawingPagerAdapter.Comment(
                            x = resp.x,
                            y = resp.y,
                            title = resp.title,
                            content = resp.content
                        )
                    }
                    adapter.updateComments(commentList)
                    Log.d("HomeWeeklyTestFragment", "코멘트 조회 성공: ${commentList.size}개")
                } else {
                    Log.d("HomeWeeklyTestFragment", "코멘트 조회 실패: ${response.code()} ${response.message()}")
                }
            }
            override fun onFailure(call: Call<List<TestCommentGetResponse>>, t: Throwable) {
                Log.d("HomeWeeklyTestFragment", "코멘트 조회 오류: ${t.message}")
            }
        })
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
        // 오늘 날짜(비교용; 시간 0시로 설정)
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(todayCal.time)

        // 이번 주 시작 날짜 복제
        val thisWeekStart = calendar.clone() as Calendar

        for (i in 0 until 7) {
            val currentDayCal = calendar.clone() as Calendar
            val currentDayStr = sdf.format(currentDayCal.time)

            // 디자인 결정: 오늘은 항상 item_calendar_today,
            // 그 외에 (오늘이 아닌) 선택된 날짜는 item_calendar_selected,
            // 기본 날짜는 item_calendar_day
            val layoutRes = when {
                currentDayStr == todayStr -> R.layout.item_calendar_today
                currentDayStr == currentSelectedDate -> R.layout.item_calendar_today
                else -> R.layout.item_calendar_day
            }
            val dayView = layoutInflater.inflate(layoutRes, binding.testCalendarGl, false)
            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = "${currentDayCal.get(Calendar.DAY_OF_MONTH)}"

            if (currentDayCal.after(todayCal)) {
                dayView.isClickable = false
                dayView.alpha = 0.5f
            } else {
                dayView.setOnClickListener {
                    // 자동 저장: 현재 선택된 날짜의 기록 저장
                    saveTestRecordForDate(currentSelectedDate)
                    // 업데이트: 새로 선택한 날짜 저장
                    currentSelectedDate = currentDayStr
                    // 조회: 해당 날짜의 기록 불러오기
                    loadRecordForDate(currentDayStr)
                    // UI 갱신: 달력 UI 업데이트하여 선택된 날짜는 item_calendar_selected로 표시
                    updateCalendarWeek()
                }
            }
            binding.testCalendarGl.addView(dayView)
            calendar.add(Calendar.DATE, 1)
        }
        // 달력 업데이트 후 calendar를 이번 주 시작 날짜로 재설정
        calendar.set(
            thisWeekStart.get(Calendar.YEAR),
            thisWeekStart.get(Calendar.MONTH),
            thisWeekStart.get(Calendar.DAY_OF_MONTH)
        )
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
            TestEmotion(binding.testEmotionLoveIv, R.drawable.img_emotion_love, R.drawable.img_emotion_love_off, "love"),
            TestEmotion(binding.testEmotionSadIv, R.drawable.img_emotion_sad, R.drawable.img_emotion_sad_off, "sad"),
            TestEmotion(binding.testEmotionLighteningIv, R.drawable.img_emotion_lightening, R.drawable.img_emotion_lightening_off, "lightening"),
            TestEmotion(binding.testEmotionSleepyIv, R.drawable.img_emotion_sleepy, R.drawable.img_emotion_sleepy_off, "sleepy"),
            TestEmotion(binding.testEmotionHappyIv, R.drawable.img_emotion_happy, R.drawable.img_emotion_happy_off, "happy"),
            TestEmotion(binding.testEmotionAngryIv, R.drawable.img_emotion_angry, R.drawable.img_emotion_angry_off, "angry"),
            TestEmotion(binding.testEmotionTiredIv, R.drawable.img_emotion_tired, R.drawable.img_emotion_tired_off, "tired"),
            TestEmotion(binding.testEmotionXxIv, R.drawable.img_emotion_xx, R.drawable.img_emotion_xx_off, "xx"),
            TestEmotion(binding.testEmotionStressIv, R.drawable.img_emotion_stress, R.drawable.img_emotion_stress_off, "stress")
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

    private fun saveTestRecordForDate(recordDate: String) {
        val drawing = selectedImageUri?.toString() ?: ""
        val todayMood = selectedMood

        val createdAt = savedCreatedAt ?: getCurrentDateTime().also { savedCreatedAt = it }
        val updatedAt = getCurrentDateTime()

        val scoreText = binding.testScoreTv.text.toString()  // 예: "00점"
        val scoreInt = scoreText.substringBefore("점").trim().toIntOrNull() ?: 0

        val request = TestRecordSaveRequest(
            userId = 1,
            testDayRecording = recordDate,
            drawing = drawing,
            drawingTime = binding.testTimeTakenTimeTv.text.toString(),
            score = scoreInt,
            feedback = binding.testFeedbackEdittextEt.text.toString(),
            difficultIssue = binding.testHardEdittextEt.text.toString(),
            goodIssue = binding.testGoodEdittextEt.text.toString(),
            addTime = binding.testMoreTimeEdittextEt.text.toString(),
            satisfication = binding.progressText.text.toString(),
            todayMood = todayMood,
            moodDetail = binding.testFeelEdittextEt.text.toString(),
            question = binding.testQuestionEdittextEt.text.toString(),
            createdAt = createdAt,
            updateAt = updatedAt
        )

        RetrofitClient.service.postTestRecordSave(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("HomeWeeklyTestFragment", "[$recordDate] 기록 자동 저장 성공")
                    Toast.makeText(requireContext(), "[$recordDate] 자동 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("HomeWeeklyTestFragment", "[$recordDate] 기록 저장 실패: ${response.code()} , ${response.message()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("HomeWeeklyTestFragment", "[$recordDate] 기록 저장 에러: ${t.message}")
            }
        })
    }

    private fun loadRecordForDate(recordDate: String) {
        RetrofitClient.service.getTestRecordGet(userId = 1, date = recordDate).enqueue(object : Callback<TestRecordGetResponse> {
            override fun onResponse(call: Call<TestRecordGetResponse>, response: Response<TestRecordGetResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { record ->
                        binding.testFeedbackEdittextEt.setText(record.feedback ?: "")
                        binding.testHardEdittextEt.setText(record.difficultIssue ?: "")
                        binding.testGoodEdittextEt.setText(record.goodIssue ?: "")
                        binding.progressText.setText(record.satisfication ?: "")
                        binding.testQuestionEdittextEt.setText(record.question ?: "")
                        binding.testScoreTv.text = "${record.score} 점"
                        binding.testTimeTakenTimeTv.text = record.drawingTime ?: binding.testTimeTakenTimeTv.text
                        selectedMood = record.todayMood ?: ""
                        binding.testFeelEdittextEt.setText(record.moodDetail ?: "")
                        Toast.makeText(requireContext(), "[$recordDate] 기록을 불러왔습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("HomeWeeklyTestFragment", "[$recordDate] 기록 조회 실패: ${response.code()} ${response.message()}")
                    Toast.makeText(requireContext(), "[$recordDate] 기록 조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TestRecordGetResponse>, t: Throwable) {
                Log.d("HomeWeeklyTestFragment", "[$recordDate] 기록 조회 에러: ${t.message}")
                Toast.makeText(requireContext(), "[$recordDate] 기록 조회 에러", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return dateFormat.format(Date())
    }

    private fun getCurrentDate(format: String): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}