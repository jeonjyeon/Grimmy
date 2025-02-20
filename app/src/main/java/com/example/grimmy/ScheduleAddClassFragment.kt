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
import com.example.grimmy.Retrofit.Request.ClassUpdateRequest
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

    // 플래그: 수정 모드 여부 (기본 false: 추가)
    private var editMode: Boolean = false
    // 수정 시 대상 수업의 detailId
    private var scheduleDetailId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // arguments를 통해 수정 모드와 detailId를 전달받았다면 저장
        arguments?.let {
            editMode = it.getBoolean("editMode", false)
            scheduleDetailId = it.getInt("scheduleDetailId", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleAddClassBinding.inflate(inflater, container, false)

        // 만약 수정 모드라면, 기존 수업 정보를 GET API로 불러와 필드에 채웁니다.
        if (editMode && scheduleDetailId != -1) {
            RetrofitClient.service.getScheduleDetail(scheduleDetailId).enqueue(object :
                Callback<ClassAddResponse> {
                override fun onResponse(call: Call<ClassAddResponse>, response: Response<ClassAddResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val detail = response.body()!!
                        binding.scheduleAddClassNameEt.setText(detail.title)
                        binding.scheduleAddClassPlaceEt.setText(detail.location)
                        binding.scheduleAddClassDaypickerBtnTv.text = detail.day
                        binding.scheduleAddClassStartTimepickerBtnTv.text = detail.startTime
                        binding.scheduleAddClassEndTimepickerBtnTv.text = detail.endTime
                    }
                }
                override fun onFailure(call: Call<ClassAddResponse>, t: Throwable) {
                    Toast.makeText(context, "수업 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

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

                val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("userId", -1)
                if (userId == -1) {
                    showAlert("유효한 사용자 정보가 없습니다.")
                    return@setOnClickListener
                }

                val year = 2025

                if (editMode && scheduleDetailId != -1) {
                    // 수정 모드: 먼저 GET API로 기존 수업들을 받아와 겹치는지 검사합니다.
                    RetrofitClient.service.getSchedule(userId, year).enqueue(object : Callback<GetScheduleResponse> {
                        override fun onResponse(call: Call<GetScheduleResponse>, response: Response<GetScheduleResponse>) {
                            if (response.isSuccessful && response.body() != null) {
                                // 현재 수정 대상 수업은 제외하고 비교합니다.
                                val existingClasses = response.body()?.details?.filter { it.scheduleDetailId != scheduleDetailId } ?: emptyList()
                                if (isOverlapping(newClass, existingClasses)) {
                                    showAlert("시간표가 겹쳐 수정할 수 없습니다.")
                                    return
                                }
                                // 겹치지 않으면 updateClass API 호출
                                val request = ClassUpdateRequest(
                                    scheduleId = 1, // 필요에 따라 기존 스케줄 ID 채워넣기
                                    userId = userId,
                                    title = className,
                                    location = classPlace,
                                    day = classDay,
                                    startTime = startTime,
                                    endTime = endTime
                                )
                                RetrofitClient.service.updateClass(scheduleDetailId, request).enqueue(object : Callback<ClassAddResponse> {
                                    override fun onResponse(call: Call<ClassAddResponse>, response: Response<ClassAddResponse>) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "수업 수정 성공", Toast.LENGTH_SHORT).show()
                                            refreshSchedule(userId, year)
                                        } else {
                                            showAlert("수업 수정에 실패하였습니다.")
                                        }
                                    }
                                    override fun onFailure(call: Call<ClassAddResponse>, t: Throwable) {
                                        showAlert("수업 수정에 실패하였습니다.")
                                    }
                                })
                            } else {
                                showAlert("시간표 정보를 불러오는데 실패하였습니다.")
                            }
                        }
                        override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
                            showAlert("시간표 정보를 불러오는데 실패하였습니다.")
                        }
                    })
                } else {
                    // 추가 모드: 기존 코드 실행 (GET 후 중복 체크, POST API 호출 등)
                    RetrofitClient.service.getSchedule(userId, year).enqueue(object : Callback<GetScheduleResponse> {
                        override fun onResponse(call: Call<GetScheduleResponse>, response: Response<GetScheduleResponse>) {
                            val existingClasses = if (response.isSuccessful) {
                                response.body()?.details ?: emptyList()
                            } else if (response.code() == 404) {
                                emptyList()
                            } else {
                                showAlert("시간표 정보를 불러오는데 실패하였습니다.")
                                return
                            }
                            Log.d("ScheduleAPI", "기존 시간표: ${response.body()}")

                            if (isOverlapping(newClass, existingClasses)) {
                                showAlert("시간표가 겹쳐 추가할 수 없습니다.")
                            } else {
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
                                    override fun onResponse(call: Call<ClassAddResponse>, response: Response<ClassAddResponse>) {
                                        if (response.isSuccessful) {
                                            Log.d("ScheduleAPI", "추가된 수업: ${response.body()}")
                                            Toast.makeText(context, "수업 추가 성공", Toast.LENGTH_SHORT).show()
                                            refreshSchedule(userId, year)
                                        } else {
                                            showAlert("수업 추가에 실패하였습니다.")
                                        }
                                    }
                                    override fun onFailure(call: Call<ClassAddResponse>, t: Throwable) {
                                        showAlert("수업 추가에 실패하였습니다.")
                                    }
                                })
                            }
                        }
                        override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
                            showAlert("시간표 정보를 불러오는데 실패하였습니다.")
                        }
                    })
                }
            }
        }

        // 타임피커 다이얼로그 호출 메소드 사용
        binding.scheduleAddClassDayLl.setOnClickListener {
            val currentDay = binding.scheduleAddClassDaypickerBtnTv.text.toString()
            dayPickerDialog.setInitialDay(currentDay)
            dayPickerDialog.setOnDaySetListener(this)
            dayPickerDialog.show(parentFragmentManager, "dayPicker")
        }

        binding.scheduleAddClassStartTimeLl.setOnClickListener {
            showTimePicker(startTimePickerDialog, binding.scheduleAddClassStartTimepickerBtnTv.text.toString(), "startTimePicker")
        }

        binding.scheduleAddClassEndTimeLl.setOnClickListener {
            showTimePicker(endTimePickerDialog, binding.scheduleAddClassEndTimepickerBtnTv.text.toString(), "endTimePicker")
        }

        return binding.root
    }

    // 최신 시간표 갱신 메소드 (위에서 설명한 refreshSchedule)
    private fun refreshSchedule(userId: Int, year: Int) {
        RetrofitClient.service.getSchedule(userId, year).enqueue(object : Callback<GetScheduleResponse> {
            override fun onResponse(call: Call<GetScheduleResponse>, response: Response<GetScheduleResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val scheduleDetails = response.body()?.details ?: emptyList()
                    scheduleViewModel.setClassSchedules(scheduleDetails)
                } else {
                    Log.e("ScheduleAPI", "추가/수정 후 시간표 불러오기 실패: ${response.errorBody()?.string()}")
                }
                requireActivity().supportFragmentManager.popBackStack()
            }
            override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
                Log.e("ScheduleAPI", "추가/수정 후 시간표 조회 API 실패", t)
                requireActivity().supportFragmentManager.popBackStack()
            }
        })
    }

    // 타임피커 다이얼로그 호출 메소드 (오버로딩)
    private fun showTimePicker(dialog: StartTimePickerDialogFragment, timeString: String, tag: String) {
        val parts = timeString.split(":")
        val currentHour = parts[0].toInt()
        val currentMinute = parts[1].toInt()
        val args = Bundle().apply {
            putInt("initialHour", currentHour)
            putInt("initialMinute", currentMinute)
        }
        dialog.arguments = args
        dialog.setOnTimeSetListener(this)
        dialog.show(parentFragmentManager, tag)
    }

    private fun showTimePicker(dialog: EndTimePickerDialogFragment, timeString: String, tag: String) {
        val parts = timeString.split(":")
        val currentHour = parts[0].toInt()
        val currentMinute = parts[1].toInt()
        val args = Bundle().apply {
            putInt("initialHour", currentHour)
            putInt("initialMinute", currentMinute)
        }
        dialog.arguments = args
        dialog.setOnTimeSetListener(this)
        dialog.show(parentFragmentManager, tag)
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
        val chosenTimeInMinutes = hour * 60 + minute
        if (startTimePickerDialog.isVisible) {
            binding.scheduleAddClassStartTimepickerBtnTv.text = String.format("%02d:%02d", hour, minute)
            val currentEndTimeStr = binding.scheduleAddClassEndTimepickerBtnTv.text.toString()
            val parts = currentEndTimeStr.split(":")
            if (parts.size == 2) {
                val endHour = parts[0].toIntOrNull() ?: hour
                val endMinute = parts[1].toIntOrNull() ?: minute
                val currentEndTimeInMinutes = endHour * 60 + endMinute
                if (chosenTimeInMinutes >= currentEndTimeInMinutes) {
                    val newEndHour = (hour + 1) % 24
                    binding.scheduleAddClassEndTimepickerBtnTv.text = String.format("%02d:%02d", newEndHour, minute)
                }
            }
        } else if (endTimePickerDialog.isVisible) {
            binding.scheduleAddClassEndTimepickerBtnTv.text = String.format("%02d:%02d", hour, minute)
            val currentStartTimeStr = binding.scheduleAddClassStartTimepickerBtnTv.text.toString()
            val parts = currentStartTimeStr.split(":")
            if (parts.size == 2) {
                val startHour = parts[0].toIntOrNull() ?: hour
                val startMinute = parts[1].toIntOrNull() ?: minute
                val currentStartTimeInMinutes = startHour * 60 + startMinute
                if (chosenTimeInMinutes <= currentStartTimeInMinutes) {
                    val newStartHour = (hour + 23) % 24
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
