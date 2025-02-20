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
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.grimmy.Retrofit.Request.DailyRecordGetRequest
import com.example.grimmy.Retrofit.Request.DailyRecordSaveRequest
import com.example.grimmy.Retrofit.Response.DailyCommentGetResponse
import com.example.grimmy.Retrofit.Response.DailyRecordGetResponse
import com.example.grimmy.Retrofit.Response.DailyRecordSaveResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentHomeWeeklyBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
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

    // SharedPreferences와 user_id를 늦은 초기화로 선언
    private lateinit var sharedPref: android.content.SharedPreferences
    private var user_id: Int = 0

    private var drawingUrl: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Context가 attach된 이후에 초기화
        sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        user_id = sharedPref.getInt("userId", 0)

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
            saveRecordForDate(parseDate(currentSelectedDate))
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CUSTOM_GALLERY && resultCode == Activity.RESULT_OK) {
            val selectedImages = data?.getParcelableArrayListExtra<Uri>("selectedImages")
            val imageUrl = data?.getStringExtra("imageUrl")
            if (!imageUrl.isNullOrEmpty()) {
                drawingUrl = imageUrl
            }
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

        // 예시로 dailyId는 1로 사용 (실제 사용시 해당 날짜의 daily record id로 대체)
        val dailyId = 1
        val adapter = DrawingPagerAdapter(selectedImages, dailyId)
        viewPager.adapter = adapter

        dotsIndicator.setViewPager2(viewPager)
        viewPager.visibility = View.VISIBLE
        dotsIndicator.visibility = View.VISIBLE
        placeholder.visibility = View.GONE

//        loadDailyComments(dailyId, adapter)
    }

