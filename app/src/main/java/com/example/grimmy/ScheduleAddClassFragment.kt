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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.example.grimmy.Retrofit.Request.ClassAddRequest
import com.example.grimmy.Retrofit.Response.ClassAddResponse
import com.example.grimmy.Retrofit.Response.GetScheduleResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.DialogAlertCustomBinding
import com.example.grimmy.databinding.FragmentScheduleAddClassBinding
import com.example.grimmy.utils.parseDayToIndex
import com.example.grimmy.utils.parseTimeToMinutes
import com.example.grimmy.viewmodel.ScheduleViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleAddClassFragment : Fragment(),
    StartTimePickerDialogFragment.OnTimeSetListener,
    EndTimePickerDialogFragment.OnTimeSetListener,
    DayPickerDialogFragment.OnDaySetListener {

    private lateinit var binding: FragmentScheduleAddClassBinding
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

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
                hideKeyboard()
                // 로컬 중복 체크용 객체 생성
                val newClass = ClassSchedule(className, classPlace, classDay, startTime, endTime)

                // SharedPreferences에서 userId 가져오기 (파일명/키는 프로젝트에 맞게)
                val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("userId", -1)
                if (userId == -1) {
                    showAlert("유효한 사용자 정보가 없습니다.")
                    return@setOnClickListener
                }

                // 예시로 2025년 사용 (필요시 동적으로 변경)
                val year = 2025

                // 1. GET API 호출: 기존 시간표 내 수업 정보 가져오기 (userId와 year 전달)
                RetrofitClient.service.getSchedule(userId, year).enqueue(object : Callback<GetScheduleResponse> {
                    override fun onResponse(
                        call: Call<GetScheduleResponse>,
                        response: Response<GetScheduleResponse>
                    ) {
                        val existingClasses = if (response.isSuccessful) {
                            response.body()?.details ?: emptyList()
                        } else if (response.code() == 404) {
                            // 404인 경우, 빈 시간표로 간주 (수업 목록 없음)
                            emptyList()
                        } else {
                            showAlert("시간표 정보를 불러오는데 실패하였습니다.")
                            return
                        }
                        Log.d("ScheduleAPI", "기존 시간표: ${response.body()}")

                        // 중복 체크
                        if (isOverlapping(newClass, existingClasses)) {
                            showAlert("시간표가 겹쳐 추가할 수 없습니다.")
                        } else {
                            // POST API 호출: 수업 추가 요청
                            val scheduleId = response.body()?.scheduleId ?: 1
                            val request = ClassAddRequest(
                                scheduleId = scheduleId,
                                userId = userId,
                                title = className,
                                location = classPlace,
                                day = classDay,
                                startTime = startTime,
                                endTime = endTime
                            )
                            RetrofitClient.service.addClass(userId, request).enqueue(object : Callback<ClassAddResponse> {
                                override fun onResponse(
                                    call: Call<ClassAddResponse>,
                                    response: Response<ClassAddResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        val addedClass = response.body()
                                        Log.d("ScheduleAPI", "추가된 수업: $addedClass")
                                        Toast.makeText(context, "수업 추가 성공", Toast.LENGTH_SHORT).show()
                                        // 수업 추가 성공 후, 최신 시간표 데이터를 GET API를 통해 받아옵니다.
                                        RetrofitClient.service.getSchedule(userId, year).enqueue(object : Callback<GetScheduleResponse> {
                                            override fun onResponse(
                                                call: Call<GetScheduleResponse>,
                                                response: Response<GetScheduleResponse>
                                            ) {
                                                if (response.isSuccessful && response.body() != null) {
                                                    val scheduleDetails = response.body()?.details ?: emptyList()
                                                    scheduleViewModel.setClassSchedules(scheduleDetails)
                                                } else {
                                                    Log.e("ScheduleAPI", "추가 후 시간표 불러오기 실패: ${response.errorBody()?.string()}")
                                                }
                                                // 데이터 업데이트 후 ScheduleFragment로 돌아갑니다.
                                                requireActivity().supportFragmentManager.popBackStack()
                                            }
                                            override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
                                                Log.e("ScheduleAPI", "추가 후 시간표 조회 API 실패", t)
                                                requireActivity().supportFragmentManager.popBackStack()
                                            }
                                        })

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
                    }
                    override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
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
        val dialogBinding = DialogAlertCustomBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.alertDialogMessageTv.text = message
        dialogBinding.alertDialogBtnTv.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // 기존 수업과 중복 체크: 기존 수업 리스트 (List<ClassAddResponse>)와 새 수업(ClassSchedule)을 비교
    private fun isOverlapping(newClass: ClassSchedule, existingClasses: List<ClassAddResponse>): Boolean {
        val newStartTime = parseTimeToMinutes(newClass.startTime)
        val newEndTime = parseTimeToMinutes(newClass.endTime)
        val newDayIndex = parseDayToIndex(newClass.day)

        for (existing in existingClasses) {
            if (parseDayToIndex(existing.day) == newDayIndex) {
                val existingStart = parseTimeToMinutes(existing.startTime)
                val existingEnd = parseTimeToMinutes(existing.endTime)
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
