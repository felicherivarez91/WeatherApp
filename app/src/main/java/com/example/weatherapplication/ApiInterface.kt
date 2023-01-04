package com.example.weatherapplication

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

   // @GET("/v1/forecast?latitude=52.52&longitude=13.41&current_weather=true")
    @GET("/v1/forecast?")
    suspend fun getWeather(@Query("latitude") latitude:String,@Query("longitude") longitude:String, @Query("current_weather") current_weather:Boolean ): Response<Data>

}