package com.example.grimmy

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.grimmy.databinding.FragmentHomeMonthlyBinding
import java.util.Calendar

class HomeMonthlyFragment : Fragment() {

    private lateinit var binding : FragmentHomeMonthlyBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHomeMonthlyBinding.inflate(inflater,container,false)

        binding.monthlyDatepickerLl.setOnClickListener {
            DatePickerDialogFragment().show(parentFragmentManager, "yearmonthPicker")
        }

        return binding.root
    }

}