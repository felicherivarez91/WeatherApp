package com.example.weatherapplication

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "weather-table")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Int,
    val location: String
)
