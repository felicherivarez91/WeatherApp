package com.example.weatherapplication

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.weatherapplication.databinding.FragmentHomeBinding
import com.google.android.gms.location.*


class Home_Fragment : Fragment(){

    private var binding: FragmentHomeBinding? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var latitude : String = ""
    var longitude : String = ""
    companion object {
        private const val PERMISSION_ID = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding?.btnCurrentLocation?.setOnClickListener {

            // pass arguments to second fragment
            getLastLocation()
            if(!latitude.isBlank() || !longitude.isBlank()) {
              openWeatherDetails(latitude,longitude)
            }
        }



        // custom latitude and longitude from user
        binding?.btnCustomLocation?.setOnClickListener {
            // check if the values are not null
            if(!binding?.etLatitudeInput?.text.isNullOrEmpty()|| !binding?.etLongitudeInput?.text.isNullOrEmpty()){
                latitude = binding?.etLatitudeInput?.text.toString()
                longitude = binding?.etLongitudeInput?.text.toString()
                // pass arguments to second fragment
                openWeatherDetails(latitude,longitude)

            }else{
                Toast.makeText(
                    requireContext(),
                    "Please input the missing value",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

        }

        return binding?.root
    }



    fun checkPermission():Boolean{
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if(
            ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false
    }
    private fun requestPermission(){
        //this function will allows us to tell the user to request the necessary permission if they are not granted
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    fun isLocationEnabled():Boolean{
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun getLastLocation(){
        if(checkPermission()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
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
                    val location:Location? = task.result
                    if(location == null){
                        newLocationData()

                    }else{
                        longitude=  location.longitude.toString()
                        latitude=location.latitude.toString()
                    }
                }
            }else{
                Toast.makeText(requireContext(),"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            requestPermission()
            getLastLocation()

        }
    }


    private fun newLocationData(){
        val locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
            locationRequest,locationCallback,Looper.myLooper()
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

    fun openWeatherDetails(lat : String, long : String){
        val bundle = Bundle()
        bundle.putString("latitude", lat)
        bundle.putString("longitude", long)
        findNavController().navigate(R.id.action_home2_to_weatherDetails,bundle)

    }
}

