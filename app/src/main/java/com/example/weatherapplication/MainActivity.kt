package com.example.weatherapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var viewModel: MainActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.getLastLocation(this)

        binding?.btnCurrentLocation?.setOnClickListener {
            if (viewModel.isOnline()) {
              //  Toast.makeText(this, "There is internet", Toast.LENGTH_LONG).show()
                viewModel.getCurrentLocationWeather(this)
                setupUI()
            } else {
                Toast.makeText(this, "There is no internet", Toast.LENGTH_LONG).show()
                setUpUIFromDatabase()
            }

        }


        // custom latitude and longitude from user
        binding?.btnCustomLocation?.setOnClickListener {
            if (viewModel.isOnline()) {
            //    Toast.makeText(this, "There is internet", Toast.LENGTH_LONG).show()
                if (binding?.etLatitudeInput?.text.isNullOrEmpty() || binding?.etLongitudeInput?.text.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        "Please input the missing value",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                 if(binding?.etLatitudeInput?.text.toString().toDouble()<-90 || binding?.etLatitudeInput?.text.toString().toDouble() > 90) {
                    Toast.makeText(
                        this,
                        "value of latitude must be between -90 to 90",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }else if(binding?.etLongitudeInput?.text.toString().toDouble()<-180 || binding?.etLongitudeInput?.text.toString().toDouble() > 180) {
                    Toast.makeText(
                        this,
                        "value of longitude must be between -180 to 180",
                        Toast.LENGTH_LONG
                    )
                        .show()

                } else {
                    viewModel.getCustomLocationWeather(this,
                        binding!!.etLatitudeInput.text.toString(),
                        binding!!.etLongitudeInput.text.toString()
                    )
                    setupUI()

                }
            } else {
                Toast.makeText(this, "There is no internet", Toast.LENGTH_LONG).show()
                viewModel.getWeatherFromDB()
                setupUI()

            }


        }

        viewModel.weatherData.observe(this) { weatherData ->
            binding?.tvLatitude?.text = weatherData.latitude.toString()
            binding?.tvLongitude?.text = weatherData.longitude.toString()
            binding?.tvTemperature?.text = weatherData.current_weather.temperature.toString()
            binding?.tvWindDirection?.text = weatherData.current_weather.winddirection.toString()
            binding?.tvWindspeed?.text = weatherData.current_weather.windspeed.toString()
            binding?.tvLocation?.text = viewModel.getLocation(weatherData.latitude, weatherData.longitude)

        }
    }


    // adds the weather result to database
    private fun addRecord() {

        lifecycleScope.launch {
            Log.d("saving to db scope", "Thread ${Thread.currentThread()}")

            if (!viewModel.addRecordToDB()) {
                Toast.makeText(
                    applicationContext,
                    "failed to save entry",
                    Toast.LENGTH_LONG
                ).show()
        //        Toast.makeText(applicationContext, "Record Saved", Toast.LENGTH_LONG)
        //            .show()
       //     } else {
        //        Toast.makeText(
           //         applicationContext,
          //          "failed to save entry",
           //         Toast.LENGTH_LONG
           //     )
           //         .show()
            }

        }
    }


    private fun setUpUIFromDatabase() {
        viewModel.getWeatherFromDB()
        setupUI()
    }

    private fun setupUI() {
        viewModel.weatherData.observe(this) { weatherData ->
            binding?.tvLatitude?.text = weatherData.latitude.toString()
            binding?.tvLongitude?.text = weatherData.longitude.toString()
            binding?.tvTemperature?.text = weatherData.current_weather.temperature.toString()
            binding?.tvWindDirection?.text = weatherData.current_weather.winddirection.toString()
            binding?.tvWindspeed?.text = weatherData.current_weather.windspeed.toString()
            binding?.tvLocation?.text = viewModel.getLocation(weatherData.latitude, weatherData.longitude)

            // if the last record is the same or there is no internet dont  add record
            if (viewModel.isOnline()) {
                addRecord()
            }
        }

    }

}