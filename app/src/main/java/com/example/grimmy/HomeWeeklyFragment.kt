package com.example.grimmy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.grimmy.Retrofit.Request.DailyRecordGetRequest
import com.example.grimmy.Retrofit.Request.DailyRecordSaveRequest
import com.example.grimmy.Retrofit.Response.DailyRecordGetResponse
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeWeeklyBinding.inflate(inflater, container, false)

        // 초기 달력 설정
        adjustToStartOfWeek()
        updateCalendarWeek()
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        // 화면이 처음 로드될 때 오늘 날짜(현재 선택된 날짜)에 대한 기록을 조회
        loadRecordForDate(currentSelectedDate)

        // 이전/다음 주 버튼 리스너
        binding.weeklyCalendarPreviousBtnIv.setOnClickListener { changeWeek(-1) }
        binding.weeklyCalendarNextBtnIv.setOnClickListener { changeWeek(1) }

        // 날짜 선택 다이얼로그
        binding.weeklyDatepickerLl.setOnClickListener {
            val pickerFragment = DatePickerDialogFragment().apply {
                listener = this@HomeWeeklyFragment
            }
            pickerFragment.show(parentFragmentManager, "yearmonthPicker")
        }

        // CustomGalleryActivity 호출
        binding.weeklyTodayDrawingBoxCl.setOnClickListener {
            val intent = Intent(activity, CustomGalleryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CUSTOM_GALLERY)
        }
        binding.weeklyTodayDrawingPlusBtnIv.setOnClickListener {
            val intent = Intent(activity, CustomGalleryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CUSTOM_GALLERY)
        }

        // 토글 버튼
        binding.weeklyToggleBtnIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.home_frame, HomeWeeklyTestFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.weeklyPageUpBtnIv.setOnClickListener {
            pageUpListener?.onPageUpClicked()
            // 필요 시 임시 저장 버튼으로도 호출 가능
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
        // 화면 전환 전에 현재 선택된 날짜에 대해 자동 저장
        saveRecordForDate(parseDate(currentSelectedDate))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CUSTOM_GALLERY && resultCode == Activity.RESULT_OK) {
            val selectedImages = data?.getParcelableArrayListExtra<Uri>("selectedImages")
            if (!selectedImages.isNullOrEmpty()) {
                // 하나의 이미지만 사용하도록 첫 번째 URI 저장
                selectedImageUri = selectedImages.first()
                setupDrawingViewPager(selectedImages)
            }
        }
    }

    private fun parseDate(dateString: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // ✅ yyyy-MM-dd 형식 지정
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        val formattedDate = sdf.format(sdf.parse(dateString) ?: Date()) // ✅ String으로 변환 후 다시 Date로 변환
        return sdf.parse(formattedDate) ?: Date()
    }

    private fun setupDrawingViewPager(selectedImages: List<Uri>) {
        val viewPager = binding.weeklyTodayDrawingBoxCl.findViewById<ViewPager2>(R.id.drawing_viewpager)
        val dotsIndicator = binding.weeklyTodayDrawingBoxCl.findViewById<DotsIndicator>(R.id.weekly_dot_indicator_di)
        val placeholder = binding.weeklyTodayDrawingBoxCl.findViewById<View>(R.id.weekly_placeholder_ll)

        // DrawingPagerAdapter를 생성하고 viewPager의 어댑터로 지정
        val adapter = DrawingPagerAdapter(selectedImages)
        viewPager.adapter = adapter

        // 필요에 따라 dot indicator 등의 설정
        dotsIndicator.setViewPager2(viewPager)
        viewPager.visibility = View.VISIBLE
        dotsIndicator.visibility = View.VISIBLE
        placeholder.visibility = View.GONE
    }

    // --- 달력 관련 메소드 ---
    private fun adjustToStartOfWeek() {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -1)
        }
    }

    private fun updateCalendarWeek() {
        binding.weeklyCalendarGl.removeAllViews()
        // 오늘 날짜(비교용; 시간 0시로 맞춤)
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

            // 오늘 날짜는 항상 item_calendar_today 디자인 유지
            // 만약 현재 선택된 날짜가 오늘과 같다면 item_calendar_today가 선택 디자인과 동일하다고 볼 수 있음
            // 오늘이 아니면서 사용자가 선택한 날짜면 별도의 선택 디자인(item_calendar_selected)
            val layoutRes = when {
                currentDayStr == todayStr -> R.layout.item_calendar_today
                currentDayStr == currentSelectedDate -> R.layout.item_calendar_today
                else -> R.layout.item_calendar_day
            }
            val dayView = layoutInflater.inflate(layoutRes, binding.weeklyCalendarGl, false)
            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = "${currentDayCal.get(Calendar.DAY_OF_MONTH)}"

            if (currentDayCal.after(todayCal)) {
                dayView.isClickable = false
                dayView.alpha = 0.5f
            } else {
                dayView.setOnClickListener {
                    // 자동 저장: 기존 선택된 날짜 기록 저장
                    saveRecordForDate(parseDate(currentSelectedDate))
                    // 업데이트: 새 선택 날짜 설정
                    currentSelectedDate = currentDayStr
                    // 조회: 해당 날짜의 기록을 불러오기
                    loadRecordForDate(currentDayStr)
                    // UI 갱신: 달력의 각 항목이 올바른 디자인으로 표시되도록 갱신
                    updateCalendarWeek()
                }
            }
            binding.weeklyCalendarGl.addView(dayView)
            calendar.add(Calendar.DATE, 1)
        }
        // 달력 업데이트 후 calendar를 이번 주 시작 날짜로 재설정
        calendar.set(
            thisWeekStart.get(Calendar.YEAR),
            thisWeekStart.get(Calendar.MONTH),
            thisWeekStart.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun changeWeek(direction: Int) {
        calendar.add(Calendar.DATE, direction * 7)
        updateCalendarWeek()
        updateDateTextView(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    private fun updateDateTextView(year: Int, month: Int) {
        // 예: "YYYY년 M월" 형식으로 달력 상단에 표시
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
/*
    // --- ViewPager2 어댑터 ---
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
*/
    // --- 감정 관련 ---
    private fun setupEmotionClickListeners() {
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
     * 선택한 감정만 활성 이미지로, 나머지는 비활성 이미지로 변경하고 선택된 감정의 mood를 저장합니다.
     */
    private fun selectEmotion(selectedEmotion: TestEmotion) {
        selectedMood = selectedEmotion.mood
        for (emotion in emotions) {
            if (emotion == selectedEmotion) {
                emotion.view.setImageResource(emotion.activeRes)
            } else {
                emotion.view.setImageResource(emotion.disabledRes)
            }
        }
    }

    // --- 자동 저장 및 기록 조회 함수 ---
    private fun saveRecordForDate(recordDate: Date) {
        val drawing = selectedImageUri?.toString() ?: ""
        val todayMood = selectedMood

        val createdAt = savedCreatedAt ?: getCurrentDateTime().also { savedCreatedAt = it }
        val updatedAt = getCurrentDateTime()

        val request = DailyRecordSaveRequest(
            userId = 1,
            dailyDayRecording = recordDate,
            drawing = drawing,
            drawingTime = binding.weeklyTimeTakenTimeTv.text.toString(),
            feedback = binding.weeklyFeedbackEdittextEt.text.toString(),
            dfficultIssue = binding.weeklyHardEdittextEt.text.toString(),
            goodIssue = binding.weeklyGoodEdittextEt.text.toString(),
            todayMood = todayMood,
            moodDetail = binding.weeklyFeelEdittextEt.text.toString(),
            question = binding.weeklyQuestionEdittextEt.text.toString(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        Log.d("apiConnect",request.toString())
        Log.d("apiConnect",request.dailyDayRecording::class.java.toString())

        RetrofitClient.service.postDailyRecordSave(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("HomeWeeklyFragment", "[$recordDate] 기록 자동 저장 성공")
                    Toast.makeText(requireContext(), "[$recordDate] 자동 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("HomeWeeklyFragment", "[$recordDate] 기록 저장 실패: ${response.code()} , ${response.message()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("HomeWeeklyFragment", "[$recordDate] 기록 저장 에러: ${t.message}")
            }
        })
    }

    private fun loadRecordForDate(recordDate: String) {
        RetrofitClient.service.getDailyRecordGet(userId = 1, date = recordDate).enqueue(object : Callback<DailyRecordGetResponse> {
            override fun onResponse(call: Call<DailyRecordGetResponse>, response: Response<DailyRecordGetResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { record ->
                        // EditText 등에 기록된 내용을 채웁니다.
                        binding.weeklyFeedbackEdittextEt.setText(record.feedback ?: "")
                        binding.weeklyHardEdittextEt.setText(record.difficultIssue ?: "")
                        binding.weeklyGoodEdittextEt.setText(record.goodIssue ?: "")
                        binding.weeklyQuestionEdittextEt.setText(record.question ?: "")
                        // 예시로, drawingTime이나 moodDetail 등도 필요하면 채워 넣습니다.
                        binding.weeklyTimeTakenTimeTv.text = record.drawingTime ?: binding.weeklyTimeTakenTimeTv.text
                        // 만약 mood 정보가 있으면 selectedMood도 업데이트
                        selectedMood = record.todayMood ?: ""
                        binding.weeklyFeelEdittextEt.setText(record.moodDetail ?: "")
                        Toast.makeText(requireContext(), "[$recordDate] 기록을 불러왔습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("HomeWeeklyFragment", "[$recordDate] 기록 조회 실패: ${response.code()} ${response.message()}")
                    Toast.makeText(requireContext(), "[$recordDate] 기록 조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DailyRecordGetResponse>, t: Throwable) {
                Log.d("HomeWeeklyFragment", "[$recordDate] 기록 조회 에러: ${t.message}")
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
}