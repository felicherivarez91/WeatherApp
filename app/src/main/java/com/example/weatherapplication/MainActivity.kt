package com.example.weatherapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

        viewModel.latitude = "13.62"
        viewModel.longitude = "24.75"


     /* //  val weatherData = getWeather(latitude,longitude)

        binding?.tvLatitude?.text = weatherData.latitude.toString()
        binding?.tvLongitude?.text = weatherData.longitude.toString()
        binding?.tvTemperature?.text = weatherData.current_weather.temperature.toString()
        binding?.tvWindDirection?.text = weatherData.current_weather.winddirection.toString()
        binding?.tvWindspeed?.text = weatherData.current_weather.windspeed.toString()*/




    }


    private fun getWeather(latitude: String, longitude: String): Data {
        var data: Data = Data(0.0,0.0,CurrentWeather(0.0,0.0,0))

        var retrofit = RetrofitClient().getClient()
        var apiInterface = retrofit.create(ApiInterface::class.java)

       // lifecycleScope.launchWhenCreated {
            try {

                val response = apiInterface.getWeather(latitude,longitude,true)
                if (response.isSuccessful()) {
                    //your code for handling success response
                    data = response.body()!!

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        response.errorBody().toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }catch (Ex:Exception){
                Log.e("Error",Ex.localizedMessage)
            }
       // }
        return data
    }
}