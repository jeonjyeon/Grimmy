package com.example.grimmy

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.grimmy.Retrofit.Request.ClassAddRequest
import com.example.grimmy.Retrofit.Response.ClassAddResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.DialogAlertCustomBinding
import com.example.grimmy.databinding.FragmentScheduleAddClassBinding
import com.example.grimmy.utils.parseDayToIndex
import com.example.grimmy.utils.parseTimeToMinutes
import com.example.grimmy.viewmodel.ScheduleViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleAddClassFragment : Fragment(), StartTimePickerDialogFragment.OnTimeSetListener,
    EndTimePickerDialogFragment.OnTimeSetListener, DayPickerDialogFragment.OnDaySetListener {
    private lateinit var binding: FragmentScheduleAddClassBinding

    val startTimePickerDialog = StartTimePickerDialogFragment()
    val endTimePickerDialog = EndTimePickerDialogFragment()
    val dayPickerDialog = DayPickerDialogFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleAddClassBinding.inflate(inflater, container, false)

        binding.scheduleClassAddOkTv.setOnClickListener {
            val className = binding.scheduleAddClassNameEt.text.toString().trim()
            val classPlace = binding.scheduleAddClassPlaceEt.text.toString().trim()
            val classDay = binding.scheduleAddClassDaypickerBtnTv.text.toString()
            val startTime = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val endTime = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()

            if (className.isEmpty() || classPlace.isEmpty()) {
                showAlert("수업 명과 장소를 입력해 주세요.")
            } else {
                // 새 수업 객체 생성 (로컬 데이터 클래스: ClassSchedule)
                val newClass = ClassSchedule(className, classPlace, classDay, startTime, endTime)

                // SharedPreferences에서 userId 가져오기 (예: "user_prefs" 파일에 "userId" 키로 저장)
                val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("userId", -1)
                if(userId == -1) {
                    showAlert("유효한 사용자 정보가 없습니다.")
                    return@setOnClickListener
                }

                // 1. GET API 호출: 기존 시간표 내 수업 정보 가져오기 (scheduleId는 예시로 1)
                RetrofitClient.service.getSchedule(1).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            val responseString = response.body()?.string() ?: ""
                            Log.d("ScheduleAPI", "기존 시간표 원시 응답: $responseString")

                            try {
                                // 기존 수업 목록이 JSON 배열 형식이라고 가정
                                val gson = Gson()
                                val type = object : TypeToken<List<ClassAddResponse>>() {}.type
                                val existingClasses: List<ClassAddResponse> = gson.fromJson(responseString, type)
                                Log.d("ScheduleAPI", "기존 시간표 파싱 결과: $existingClasses")

                                // 2. 중복 수업 체크
                                if (isOverlapping(newClass, existingClasses)) {
                                    showAlert("시간표가 겹쳐 추가할 수 없습니다.")
                                } else {
                                    // 3. POST API 호출: 수업 추가 요청
                                    val request = ClassAddRequest(1, userId, className, classPlace, classDay, startTime, endTime)
                                    RetrofitClient.service.addClass(request).enqueue(object : Callback<ClassAddResponse> {
                                        override fun onResponse(
                                            call: Call<ClassAddResponse>,
                                            response: Response<ClassAddResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                val addedClass = response.body()
                                                Log.d("ScheduleAPI", "추가된 수업: $addedClass")

                                                // 추가 후 최신 시간표를 다시 조회하여 로그 출력
                                                RetrofitClient.service.getSchedule(1).enqueue(object : Callback<ResponseBody> {
                                                    override fun onResponse(
                                                        call: Call<ResponseBody>,
                                                        response: Response<ResponseBody>
                                                    ) {
                                                        if (response.isSuccessful) {
                                                            val updatedResponse = response.body()?.string() ?: ""
                                                            Log.d("ScheduleAPI", "업데이트된 시간표 응답: $updatedResponse")
                                                        } else {
                                                            Log.d("ScheduleAPI", "업데이트된 시간표 조회 실패: ${response.errorBody()?.string()}")
                                                        }
                                                    }
                                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                                        Log.e("ScheduleAPI", "업데이트된 시간표 조회 에러", t)
                                                    }
                                                })
                                                requireActivity().supportFragmentManager.popBackStack()
                                            } else {
                                                Log.d("ScheduleAPI", "수업 추가 실패: ${response.errorBody()?.string()}")
                                                showAlert("수업 추가에 실패하였습니다.")
                                            }
                                        }
                                        override fun onFailure(call: Call<ClassAddResponse>, t: Throwable) {
                                            Log.e("ScheduleAPI", "수업 추가 에러", t)
                                            showAlert("수업 추가에 실패하였습니다.")
                                        }
                                    })
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showAlert("시간표 정보를 파싱하는데 실패하였습니다.")
                            }
                        } else {
                            Log.d("ScheduleAPI", "시간표 조회 실패: ${response.errorBody()?.string()}")
                            showAlert("시간표 정보를 불러오는데 실패하였습니다.")
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("ScheduleAPI", "시간표 조회 에러", t)
                        showAlert("시간표 정보를 불러오는데 실패하였습니다.")
                    }
                })
            }
        }

        binding.scheduleAddClassDayLl.setOnClickListener {
            val currentDay = binding.scheduleAddClassDaypickerBtnTv.text.toString()
            dayPickerDialog.setInitialDay(currentDay)
            dayPickerDialog.setOnDaySetListener(this)
            dayPickerDialog.show(parentFragmentManager, "dayPicker")
        }

        binding.scheduleAddClassStartTimeLl.setOnClickListener {
            val currentTime = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val parts = currentTime.split(":")
            val currentHour = parts[0].toInt()
            val currentMinute = parts[1].toInt()

            val args = Bundle().apply {
                putInt("initialHour", currentHour)
                putInt("initialMinute", currentMinute)
            }
            startTimePickerDialog.arguments = args
            startTimePickerDialog.setOnTimeSetListener(this)
            startTimePickerDialog.show(parentFragmentManager, "startTimePicker")
        }

        binding.scheduleAddClassEndTimeLl.setOnClickListener {
            val currentTime = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()
            val parts = currentTime.split(":")
            val currentHour = parts[0].toInt()
            val currentMinute = parts[1].toInt()

            val args = Bundle().apply {
                putInt("initialHour", currentHour)
                putInt("initialMinute", currentMinute)
            }
            endTimePickerDialog.arguments = args
            endTimePickerDialog.setOnTimeSetListener(this)
            endTimePickerDialog.show(parentFragmentManager, "startTimePicker")
        }

        return binding.root
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogBinding = DialogAlertCustomBinding.inflate(layoutInflater) // 커스텀 레이아웃의 ViewBinding 생성
        builder.setView(dialogBinding.root)
        // 다이얼로그의 배경을 투명하게 설정
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // ViewBinding을 사용하여 TextView와 Button에 접근
        dialogBinding.alertDialogMessageTv.text = message
        dialogBinding.alertDialogBtnTv.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    // 기존의 수업과 1분이라도 겹치는지 API로 받은 수업 리스트로 확인
    private fun isOverlapping(newClass: ClassSchedule, existingClasses: List<ClassAddResponse>): Boolean {
        val newStartTime = parseTimeToMinutes(newClass.startTime)
        val newEndTime = parseTimeToMinutes(newClass.endTime)
        val newDayIndex = parseDayToIndex(newClass.day)

        for (existing in existingClasses) {
            if (parseDayToIndex(existing.day) == newDayIndex) { // 같은 요일이면
                val existingStart = parseTimeToMinutes(existing.startTime)
                val existingEnd = parseTimeToMinutes(existing.endTime)
                // 1분이라도 겹치면 true 반환
                if (!(newEndTime <= existingStart || newStartTime >= existingEnd)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showBottomNav()
    }

    override fun onTimeSet(hour: Int, minute: Int) {
        // 선택한 시간(분 단위)
        val chosenTimeInMinutes = hour * 60 + minute

        if (startTimePickerDialog.isVisible) {
            // 시작 시간 텍스트 업데이트
            binding.scheduleAddClassStartTimepickerBtnTv.text = String.format("%02d:%02d", hour, minute)

            // 현재 설정된 종료 시간 파싱
            val currentEndTimeStr = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()
            val parts = currentEndTimeStr.split(":")
            if (parts.size == 2) {
                val endHour = parts[0].toIntOrNull() ?: hour
                val endMinute = parts[1].toIntOrNull() ?: minute
                val currentEndTimeInMinutes = endHour * 60 + endMinute

                // 시작 시간이 종료 시간보다 크면 종료 시간을 시작 시간 + 1시간으로 설정 (24시간 형식 고려)
                if (chosenTimeInMinutes >= currentEndTimeInMinutes) {
                    val newEndHour = (hour + 1) % 24
                    binding.scheduleAddClassEndTimepickerBtnTv.text = String.format("%02d:%02d", newEndHour, minute)
                }
            }
        } else if (endTimePickerDialog.isVisible) {
            // 종료 시간 텍스트 업데이트
            binding.scheduleAddClassEndTimepickerBtnTv.text = String.format("%02d:%02d", hour, minute)

            // 현재 설정된 시작 시간 파싱
            val currentStartTimeStr = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val parts = currentStartTimeStr.split(":")
            if (parts.size == 2) {
                val startHour = parts[0].toIntOrNull() ?: hour
                val startMinute = parts[1].toIntOrNull() ?: minute
                val currentStartTimeInMinutes = startHour * 60 + startMinute

                // 종료 시간이 시작 시간보다 작으면 시작 시간을 종료 시간 - 1시간으로 설정 (24시간 형식 고려)
                if (chosenTimeInMinutes <= currentStartTimeInMinutes) {
                    val newStartHour = (hour + 23) % 24  // (hour - 1) mod 24
                    binding.scheduleAddClassStartTimepickerBtnTv.text = String.format("%02d:%02d", newStartHour, minute)
                }
            }
        }
    }


    override fun onDaySet(day: String) {
        binding.scheduleAddClassDaypickerBtnTv.text = day
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}