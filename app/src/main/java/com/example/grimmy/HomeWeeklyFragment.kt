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
import android.widget.Toast
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
import java.util.TimeZone

class HomeWeeklyFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentHomeWeeklyBinding
    private var calendar = Calendar.getInstance()

    companion object {
        const val REQUEST_CODE_CUSTOM_GALLERY = 1001
    }

    private var pageUpListener: OnPageUpListener? = null

    private var currentSelectedDate: String = getCurrentDate("yyyy-MM-dd")
    private lateinit var emotions: List<TestEmotion>
    private var selectedMood: String = ""
    // 선택된 이미지 URI를 저장 (CustomGalleryActivity에서 전달받은 값)
    private var selectedImageUri: Uri? = null
    private var savedCreatedAt: String? = null

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
            // DailyRecordSave() // 서버 연동 확인을 위해 임시 버튼으로 사용
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

    override fun onPause() {
        super.onPause()
        // 화면 전환 전에 자동 저장
        DailyRecordSave()
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

        // 오늘 날짜 (시간은 0시로 맞춰 비교 용이하게 처리)
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 이번 주의 시작 날짜를 복제 (현재 calendar는 달력 시작 날짜)
        val thisWeekStart = calendar.clone() as Calendar

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until 7) {
            // 달력에서 표시할 날짜 복제
            val currentDayCal = calendar.clone() as Calendar

            val dayView = layoutInflater.inflate(
                if (currentDayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    currentDayCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                    currentDayCal.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH)
                ) R.layout.item_calendar_today else R.layout.item_calendar_day,
                binding.weeklyCalendarGl, false
            )

            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = "${currentDayCal.get(Calendar.DAY_OF_MONTH)}"

            // 미래 날짜이면 비활성화 처리
            if (currentDayCal.after(todayCal)) {
                dayView.isClickable = false
                dayView.alpha = 0.5f
            } else {
                // 과거 또는 오늘이면 클릭 리스너 등록
                dayView.setOnClickListener {
                    // 먼저 현재 선택 날짜의 데이터를 자동 저장
                    DailyRecordSave()
                    // 새로 선택한 날짜
                    val newSelectedDate = sdf.format(currentDayCal.time)
                    currentSelectedDate = newSelectedDate
                    // 기록 조회 및 편집 화면 업데이트 (추후 API 호출 등 구현)
                    // loadRecordForDate(newSelectedDate)
                    // (필요하다면, 선택된 날짜 UI 표시 업데이트도 수행)
                }
            }

            binding.weeklyCalendarGl.addView(dayView)
            calendar.add(Calendar.DATE, 1)
        }
        // 달력 업데이트 후 calendar를 이번 주의 시작 날짜로 재설정
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

    private fun getCurrentDate(format: String): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date())
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

    private fun getCurrentDateTime(): String {
        // "yyyy-MM-dd'T'HH:mm:ss" 형식으로 현재 날짜와 시간을 반환하는 포맷터 생성
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        // 타임존을 한국 표준시(Asia/Seoul)로 설정
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return dateFormat.format(Date())
    }

    private fun DailyRecordSave() {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val drawing = selectedImageUri.toString()
        val drawingTime = binding.weeklyTimeTakenTimeTv.text.toString()
        val todayMood = selectedMood

        val createdAt = savedCreatedAt ?: getCurrentDateTime().also { savedCreatedAt = it }
        // updatedAt은 매번 저장 시 현재 날짜로 갱신
        val updatedAt = getCurrentDateTime()

        val request = DailyRecordSaveRequest(
            userId = 1,
            dailyDayRecording = currentSelectedDate,
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
                    Toast.makeText(requireContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show()
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