//    private fun loadDailyComments(dailyId: Int, adapter: DrawingPagerAdapter) {
//        RetrofitClient.service.getDailyComment(dailyId).enqueue(object : Callback<List<DailyCommentGetResponse>> {
//            override fun onResponse(call: Call<List<DailyCommentGetResponse>>, response: Response<List<DailyCommentGetResponse>>) {
//                if (response.isSuccessful) {
//                    val commentResponses = response.body() ?: emptyList()
//                    val commentList = commentResponses.map { resp ->
//                        DrawingPagerAdapter.Comment(
//                            x = resp.x,
//                            y = resp.y,
//                            title = resp.title,
//                            content = resp.content
//                        )
//                    }
//                    adapter.updateComments(commentList)
//                    Log.d("HomeWeeklyFragment", "코멘트 조회 성공: ${commentList.size}개")
//                } else {
//                    Log.d("HomeWeeklyFragment", "코멘트 조회 실패: ${response.code()} ${response.message()}")
//                }
//            }
//            override fun onFailure(call: Call<List<DailyCommentGetResponse>>, t: Throwable) {
//                Log.d("HomeWeeklyFragment", "코멘트 조회 오류: ${t.message}")
//            }
//        })
//    }

    // --- 달력 관련 메소드 ---
    private fun adjustToStartOfWeek() {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -1)
        }
    }

    private fun updateCalendarWeek() {
        binding.weeklyCalendarGl.removeAllViews()
        // 화면 전체 너비와 GridLayout의 좌우 padding을 고려하여 가용 너비 계산
        val screenWidth = resources.displayMetrics.widthPixels
        val leftPadding = binding.weeklyCalendarGl.paddingLeft
        val rightPadding = binding.weeklyCalendarGl.paddingRight
        val availableWidth = screenWidth - (leftPadding + rightPadding)
        val cellWidth = availableWidth / 7

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

            val layoutRes = when {
                currentDayStr == todayStr -> R.layout.item_calendar_today
                currentDayStr == currentSelectedDate -> R.layout.item_calendar_today
                else -> R.layout.item_calendar_day
            }
            val dayView = layoutInflater.inflate(layoutRes, binding.weeklyCalendarGl, false) as ConstraintLayout
            val textView = dayView.findViewById<TextView>(R.id.item_calendar_day_tv)
            textView.text = "${currentDayCal.get(Calendar.DAY_OF_MONTH)}"

            if (currentDayCal.after(todayCal)) {
                dayView.isClickable = false
                dayView.alpha = 0.5f
            } else {
                dayView.setOnClickListener {
                    saveRecordForDate(parseDate(currentSelectedDate))
                    currentSelectedDate = currentDayStr
                    loadRecordForDate(currentDayStr)
                    updateCalendarWeek()
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = cellWidth
                height = GridLayout.LayoutParams.WRAP_CONTENT
                bottomMargin = (10 * resources.displayMetrics.density).toInt()
            }
            dayView.layoutParams = params

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
        val createdAt = savedCreatedAt ?: getCurrentDateTime().also { savedCreatedAt = it }
        val updatedAt = getCurrentDateTime()
        val todayMood = selectedMood

        // UI에서 입력받은 나머지 값들
        val feedback = binding.weeklyFeedbackEdittextEt.text.toString()
        val difficultIssue = binding.weeklyHardEdittextEt.text.toString()
        val goodIssue = binding.weeklyGoodEdittextEt.text.toString()
        val drawingTime = binding.weeklyTimeTakenTimeTv.text.toString()
        val moodDetail = binding.weeklyFeelEdittextEt.text.toString()
        val question = binding.weeklyQuestionEdittextEt.text.toString()

        val drawingValue = drawingUrl ?: ""

        val recordRequest = DailyRecordSaveRequest(
            userId = user_id,
            dailyDayRecording = recordDate,
            drawing = drawingValue,
            drawingTime = drawingTime,
            feedback = feedback,
            difficultIssue = difficultIssue,
            goodIssue = goodIssue,
            todayMood = todayMood,
            moodDetail = moodDetail,
            question = question,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // JSON 문자열로 변환 후 RequestBody 생성
        // GsonBuilder로 날짜 형식을 지정하여 JSON 문자열로 변환
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
        val jsonString = gson.toJson(recordRequest)

        // 이미지 파일이 선택된 경우 파일로 변환
        if (selectedImageUri != null) {
            val file: File? = getFileFromUri(selectedImageUri!!)
            if (file != null) {
                val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
                // "drawing"은 서버에서 인식하는 파라미터 이름입니다.
                val drawingPart = MultipartBody.Part.createFormData("drawing", file.name, requestFile)

                Log.d("HomeWeeklyFragment",recordRequest.toString())
                // API 호출
                RetrofitClient.service.postDailyRecordSave(recordRequest)
                    .enqueue(object : Callback<DailyRecordSaveResponse> {
                        override fun onResponse(
                            call: Call<DailyRecordSaveResponse>,
                            response: Response<DailyRecordSaveResponse>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("HomeWeeklyFragment", "[$recordDate] 기록 자동 저장 성공")
                                Toast.makeText(requireContext(), "[$recordDate] 자동 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("HomeWeeklyFragment", "기록 저장 실패: ${response.code()} ${response.message()}")
                                Log.d("HomeWeeklyFragment",response.body().toString())
                            }
                        }
                        override fun onFailure(call: Call<DailyRecordSaveResponse>, t: Throwable) {
                            Log.e("HomeWeeklyFragment", "기록 저장 에러: ${t.message}")
                        }
                    })
            } else {
                Toast.makeText(requireContext(), "이미지 파일 변환 실패", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 이미지가 선택되지 않은 경우, 빈 문자열로 전송 (빈 MultipartBody.Part 생성)
            val emptyRequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
            val emptyDrawingPart = MultipartBody.Part.createFormData("drawing", "", emptyRequestBody)
            RetrofitClient.service.postDailyRecordSave(recordRequest)
                .enqueue(object : Callback<DailyRecordSaveResponse> {
                    override fun onResponse(
                        call: Call<DailyRecordSaveResponse>,
                        response: Response<DailyRecordSaveResponse>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("HomeWeeklyFragment", "[$recordDate] 기록 자동 저장 성공")
                            Toast.makeText(requireContext(), "[$recordDate] 자동 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("HomeWeeklyFragment", "기록 저장 실패: ${response.code()} ${response.message()}")
                        }
                    }
                    override fun onFailure(call: Call<DailyRecordSaveResponse>, t: Throwable) {
                        Log.e("HomeWeeklyFragment", "기록 저장 에러: ${t.message}")
                    }
                })
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "temp_image.png")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            outputStream.close()
            inputStream?.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadRecordForDate(recordDate: String) {
        if (!isAdded) { // 프래그먼트가 detach된 경우 요청을 보내지 않음
            Log.d("HomeWeeklyFragment", "[$recordDate] Fragment가 attach되지 않음. 요청 생략")
            return
        }

        RetrofitClient.service.getDailyRecordGet(userId = user_id, date = recordDate).enqueue(object : Callback<DailyRecordGetResponse> {
            override fun onResponse(call: Call<DailyRecordGetResponse>, response: Response<DailyRecordGetResponse>) {
                if (!isAdded) return // ✅ 응답이 왔을 때 프래그먼트가 detach되었는지 다시 확인
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

                        // ✅ Toast 메시지 - context가 null일 경우 실행 안 함
                        val safeContext = context ?: return
                        Toast.makeText(safeContext, "[$recordDate] 기록을 불러왔습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("HomeWeeklyFragment", "[$recordDate] 기록 조회 실패: ${response.code()} ${response.message()}")

                    val safeContext = context ?: return
                    Toast.makeText(safeContext, "[$recordDate] 기록 조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DailyRecordGetResponse>, t: Throwable) {
                if (!isAdded) {
                    Log.d("HomeWeeklyFragment", "[$recordDate] Fragment가 detach 상태임. UI 업데이트 생략")
                    return
                }
                Log.d("HomeWeeklyFragment", "[$recordDate] 기록 조회 에러: ${t.message}")

                // 프래그먼트가 attach된 경우에만 Toast 실행
                val safeContext = context ?: return
                Toast.makeText(safeContext, "[$recordDate] 기록 조회 에러", Toast.LENGTH_SHORT).show()
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