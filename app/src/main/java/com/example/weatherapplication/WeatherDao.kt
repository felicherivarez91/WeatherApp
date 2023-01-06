package com.example.weatherapplication

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    // data access object

    @Insert
    suspend fun insert(weatherEntity: WeatherEntity)

    @Update
    suspend fun update (weatherEntity: WeatherEntity)

    @Delete
    suspend fun delete(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM `weather-table`")
    fun fetchAllWeather(): Flow<List<WeatherEntity>>

    @Query("SELECT * FROM `weather-table` WHERE id=:id")
    fun fetchWeatherById(id: Int): Flow<WeatherEntity>

    @Query("SELECT * FROM `weather-table` ORDER BY id DESC LIMIT 1")
    fun getLast(): Flow<WeatherEntity>

}