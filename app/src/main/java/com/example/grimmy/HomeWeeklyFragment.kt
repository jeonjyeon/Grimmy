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
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.grimmy.Retrofit.Request.DailyRecordSaveRequest
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentHomeWeeklyBinding
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.Bidi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeWeeklyFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentHomeWeeklyBinding
    private var calendar = Calendar.getInstance()

    companion object {
        const val REQUEST_CODE_CUSTOM_GALLERY = 1001
    }

    private var pageUpListener: OnPageUpListener? = null

    private lateinit var emotions: List<TestEmotion>
    private var selectedMood: String = ""
    // 선택된 이미지 URI를 저장 (CustomGalleryActivity에서 전달받은 값)
    private var selectedImageUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPageUpListener) {
            pageUpListener = context
        } else {
            throw RuntimeException("$context must implement OnPageUpListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeWeeklyBinding.inflate(inflater, container, false)

        // Initial calendar setup for the current week
        adjustToStartOfWeek()
        updateCalendarWeek()

        // Set the default value of the date text view to the current month
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1) // Calendar.MONTH는 0부터 시작

        // Set up the listeners for previous and next week buttons
        binding.weeklyCalendarPreviousBtnIv.setOnClickListener { changeWeek(-1) }
        binding.weeklyCalendarNextBtnIv.setOnClickListener { changeWeek(1) }

        // Set up the date picker dialog
        binding.weeklyDatepickerLl.setOnClickListener {
            val pickerFragment = DatePickerDialogFragment().apply {
                listener = this@HomeWeeklyFragment
            }
            pickerFragment.show(parentFragmentManager, "yearmonthPicker")
        }

        // 기존: 전체 박스 클릭 시 CustomGalleryActivity 호출
        binding.weeklyTodayDrawingBoxCl.setOnClickListener {
            val intent = Intent(activity, CustomGalleryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CUSTOM_GALLERY)
        }

        // 추가: plus 버튼 클릭 시에도 CustomGalleryActivity 호출
        binding.weeklyTodayDrawingPlusBtnIv.setOnClickListener {
            val intent = Intent(activity, CustomGalleryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CUSTOM_GALLERY)
        }

        // weekly_toggle_btn_iv 클릭 시 HomeWeeklyTestFragment로 화면 전환
        binding.weeklyToggleBtnIv.setOnClickListener {
            // 전환할 Fragment의 컨테이너 id를 R.id.fragment_container로 가정 (실제 id로 변경 필요)
            parentFragmentManager.beginTransaction()
                .replace(R.id.home_frame, HomeWeeklyTestFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.weeklyPageUpBtnIv.setOnClickListener {
            pageUpListener?.onPageUpClicked()
            DailyRecordSave() // 서버 연동 확인을 위해 임시 버튼으로 사용
        }

        setupEmotionClickListeners()

        binding.weeklyTimeTakenTimeTv.setOnClickListener {
            val timePickerFragment = TakenTimeDialogFragment().apply {
                // TextView에 표시된 현재 시간을 파싱하여 초기값 전달 (없으면 0)
                val currentText = binding.weeklyTimeTakenTimeTv.text.toString()
                val initialHours = currentText.substringBefore("시간").trim().toIntOrNull() ?: 0
                val initialMinutes = currentText.substringAfter("시간").substringBefore("분").trim().toIntOrNull() ?: 0
                arguments = Bundle().apply {
                    putInt("initialHours", initialHours)
                    putInt("initialMinutes", initialMinutes)
                }
                listener = object : TakenTimeDialogFragment.OnTimeSelectedListener {
                    override fun onTimeSelected(hours: Int, minutes: Int) {
                        binding.weeklyTimeTakenTimeTv.text = String.format("%02d시간 %02d분", hours, minutes)
                    }
                }
            }
            timePickerFragment.show(parentFragmentManager, "takenTimePicker")
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CUSTOM_GALLERY && resultCode == Activity.RESULT_OK) {
            val selectedImages = data?.getParcelableArrayListExtra<Uri>("selectedImages")
            if (!selectedImages.isNullOrEmpty()) {
                // 여기서 하나의 이미지만 사용하도록 첫 번째 URI를 저장
                selectedImageUri = selectedImages.first()
                // 업데이트: ViewPager2와 인디케이터 설정 (갤러리 미리보기 등)
                setupDrawingViewPager(selectedImages)
            }
        }
    }

    private fun setupDrawingViewPager(selectedImages: List<Uri>) {
        // weekly_today_drawing_box_cl 내부의 ViewPager2, 인디케이터, 그리고 placeholder 가져오기
        val viewPager = binding.weeklyTodayDrawingBoxCl.findViewById<ViewPager2>(R.id.drawing_viewpager)
        val dotsIndicator = binding.weeklyTodayDrawingBoxCl.findViewById<DotsIndicator>(R.id.weekly_dot_indicator_di)
        val placeholder = binding.weeklyTodayDrawingBoxCl.findViewById<View>(R.id.weekly_placeholder_ll)

        // ViewPager2 어댑터 설정
        val adapter = DrawingPagerAdapter(selectedImages)
        viewPager.adapter = adapter

        // 인디케이터와 ViewPager2 연결
        dotsIndicator.setViewPager2(viewPager)

        // 이미지가 선택되었으므로 placeholder 감추고 ViewPager2와 인디케이터 보이기
        viewPager.visibility = View.VISIBLE
        dotsIndicator.visibility = View.VISIBLE
        placeholder.visibility = View.GONE
    }

    // --- 기존 캘린더 관련 메소드들 ---
    private fun adjustToStartOfWeek() {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -1)
        }
    }

    private fun updateCalendarWeek() {
        binding.weeklyCalendarGl.removeAllViews()

        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH)
        val currentDate = today.get(Calendar.DAY_OF_MONTH)

        val thisWeekStart = calendar.clone() as Calendar
        val thisWeekEnd = (calendar.clone() as Calendar).apply {
            add(Calendar.DATE, 6)
        }

        for (i in 0 until 7) {
            val isToday = calendar.get(Calendar.YEAR) == currentYear &&
                    calendar.get(Calendar.MONTH) == currentMonth &&
                    calendar.get(Calendar.DAY_OF_MONTH) == currentDate

            val dayView = layoutInflater.inflate(
                if (isToday) R.layout.item_calendar_today else R.layout.item_calendar_day,
                binding.weeklyCalendarGl, false
            )

            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = "${calendar.get(Calendar.DAY_OF_MONTH)}"

            binding.weeklyCalendarGl.addView(dayView)
            calendar.add(Calendar.DATE, 1)
        }

        calendar.set(thisWeekStart.get(Calendar.YEAR), thisWeekStart.get(Calendar.MONTH), thisWeekStart.get(Calendar.DAY_OF_MONTH))
    }

    private fun changeWeek(direction: Int) {
        calendar.add(Calendar.DATE, direction * 7)
        updateCalendarWeek()
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    private fun updateDateTextView(year: Int, month: Int) {
        binding.weeklyDatepickerBtnTv.text = String.format("%d년 %d월", year, month)
    }

    override fun onDateSelected(year: Int, month: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        adjustToStartOfWeek()
        updateCalendarWeek()
        updateDateTextView(year, month)
    }

    // ViewPager2 어댑터: 선택된 그림들을 보여줌
    class DrawingPagerAdapter(private val images: List<Uri>) : RecyclerView.Adapter<DrawingPagerAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drawing_page, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.context)
                .load(images[position])
                .into(holder.imageView)
        }

        override fun getItemCount(): Int = images.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.drawing_page_iv)
        }
    }

    private fun setupEmotionClickListeners() {
        // 감정 ImageView들이 binding에 포함되어 있다고 가정합니다.
        // 각 감정에 대해 활성 이미지와 비활성 이미지 리소스를 지정하세요.
        emotions = listOf(
            TestEmotion(binding.emotionLoveIv, R.drawable.img_emotion_love, R.drawable.img_emotion_love_off, "love"),
            TestEmotion(binding.emotionSadIv, R.drawable.img_emotion_sad, R.drawable.img_emotion_sad_off, "sad"),
            TestEmotion(binding.emotionLighteningIv, R.drawable.img_emotion_lightening, R.drawable.img_emotion_lightening_off, "lightening"),
            TestEmotion(binding.emotionSleepyIv, R.drawable.img_emotion_sleepy, R.drawable.img_emotion_sleepy_off, "sleepy"),
            TestEmotion(binding.emotionHappyIv, R.drawable.img_emotion_happy, R.drawable.img_emotion_happy_off, "happy"),
            TestEmotion(binding.emotionAngryIv, R.drawable.img_emotion_angry, R.drawable.img_emotion_angry_off, "angry"),
            TestEmotion(binding.emotionTiredIv, R.drawable.img_emotion_tired, R.drawable.img_emotion_tired_off, "tired"),
            TestEmotion(binding.emotionXxIv, R.drawable.img_emotion_xx, R.drawable.img_emotion_xx_off, "xx"),
            TestEmotion(binding.emotionStressIv, R.drawable.img_emotion_stress, R.drawable.img_emotion_stress_off, "stress")
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
        // 선택한 감정의 mood 값을 저장
        selectedMood = selectedEmotion.mood

        for (emotion in emotions) {
            if (emotion == selectedEmotion) {
                emotion.view.setImageResource(emotion.activeRes)
            } else {
                emotion.view.setImageResource(emotion.disabledRes)
            }
        }
    }

    fun getCurrentDateZeroTime(): String {
        // "yyyy-MM-dd" 형식으로 오늘 날짜 문자열 생성 (예: "2025-02-08")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        // 시간 부분은 "T00:00:00"으로 고정하여 반환
        return "${today}T00:00:00"
    }

    private fun DailyRecordSave() {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val drawing = selectedImageUri.toString()
        val drawingTime = binding.weeklyTimeTakenTimeTv.text.toString()
        val todayMood = selectedMood

        val createdAt = getCurrentDateZeroTime()
        val updatedAt = getCurrentDateZeroTime() // 처음 생성 시 동일하게 설정

        val request = DailyRecordSaveRequest(
            userId = 1,
            dailyDayRecording = today,
            drawing = drawing,
            drawingTime = drawingTime,
            feedback = binding.weeklyFeedbackEdittextEt.text.toString(),
            difficultIssue = binding.weeklyHardEdittextEt.text.toString(),
            goodIssue = binding.weeklyGoodEdittextEt.text.toString(),
            todayMood = todayMood,
            moodDetail = binding.weeklyFeelEdittextEt.text.toString(),
            question = binding.weeklyQuestionEdittextEt.text.toString(),
            createdAt = createdAt,
            updateAt = updatedAt
        )

        val call= RetrofitClient.service.postDailyRecordSave(
            request
        )
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("HomeWeeklyFragment",request.toString())
                if (response.isSuccessful) {
                    Log.d("HomeWeeklyFragment", "데일리 기록 저장 성공")
                    // 성공 처리 로직 추가
                } else {
                    Log.d("HomeWeeklyFragment", "데일리 기록 저장 실패: ${response.code()} , 에러메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("HomeWeeklyFragment", "데일리 기록 저장 에러: ${t.message}")
            }
        })
    }

}