package com.example.weatherapplication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapplication.model.Data
import com.example.weatherapplication.retrofit.ApiInterface
import com.example.weatherapplication.retrofit.RetrofitClient
import kotlinx.coroutines.launch

class MainActivityViewModel :ViewModel() {
    val _weatherData = MutableLiveData<Data>()
    val weatherData: LiveData<Data>
        get() = _weatherData
    var latitude : String = ""
    var longitude : String = ""



    fun getWeatherData(latitude: String, longitude: String){

        val retrofit = RetrofitClient().getClient()
        val apiInterface = retrofit.create(ApiInterface::class.java)
         viewModelScope.launch {
        try {

            val response = apiInterface.getWeather(latitude,longitude,true)
            if (response.isSuccessful) {
                //your code for handling success response

                _weatherData.value = response.body()!!
            } else {

                Log.e("ERROR fetching data","Could not fetch weather data")
            }
        }catch (Ex:Exception){
            Log.e("Error", Ex.localizedMessage!!)
        }
         }
    }





}