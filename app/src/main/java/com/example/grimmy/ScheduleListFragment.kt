package com.example.grimmy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.grimmy.databinding.FragmentScheduleListBinding

class ScheduleListFragment : Fragment() {
    private lateinit var binding: FragmentScheduleListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleListBinding.inflate(inflater,container,false)

        binding.listBackIv.setOnClickListener(){
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }
}