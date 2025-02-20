package com.example.grimmy

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.grimmy.Retrofit.Response.ClassAddResponse
import com.example.grimmy.Retrofit.Response.GetScheduleResponse
import com.example.grimmy.Retrofit.RetrofitClient
import com.example.grimmy.databinding.FragmentBottomSheetClassBinding
import com.example.grimmy.viewmodel.ScheduleViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClassBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetClassBinding // bottom_sheet_class_detail.xml 의 바인딩
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomSheetClassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 전달받은 scheduleDetailId 가져오기
        val detailId = arguments?.getInt("scheduleDetailId") ?: return

        // GET API 호출 (경로상의 파라미터 이름 확인)
        RetrofitClient.service.getScheduleDetail(detailId).enqueue(object :
            Callback<ClassAddResponse> {
            override fun onResponse(call: Call<ClassAddResponse>, response: Response<ClassAddResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val detail = response.body()!!
                    // XML에 정의된 뷰에 상세 정보를 표시
                    binding.classTitleTv.text = detail.title
                    binding.classDay.text = detail.day
                    binding.classPlaceTv.text = detail.location
                    binding.classStartTimeTv.text = detail.startTime
                    binding.classEndTimeTv.text = detail.endTime

                    binding.classDeleteCl.setOnClickListener {
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setMessage("'${binding.classTitleTv.text}' 수업을 삭제하시겠습니까?")
                            .setNegativeButton("취소") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setPositiveButton("삭제") { dialog, _ ->
                                // DELETE API 호출
                                RetrofitClient.service.deleteClass(detailId)
                                    .enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Toast.makeText(context, "수업이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                                // 삭제 후 최신 시간표 업데이트 (메소드 호출)
                                                updateScheduleAfterDeletion()
                                            } else {
                                                Toast.makeText(context, "삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            }
                            .create()
                            .show()
                    }
                    // 여기서 수정 버튼을 추가합니다.
                    binding.classEditCl.setOnClickListener {
                        // 수정 모드로 ScheduleAddClassFragment 실행
                        val bundle = Bundle().apply {
                            putBoolean("editMode", true)
                            putInt("scheduleDetailId", detailId)
                        }
                        dismiss() // 바텀시트 닫기
                        val fragment = ScheduleAddClassFragment()
                        fragment.arguments = bundle
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.schedule_frame, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                } else {
                    Toast.makeText(context, "수업 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
            override fun onFailure(call: Call<ClassAddResponse>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        })
    }

    // 최신 시간표 데이터를 업데이트하는 메소드
    private fun updateScheduleAfterDeletion() {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)
        val year = 2025 // 필요에 따라 동적으로 처리 가능

        RetrofitClient.service.getSchedule(userId, year)
            .enqueue(object : Callback<GetScheduleResponse> {
                override fun onResponse(call: Call<GetScheduleResponse>, response: Response<GetScheduleResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val scheduleDetails = response.body()?.details ?: emptyList()
                        scheduleViewModel.setClassSchedules(scheduleDetails)
                    } else {
                        Log.e("ScheduleAPI", "삭제 후 시간표 불러오기 실패: ${response.errorBody()?.string()}")
                    }
                    // 업데이트 후 바텀시트 닫기
                    dismiss()
                }
                override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
                    Log.e("ScheduleAPI", "삭제 후 시간표 조회 API 실패", t)
                    dismiss()
                }
            })
    }
}
