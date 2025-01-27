package com.example.grimmy

import android.app.DatePickerDialog
    import android.content.Context
    import android.os.Bundle
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import com.example.grimmy.databinding.FragmentHomeMonthlyBinding
    import java.util.Calendar

class HomeMonthlyFragment : Fragment(), DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var binding: FragmentHomeMonthlyBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHomeMonthlyBinding.inflate(inflater, container, false)

        // 초기 날짜 설정: 현재 년도와 월
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 +1
        displayDate(currentYear, currentMonth)

        binding.monthlyDatepickerLl.setOnClickListener {
            val pickerFragment = DatePickerDialogFragment().apply {
                listener = this@HomeMonthlyFragment
            }
            pickerFragment.show(parentFragmentManager, "yearmonthPicker")
        }

        return binding.root
    }

    override fun onDateSelected(year: Int, month: Int) {
        displayDate(year, month)
    }

    private fun displayDate(year: Int, month: Int) {
        // 여기서 월 표시는 1월부터 시작하므로 배열 인덱스 조정 없이 직접 출력
        binding.monthlyDatepickerBtnTv.text = "${year}년 ${month}월"
    }
}