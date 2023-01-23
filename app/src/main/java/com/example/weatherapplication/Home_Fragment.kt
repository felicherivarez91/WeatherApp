package com.example.weatherapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.weatherapplication.databinding.FragmentHomeBinding


class Home_Fragment : Fragment() {
    var binding: FragmentHomeBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment


        binding?.btnCurrentLocation?.setOnClickListener {
            // add permission for current location
            // get current latitude and longitude


            // check if there is internet
            findNavController().navigate(R.id.action_home2_to_weatherDetails)

        }
        return binding?.root
    }

}