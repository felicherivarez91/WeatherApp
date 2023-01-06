package com.example.weatherapplication

import android.app.Application

class WeatherApp : Application() {
    val db by lazy{
        WeatherDatabase.getInstance(this)
    }
}