package com.example.trafficlights

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest


const val USER_ID = "USER_ID"
const val REGISTRATION = "REGISTRATION"

const val SURNAME = "SURNAME"
const val NAME = "NAME"
const val FATHERNAME = "FATHERNAME"
const val PHONENUMBER = "PHONENUMBER"

const val CHANNEL_ID = "777"
const val CHANNEL_NAME = "Notifer"
const val notificationId = 1

const val RECEIVED = "Поступила"
const val IN_PROGRESS = "В обработке"
const val DONE = "Выполнена"
const val CANCELLED = "Отменена"

const val REQUEST_IMAGE_CAPTURE = 14
const val REQUEST_CAMERA_CODE_PERMISSION = 15
const val PERMISSION_ALL = 1
val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.CAMERA
)

object Utils {
    public lateinit var location: Location

    fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun getGeolocation(context: Context) {
        val fusedLocationProviderClient= FusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Выдайте разрешения приложению в настройках", Toast.LENGTH_LONG).show()
        }
        val addOnCompleteListener = fusedLocationProviderClient
            .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Log.d("debug", task.result.toString())
                    location = task.result
                }
            }
    }

    fun isInit() = this::location.isInitialized



}