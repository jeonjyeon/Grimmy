package com.example.grimmy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.grimmy.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private var test_state : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater,container,false)

        childFragmentManager.beginTransaction().replace(R.id.home_frame,HomeMonthlyFragment()).commit()

        binding.homeMonthlySelectedBtnIv.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.home_frame,HomeWeeklyFragment()).commit()
            test_state = true
            binding.homeWeeklySelectedBtnIv.visibility = View.VISIBLE
            binding.homeMonthlySelectedBtnIv.visibility = View.GONE
        }

        binding.homeWeeklySelectedBtnIv.setOnClickListener {
            childFragmentManager.beginTransaction().replace(R.id.home_frame,HomeMonthlyFragment()).commit()
            test_state = false
            binding.homeWeeklySelectedBtnIv.visibility = View.GONE
            binding.homeMonthlySelectedBtnIv.visibility = View.VISIBLE
        }

        return binding.root
    }

}