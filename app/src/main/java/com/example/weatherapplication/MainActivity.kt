package com.example.weatherapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapplication.databinding.ActivityMainBinding
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var viewModel: MainActivityViewModel
    var status = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        private const val PERMISSION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(application)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        getLastLocation()

        viewModel.weatherData.observe(this) { weatherData ->
            binding?.tvLatitude?.text = weatherData.latitude.toString()
            binding?.tvLongitude?.text = weatherData.longitude.toString()
            binding?.tvTemperature?.text = weatherData.current_weather.temperature.toString()
            binding?.tvWindDirection?.text = weatherData.current_weather.winddirection.toString()
            binding?.tvWindspeed?.text = weatherData.current_weather.windspeed.toString()
            binding?.tvLocation?.text =
                viewModel.getLocation(weatherData.latitude, weatherData.longitude)
        }
        binding?.btnCurrentLocation?.setOnClickListener {
            getLastLocation()

                if (isOnline())
                {
                    //  Toast.makeText(this, "There is internet", Toast.LENGTH_LONG).show()
                    viewModel.getCurrentLocationWeather(this@MainActivity)
                    setupUI()
                } else {
                    Toast.makeText(this@MainActivity, "There is no internet", Toast.LENGTH_LONG)
                        .show()
                    setUpUIFromDatabase()
                }



        }


        // custom latitude and longitude from user
        binding?.btnCustomLocation?.setOnClickListener {

                if (isOnline()) {
                    //    Toast.makeText(this, "There is internet", Toast.LENGTH_LONG).show()
                    if (binding?.etLatitudeInput?.text.isNullOrEmpty() || binding?.etLongitudeInput?.text.isNullOrEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Please input the missing value",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else if (binding?.etLatitudeInput?.text.toString()
                            .toDouble() < -90 || binding?.etLatitudeInput?.text.toString()
                            .toDouble() > 90
                    ) {
                        Toast.makeText(
                            this@MainActivity,
                            "value of latitude must be between -90 to 90",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else if (binding?.etLongitudeInput?.text.toString()
                            .toDouble() < -180 || binding?.etLongitudeInput?.text.toString()
                            .toDouble() > 180
                    ) {
                        Toast.makeText(
                            this@MainActivity,
                            "value of longitude must be between -180 to 180",
                            Toast.LENGTH_LONG
                        )
                            .show()

                    } else {
                        viewModel.getCustomLocationWeather(
                            binding!!.etLatitudeInput.text.toString(),
                            binding!!.etLongitudeInput.text.toString()
                        )
                        setupUI()

                    }
                } else {
                    Toast.makeText(this@MainActivity, "There is no internet", Toast.LENGTH_LONG)
                        .show()
                    setUpUIFromDatabase()

                }

            getLastLocation()
        }

    }


    // adds the weather result to database
    private fun addRecord() {
           // if (isOnline()) {
                viewModel.addRecordToDB()
           // }

    }


    private fun setUpUIFromDatabase() {
        viewModel.getWeatherFromDB()
        setupUI()
    }

    private fun setupUI() {
        viewModel.weatherData.observe(this) { weatherData ->
            binding?.tvLatitude?.text = weatherData.latitude.toString()
            binding?.tvLongitude?.text = weatherData.longitude.toString()
            binding?.tvTemperature?.text = weatherData.current_weather.temperature.toString()
            binding?.tvWindDirection?.text = weatherData.current_weather.winddirection.toString()
            binding?.tvWindspeed?.text = weatherData.current_weather.windspeed.toString()
            binding?.tvLocation?.text = viewModel.getLocation(weatherData.latitude, weatherData.longitude)
        }

        // if the last record is the same or there is no internet dont  add record
            if (isOnline()) {
                addRecord()
            }


    }


    // functions to get the location and check internet-------------------

    // checks if internet is connected
    fun isOnline(): Boolean {
     //   Log.d("check internet scope", "Thread ${Thread.currentThread()}")
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    private fun checkPermission(): Boolean {
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // changed from application context to activity
    private fun requestPermission() {
        //this function will allows us to tell the user to request the necessary permission if they are not granted
        ActivityCompat.requestPermissions(
            this,
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

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
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
                        // TODO observe
                        //get last latitude and longitude used for current location
                        viewModel.longitude = location.longitude.toString()
                        viewModel.latitude = location.latitude.toString()

                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            requestPermission()

        }
    }


    private fun newLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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
                // TODO OBSERVE
                viewModel.longitude = lastLocation.longitude.toString()
                viewModel.latitude = lastLocation.latitude.toString()
            }
        }

    }


}