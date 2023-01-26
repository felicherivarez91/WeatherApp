package com.example.weatherapplication.model

data class Data(
    var latitude: Double,
    var longitude: Double,
    var current_weather: CurrentWeather,
    var location: String
    )
