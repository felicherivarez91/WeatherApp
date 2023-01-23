package com.example.weatherapplication.network

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.weatherapplication.MainActivity
import com.example.weatherapplication.R


class CheckInternetBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

       val main =  context as MainActivity
        main.setContentView(R.layout.activity_main)
        val isOnline: Boolean = NetworkUtil().isOnline(context)

        if(isOnline){
            //there is internet connection
            Toast.makeText(context, "There is internet", Toast.LENGTH_LONG).show();
           // image.setImageResource(R.drawable.internet)
            // retrieves the data
     /*       main.viewModel.getWeatherData(main.viewModel.latitude, main.viewModel.longitude)
            //saves data to database
     //       main.addRecord(main.weatherDao)
            // sets the ui
            main.setupUI()
*/

        }else{
            Toast.makeText(context, "There is no internet", Toast.LENGTH_LONG).show();
            // no internet
         //   main.setUpUIFromDatabase(main.weatherDao)


        }


        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
    }
}