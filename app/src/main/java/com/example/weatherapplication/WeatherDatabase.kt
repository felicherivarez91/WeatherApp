package com.example.weatherapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.InternalCoroutinesApi


@Database(entities = [WeatherEntity::class], version = 2)
abstract class WeatherDatabase: RoomDatabase(){

    abstract fun employeeDao():WeatherDao
    companion object{
        @Volatile
        private var INSTRANCE: WeatherDatabase?=null

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): WeatherDatabase{

            kotlinx.coroutines.internal.synchronized(this) {
                var instance = INSTRANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WeatherDatabase::class.java,
                        "weather_database"
                    ).fallbackToDestructiveMigration()
                        .build()

                    INSTRANCE = instance
                }
                return instance
            }
        }
    }
}