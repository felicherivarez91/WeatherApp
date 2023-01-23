package com.example.weatherapplication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherapplication.databinding.FragmentHomeBinding
import com.example.weatherapplication.databinding.FragmentWeatherDetailsBinding
import com.example.weatherapplication.model.CurrentWeather
import com.example.weatherapplication.model.Data
import kotlinx.coroutines.launch

class WeatherDetails_Fragment : Fragment() {
    var binding: FragmentWeatherDetailsBinding? = null
    lateinit var viewModel: MainActivityViewModel
    lateinit var weatherDao : WeatherDao
    var status = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherDetailsBinding.inflate(inflater, container, false)

        // database
        weatherDao = (requireActivity().application as WeatherApp).db.employeeDao()

        //viewmodel
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // will be changed to have the user enter the values or current location
        viewModel.latitude = "30"
        viewModel.longitude = "80"

        // internet network receiver
        // internetReceiver = CheckInternetBroadcastReceiver()
        //checkInternet()

        // check if there is internet -> ask user to choose coordinates or current location
        if(isOnline()){
            Toast.makeText(requireContext(), "There is internet", Toast.LENGTH_LONG).show()
            //  retrieves the data
            viewModel.getWeatherData(viewModel.latitude, viewModel.longitude)

            // sets the ui
            setupUI()

            //saves data to database
            //       addRecord(weatherDao)

        }else{
            //if no internet -> display the last data from room database
            Toast.makeText(requireContext() , "There is no internet", Toast.LENGTH_LONG).show()

            setUpUIFromDatabase(weatherDao)

        }


        // Inflate the layout for this fragment
        return binding?.root
    }

    // adds the weather result to database
    fun addRecord(weatherDao: WeatherDao) {

        val latitude = binding?.tvLatitude?.text.toString().toDoubleOrNull()
        val longitude = binding?.tvLongitude?.text.toString().toDoubleOrNull()
        val temperature = binding?.tvTemperature?.text.toString().toDoubleOrNull()
        val windirection = binding?.tvWindDirection?.text.toString().toIntOrNull()
        val windspeed = binding?.tvWindspeed?.text.toString().toDoubleOrNull()
        if (latitude != null && longitude != null && temperature != null && windirection != null && windspeed != null) {
            lifecycleScope.launch {
                weatherDao.insert(
                    WeatherEntity(
                        latitude = latitude,
                        longitude = longitude,
                        temperature = temperature,
                        winddirection = windirection,
                        windspeed = windspeed, id = 0
                    )
                )
                Toast.makeText(requireContext(), "Record Saved", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "failed to save entry",
                Toast.LENGTH_LONG
            )
                .show()

        }
    }


    fun setUpUIFromDatabase(weatherDao: WeatherDao) {
        lifecycleScope.launch{
            weatherDao.getLast().collect{
                val lastRecord = it
                if (lastRecord != null) {
                    viewModel.latitude = lastRecord.latitude.toString()
                    viewModel.longitude = lastRecord.longitude.toString()
                    val weatherData = Data(latitude = lastRecord.latitude,
                        longitude = lastRecord.longitude,
                        current_weather = CurrentWeather(
                            temperature = lastRecord.temperature,
                            winddirection = lastRecord.winddirection,
                            windspeed = lastRecord.windspeed)
                    )
                    viewModel._weatherData.value = weatherData
                    setupUI()
                }
            }
        }
    }

    fun setupUI(){
        viewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            binding?.tvLatitude?.text = weatherData.latitude.toString()
            binding?.tvLongitude?.text = weatherData.longitude.toString()
            binding?.tvTemperature?.text = weatherData.current_weather.temperature.toString()
            binding?.tvWindDirection?.text = weatherData.current_weather.winddirection.toString()
            binding?.tvWindspeed?.text = weatherData.current_weather.windspeed.toString()
            // if the last record is the same or there is no internet dont  add record
            if(isOnline()){
                addRecord(weatherDao)}
        }

    }

    private fun checkInternet() {
        //   registerReceiver(internetReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }


    override fun onPause() {
        super.onPause()
        //    unregisterReceiver(internetReceiver)
    }

    // checks if internet is connected
    private fun isOnline(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    status = true
                    return status
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    status = true
                    return status
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    status = true
                    return status
                }
            }
        }
        status = false
        return status
    }

}