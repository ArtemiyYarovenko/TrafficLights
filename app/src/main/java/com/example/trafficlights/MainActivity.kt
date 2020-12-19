package com.example.trafficlights

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight



class MainActivity : AppCompatActivity() {

    private lateinit var tvExample: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvExample = findViewById(R.id.test)

       if (isGooglePlayServicesAvailable()) {
            //google play services доступны на устройстве
           tvExample.text = "Google Play Services доступны"
        }
        else {
            //google play services не доступны на устройстве
            tvExample.text = "Google Play Services не доступны"
        }


    }

     private fun isGooglePlayServicesAvailable () :Boolean {
        val googleApiAvailabilityLight = GoogleApiAvailabilityLight.getInstance()
        val status:Int = googleApiAvailabilityLight.isGooglePlayServicesAvailable(this)
        return status == ConnectionResult.SUCCESS
    }
}