package com.example.weatherapplication

import com.google.gson.annotations.SerializedName

data class Data(
    var latitude: Double ,
    var longitude: Double ,
    var current_weather: CurrentWeather,

    )
