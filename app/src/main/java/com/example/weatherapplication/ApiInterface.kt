package com.example.weatherapplication

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

   // @GET("/v1/forecast?latitude=52.52&longitude=13.41&current_weather=true")
    @GET("/v1/forecast?latitude={latitude}&longitude={longitude}&current_weather=true")

    suspend fun getWeather(@Path("latitude") latitude:String,@Path("longitude") longitude:String): Response<Data>


}