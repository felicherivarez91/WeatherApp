package com.example.weatherapplication

import com.google.gson.annotations.SerializedName

data class CurrentWeather (
    var temperature: Double,
    var windspeed: Double,
    var winddirection: Int
        )
