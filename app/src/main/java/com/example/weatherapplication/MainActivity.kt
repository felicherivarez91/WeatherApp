package com.example.weatherapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherapplication.databinding.ActivityMainBinding
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // will be changed to have the user enter the values or current location
        viewModel.latitude = "10"
        viewModel.longitude = "20"

        // check if there is internet -> ask user to choose coordinates or current location

        // retrieves the data
        viewModel.getWeatherData(viewModel.latitude, viewModel.longitude)
        // sets the ui
        viewModel.weatherData.observe(this, Observer { weatherData ->
            binding?.tvLatitude?.text = weatherData.latitude.toString()
            binding?.tvLongitude?.text = weatherData.longitude.toString()
            binding?.tvTemperature?.text = weatherData.current_weather.temperature.toString()
            binding?.tvWindDirection?.text = weatherData.current_weather.winddirection.toString()
            binding?.tvWindspeed?.text = weatherData.current_weather.windspeed.toString()})


        //if no internet -> display the last data from room database

    }


}