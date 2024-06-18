package com.alptugkafkasli.foodfusion

import DailyAppetizerFragment
import DailyBreakfastFragment
import DailyDessertFragment
import DailyMaincourseFragment
import DailyMezzeFragment
import DailySaladFragment
import DailySoupFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alptugkafkasli.foodfusion.databinding.FragmentHomeBinding


class HomeFragment : Fragment(){


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
    }
    private fun setOnClickListeners(){
        binding.dailySoupContainer.setOnClickListener(){
           navigateToFragment(DailySoupFragment())
        }
        binding.dailySaladContainer.setOnClickListener(){
            navigateToFragment(DailySaladFragment())
        }
        binding.dailyMezzeContainer.setOnClickListener(){
            navigateToFragment(DailyMezzeFragment())
        }
        binding.dailyMaincourseContainer.setOnClickListener(){
            navigateToFragment(DailyMaincourseFragment())
        }
        binding.dailyDessertContainer.setOnClickListener(){
            navigateToFragment(DailyDessertFragment())
        }
        binding.dailyBreakfastContainer.setOnClickListener(){
            navigateToFragment(DailyBreakfastFragment())
        }
        binding.dailyAppetizerContainer.setOnClickListener(){
            navigateToFragment(DailyAppetizerFragment())
        }
    }
    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }





}