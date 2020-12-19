package com.example.trafficlights

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest



class MainActivity : AppCompatActivity() {

    private lateinit var tvExample: TextView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
            getLocation()


    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        // если нет разрешения на использование геолокации
            ActivityCompat.requestPermissions(this,
                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION), 44)
            return
        }

        // если геолокация есть, то делаем запрос ТЕКУЩЕЙ геолокации
        fusedLocationProviderClient
                .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnCompleteListener{
            task ->
            val location = task.result
            if (location != null) {
                Log.d("Locator", location.toString())
            }
        }

    }

    private fun isGooglePlayServicesAvailable () :Boolean {
        val googleApiAvailabilityLight = GoogleApiAvailabilityLight.getInstance()
        val status:Int = googleApiAvailabilityLight.isGooglePlayServicesAvailable(this)
        return status == ConnectionResult.SUCCESS
    }
}