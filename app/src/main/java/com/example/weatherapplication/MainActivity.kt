package com.example.weatherapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
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
import androidx.lifecycle.lifecycleScope
import com.example.weatherapplication.databinding.ActivityMainBinding
import com.example.weatherapplication.model.CurrentWeather
import com.example.weatherapplication.model.Data
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    lateinit var viewModel: MainActivityViewModel
    lateinit var weatherDao : WeatherDao
    var status = false
    var latitude : String = ""
    var longitude : String = ""
    var location : String = ""

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    companion object {
        private const val PERMISSION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        super.onCreate(savedInstanceState)

// database
        weatherDao = (application as WeatherApp).db.employeeDao()

        //viewmodel
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        binding?.btnCurrentLocation?.setOnClickListener {

            if(latitude.isNotBlank() || longitude.isNotBlank()) {

                // check if there is internet -> ask user to choose coordinates or current location
                if(isOnline()){

                    Toast.makeText(this, "There is internet", Toast.LENGTH_LONG).show()
                    //update ui with the latitude and longitude retrieved
                    viewModel.latitude = latitude
                    viewModel.longitude = longitude
                    location = getLocation(latitude.toDouble(),longitude.toDouble())
                    //  retrieves the data
                    viewModel.getWeatherData(viewModel.latitude, viewModel.longitude,location)

                    // sets the ui
                    setupUI()


                }else{
                    //if no internet -> display the last data from room database
                    Toast.makeText(this , "There is no internet", Toast.LENGTH_LONG).show()

                    setUpUIFromDatabase(weatherDao)

                }

            }
        }


        // custom latitude and longitude from user
        binding?.btnCustomLocation?.setOnClickListener {
            // check if the values are not null
            if(!binding?.etLatitudeInput?.text.isNullOrEmpty()|| !binding?.etLongitudeInput?.text.isNullOrEmpty()){
                latitude = binding?.etLatitudeInput?.text.toString()
                longitude = binding?.etLongitudeInput?.text.toString()
                viewModel.latitude = latitude
                viewModel.longitude = longitude
                location = getLocation(latitude.toDouble(),longitude.toDouble())
                // check if there is internet -> ask user to choose coordinates or current location
                if(isOnline()){
                    Toast.makeText(this, "There is internet", Toast.LENGTH_LONG).show()
                    //  retrieves the data
                    viewModel.getWeatherData(viewModel.latitude, viewModel.longitude,location)

                    // sets the ui
                    setupUI()


                }else{
                    //if no internet -> display the last data from room database
                    Toast.makeText(this , "There is no internet", Toast.LENGTH_LONG).show()

                    setUpUIFromDatabase(weatherDao)

                }

            }else{
                Toast.makeText(
                    this,
                    "Please input the missing value",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            getLastLocation()

        }

    }


    // adds the weather result to database
    private fun addRecord(weatherDao: WeatherDao) {
        val latitude = binding?.tvLatitude?.text.toString().toDoubleOrNull()
        val longitude = binding?.tvLongitude?.text.toString().toDoubleOrNull()
        val temperature = binding?.tvTemperature?.text.toString().toDoubleOrNull()
        val windirection = binding?.tvWindDirection?.text.toString().toIntOrNull()
        val windspeed = binding?.tvWindspeed?.text.toString().toDoubleOrNull()
        val location = getLocation(latitude!!,longitude!!)
        if (latitude != null && longitude != null && temperature != null && windirection != null && windspeed != null && location!=null) {
            lifecycleScope.launch {
                weatherDao.insert(
                    WeatherEntity(
                        latitude = latitude,
                        longitude = longitude,
                        temperature = temperature,
                        winddirection = windirection,
                        windspeed = windspeed, id = 0,
                        location = location
                    )
                )
                Toast.makeText(applicationContext, "Record Saved", Toast.LENGTH_LONG)
                    .show()
            }

        } else {
            Toast.makeText(
                this,
                "failed to save entry",
                Toast.LENGTH_LONG
            )
                .show()

        }
    }


    private fun setUpUIFromDatabase(weatherDao: WeatherDao) {
        lifecycleScope.launch{
            weatherDao.getLast().collect{
                val lastRecord = it
                if (lastRecord != null) {
                    viewModel.latitude = lastRecord.latitude.toString()
                    viewModel.longitude = lastRecord.longitude.toString()

                    val weatherData = Data(latitude = lastRecord.latitude,
                        longitude = lastRecord.longitude,
                        current_weather = CurrentWeather(
                            temperature = lastRecord.temperature,
                            winddirection = lastRecord.winddirection,
                            windspeed = lastRecord.windspeed),
                        location = lastRecord.location
                    )
                    viewModel._weatherData.value = weatherData
                    setupUI()
                }
            }
        }
    }

    private fun setupUI(){
            viewModel.weatherData.observe(this) { weatherData ->
            binding?.tvLatitude?.text = weatherData.latitude.toString()
            binding?.tvLongitude?.text = weatherData.longitude.toString()
            binding?.tvTemperature?.text = weatherData.current_weather.temperature.toString()
            binding?.tvWindDirection?.text = weatherData.current_weather.winddirection.toString()
            binding?.tvWindspeed?.text = weatherData.current_weather.windspeed.toString()
                binding?.tvLocation?.text = weatherData.location
                binding?.tvLocation?.text = getLocation(latitude.toDouble(), longitude.toDouble())
             // if the last record is the same or there is no internet dont  add record
            if(isOnline()){
                addRecord(weatherDao)}
        }

    }

    // checks if internet is connected
    private fun isOnline(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
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
        }
        status = false
        return status
    }

    fun getLocation(lat:Double,long:Double):String{

        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, long, 1)
            if (addresses != null && !addresses.isEmpty()) {
                val address = addresses[0]
                location = "${address.getAddressLine(0)}\n${address.locality}, ${address.postalCode}\n${address.countryName}"
            }
        } catch (e: IOException) {
            // Handle IOException
            location = "Could not find location information for the provided coordinates"
        }
        return location
    }


    fun checkPermission():Boolean{
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if(
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false
    }
    private fun requestPermission(){
        //this function will allows us to tell the user to request the necessary permission if they are not granted
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
           PERMISSION_ID
        )
    }

    fun isLocationEnabled():Boolean{
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }


    private fun getLastLocation(){
        if(checkPermission()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }

                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task->
                    val location: Location? = task.result
                    if(location == null){
                        newLocationData()

                    }else{
                        //get last latitude and longitude used for current location
                        longitude=  location.longitude.toString()
                        latitude=location.latitude.toString()

                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            requestPermission()

        }
    }


    private fun newLocationData(){
        val locationRequest =  LocationRequest()
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()
        )
    }


    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location? = locationResult.lastLocation
            if (lastLocation != null) {
                longitude=  lastLocation.longitude.toString()
                latitude=lastLocation.latitude.toString()
            }
        }
    }
}