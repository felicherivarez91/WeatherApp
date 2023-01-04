package com.example.weatherapplication

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {

    fun getClient(): Retrofit {
    var mHttpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    var mOkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(mHttpLoggingInterceptor)
        .build()


    var retrofit: Retrofit = retrofit2.Retrofit.Builder()
        .baseUrl(Constant.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(mOkHttpClient)
        .build()
    return retrofit
}
}