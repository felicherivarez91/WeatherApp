package com.example.weatherapplication

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class MainActivityViewModel :ViewModel() {
    lateinit var weatherData : Data
    var latitude : String = ""
    var longitude : String = ""
    var viewModelStore by viewModelScope()
    private fun getWeather(latitude: String, longitude: String): Data {

        var retrofit = RetrofitClient().getClient()
        var apiInterface = retrofit.create(ApiInterface::class.java)
         viewModelScope {
        try {

            val response = apiInterface.getWeather(latitude,longitude,true)
//            if (response.isSuccessful()) {
                //your code for handling success response
                weatherData = response.body()!!
//            } else {
//                Toast.makeText(
//                    this@MainActivityViewModel,
//                    response.errorBody().toString(),
//                    Toast.LENGTH_LONG
//                ).show()
//            }
        }catch (Ex:Exception){
            Log.e("Error",Ex.localizedMessage)
        }
         }
        return weatherData
    }



}