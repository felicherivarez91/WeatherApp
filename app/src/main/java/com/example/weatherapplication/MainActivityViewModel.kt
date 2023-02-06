package com.example.weatherapplication


import android.app.Activity
import android.app.Application

import android.location.Geocoder

import android.util.Log
import androidx.lifecycle.*
import com.example.weatherapplication.model.CurrentWeather
import com.example.weatherapplication.model.Data
import com.example.weatherapplication.retrofit.ApiInterface
import com.example.weatherapplication.retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class MainActivityViewModel(application: Application) :
    AndroidViewModel(application) {
    private val weatherDao = (application as WeatherApp).db.employeeDao()
    private val _weatherData = MutableLiveData<Data>()
    val weatherData: LiveData<Data>
        get() = _weatherData
    var latitude: String = ""
    var longitude: String = ""
    private var location: String = ""
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // gets weather from api json
    private fun getWeatherData() {

        val retrofit = RetrofitClient().getClient()
        val apiInterface = retrofit.create(ApiInterface::class.java)

        // viewModelScope.launch(Dispatchers.IO) {
        coroutineScope.launch {
            try {

                val response = apiInterface.getWeather(latitude, longitude, true)
                Log.d("response api scope", "Thread ${Thread.currentThread()}")

                if (response.isSuccessful) {
                    //your code for handling success response
                    withContext(Dispatchers.Main) {
                        _weatherData.value = response.body()!!
                        Log.d("weather assign scope", "Thread ${Thread.currentThread()}")
                    }

                } else {

                    Log.e("ERROR fetching data", "Could not fetch weather data")
                }
            } catch (Ex: Exception) {
                Log.e("Error", Ex.localizedMessage!!)
            }
        }
    }


    // method to execute functions when current location button clicked


    fun getCurrentLocationWeather(activity: Activity) {
        //gets the current location and saves the lat and long
        //  getLastLocation(activity)

        if (latitude.isNotBlank() || longitude.isNotBlank()) {
            location = getLocation(latitude.toDouble(), longitude.toDouble())
            //  retrieves the data
            getWeatherData()
        }
    }

    fun getCustomLocationWeather(latitude: String, longitude: String) {
        this.latitude = latitude
        this.longitude = longitude
        location = getLocation(latitude.toDouble(), longitude.toDouble())
        // check if there is internet -> ask user to choose coordinates or current location
        //    if (isOnline()) {
        //  retrieves the data
        getWeatherData()

        // }

        //  getLastLocation(activity)

    }

    fun getWeatherFromDB() {
        // viewModelScope.launch(Dispatchers.Main) {
        coroutineScope.launch {
         //   Log.d(" get db  scope", "Thread ${Thread.currentThread()}")

            weatherDao.getLast().collect {
                val lastRecord = it
                withContext(Dispatchers.Main) {
                    Log.d("weatherdao lastrecord","${lastRecord.latitude}")
                    latitude = lastRecord.latitude.toString()
                    longitude = lastRecord.longitude.toString()
                }
                val weatherData = Data(
                    latitude = lastRecord.latitude,
                    longitude = lastRecord.longitude,
                    current_weather = CurrentWeather(
                        temperature = lastRecord.temperature,
                        winddirection = lastRecord.winddirection,
                        windspeed = lastRecord.windspeed
                    )
                )
                withContext(Dispatchers.Main) {

                    _weatherData.value = weatherData
                    Log.d("weatherdata","${weatherData.latitude}")

                }
            }
        }

    }

    fun addRecordToDB(): Boolean {

        var success = false
        try {
            Log.i("database", "${weatherData.value?.latitude}")
            if (_weatherData.value != null) {

                coroutineScope.launch {
               //     Log.d("insert db scope", "Thread ${Thread.currentThread()}")
                    // compare the last entry with the record
                    // if is not a duplicate then add entry
                    /* var isNotDuplicate = true
                     weatherDao.getLast().collect {
                         val lastRecord = it
                        if(lastRecord!=null) {
                            if (
                                lastRecord.latitude == weatherData.value!!.latitude &&
                                lastRecord.longitude == weatherData.value!!.longitude &&
                                lastRecord.temperature == weatherData.value!!.current_weather.temperature &&
                                lastRecord.winddirection == weatherData.value!!.current_weather.winddirection &&
                                lastRecord.windspeed == weatherData.value!!.current_weather.windspeed
                            ) {
                                isNotDuplicate = false
                            }
                        }
                     }*/
                    //      if (isNotDuplicate) {
                    weatherDao.insert(
                        WeatherEntity(
                            latitude = _weatherData.value!!.latitude,
                            longitude = _weatherData.value!!.longitude,
                            temperature = _weatherData.value!!.current_weather.temperature,
                            winddirection = _weatherData.value!!.current_weather.winddirection,
                            windspeed = _weatherData.value!!.current_weather.windspeed,
                            id = 0,
                            location = getLocation(
                                _weatherData.value!!.latitude,
                                _weatherData.value!!.longitude
                            )
                        )
                    )

                   success = true
                }
                //    }
                return success
            } else {
                Log.d("database", "Error inserting data into the database or there is a duplicate")

                return success
            }
        } catch (e: Exception) {
            Log.e("addRecordToDB", "Error inserting data into the database: $e")
            return false
        }
    }


    fun getLocation(lat: Double, long: Double): String {

        location = ""
        val geocoder = Geocoder(getApplication(), Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(lat, long, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                location =
                    "${address.getAddressLine(0)}\n${address.locality}, ${address.postalCode}\n${address.countryName}"
            }
        } catch (e: IOException) {
            // Handle IOException
            location = "Could not find location information for the provided coordinates"
        }
        if (location.isNullOrBlank()) {
            location = "Could not find location information for the provided coordinates"
        }
        return location
    }


}
