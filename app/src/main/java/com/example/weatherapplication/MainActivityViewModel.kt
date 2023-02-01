package com.example.weatherapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.example.weatherapplication.model.CurrentWeather
import com.example.weatherapplication.model.Data
import com.example.weatherapplication.retrofit.ApiInterface
import com.example.weatherapplication.retrofit.RetrofitClient
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class MainActivityViewModel( application: Application) :
    AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    //val repository: WeatherRepository = WeatherRepository(weatherDao = (application as WeatherApp).db.employeeDao())
    private val weatherDao = (application as WeatherApp).db.employeeDao()
    private val _weatherData = MutableLiveData<Data>()
    val weatherData: LiveData<Data>
        get() = _weatherData

    var latitude: String = ""
    var longitude: String = ""
    private var location: String = ""
    var status = false

    private var fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    companion object {
        private const val PERMISSION_ID = 1
    }


    // gets weather from api json
    private fun getWeatherData() {

        val retrofit = RetrofitClient().getClient()
        val apiInterface = retrofit.create(ApiInterface::class.java)

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val response = apiInterface.getWeather(latitude, longitude, true)
                Log.d("reponse api scope", "Thread ${Thread.currentThread()}")

                if (response.isSuccessful) {

                    //your code for handling success response
                    withContext(Dispatchers.Main) {
                        _weatherData.value = response.body()!!
                        Log.d("weather assign scope", "Thread ${Thread.currentThread()}")

                    }

                } else {

                    Log.e("ERROR fetching data", "Could not fetch weather data")
                }
            } catch (Ex: Exception) {
                Log.e("Error", Ex.localizedMessage!!)
            }
        }
    }


    // method to execute functions when current location button clicked


    fun getCurrentLocationWeather(activity: Activity) {
        //gets the current location and saves the lat and long
        getLastLocation(activity)

        if (latitude.isNotBlank() || longitude.isNotBlank()) {
            location = getLocation(latitude.toDouble(), longitude.toDouble())
            //  retrieves the data
            getWeatherData()
        }
    }

    fun getCustomLocationWeather(activity: Activity,latitude: String, longitude: String) {
        this.latitude = latitude
        this.longitude = longitude
        location = getLocation(latitude.toDouble(), longitude.toDouble())
        // check if there is internet -> ask user to choose coordinates or current location
        if (isOnline()) {
            //  retrieves the data
            getWeatherData()

        }
        getLastLocation(activity)

    }

    fun getWeatherFromDB() {
        viewModelScope.launch(Dispatchers.Main) {
            Log.d(" get db  scope", "Thread ${Thread.currentThread()}")

            weatherDao.getLast().collect {
                val lastRecord = it
                latitude = lastRecord.latitude.toString()
                longitude = lastRecord.longitude.toString()
                val weatherData = Data(
                    latitude = lastRecord.latitude,
                    longitude = lastRecord.longitude,
                    current_weather = CurrentWeather(
                        temperature = lastRecord.temperature,
                        winddirection = lastRecord.winddirection,
                        windspeed = lastRecord.windspeed
                    )
                )
                _weatherData.value = weatherData
            }
        }

    }

    fun addRecordToDB(): Boolean {
        try {
            if (isOnline() || weatherData.value != null) {
                viewModelScope.launch {
                    Log.d("insert db scope", "Thread ${Thread.currentThread()}")

                    weatherDao.insert(
                        WeatherEntity(
                            latitude = weatherData.value!!.latitude,
                            longitude = weatherData.value!!.longitude,
                            temperature = weatherData.value!!.current_weather.temperature,
                            winddirection = weatherData.value!!.current_weather.winddirection,
                            windspeed = weatherData.value!!.current_weather.windspeed,
                            id = 0,
                            location = getLocation(weatherData.value!!.latitude,weatherData.value!!.longitude)
                        )
                    )
                }
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.e("addRecordToDB", "Error inserting data into the database: $e")
            return false
        }
    }


    // functions to get the location and check internet-------------------

    // checks if internet is connected
    fun isOnline(): Boolean {
        Log.d("check internet scope", "Thread ${Thread.currentThread()}")

        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                status = true
                return status
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                status = true
                return status
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                status = true
                return status
            }

        }
            status = false
            return status

    }


    fun getLocation(lat: Double, long: Double): String {
        location=""
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, long, 1)
            if (addresses != null && !addresses.isEmpty()) {
                val address = addresses[0]
                location =
                    "${address.getAddressLine(0)}\n${address.locality}, ${address.postalCode}\n${address.countryName}"
            }
        } catch (e: IOException) {
            // Handle IOException
            location = "Could not find location information for the provided coordinates"
        }
        if(location.isNullOrBlank()){
       location = "Could not find location information for the provided coordinates"
        }
        return location
    }


    private fun checkPermission(): Boolean {
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    // changed from application context to activity
    private fun requestPermission(activity : Activity) {
        //this function will allows us to tell the user to request the necessary permission if they are not granted
        ActivityCompat.requestPermissions(
            activity ,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    fun getLastLocation(activity: Activity) {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        newLocationData()

                    } else {
                        //get last latitude and longitude used for current location
                        longitude = location.longitude.toString()
                        latitude = location.latitude.toString()

                    }
                }
            } else {
                Toast.makeText(context, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            requestPermission(activity)

        }
    }


    private fun newLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location? = locationResult.lastLocation
            if (lastLocation != null) {
                longitude = lastLocation.longitude.toString()
                latitude = lastLocation.latitude.toString()
            }
        }

    }


}
