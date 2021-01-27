package com.example.trafficlights

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import java.io.File

const val DEBUG_TAG = "DEBUG"

const val USER_ID = "USER_ID"
const val REGISTRATION = "REGISTRATION"

const val DESCRIPTION = "DESCRIPTION"
const val LATITUDE = "LATITUDE"
const val LONGITUDE = "LONGITUDE"

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

const val REQUEST_CAMERA_CODE_PERMISSION = 15
const val PERMISSION_ALL = 1
val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.CAMERA
)

const val REQUEST_CODE_ACTIVITY_QR = 1
const val REQUEST_CODE_ACTIVITY_PHOTO = 2
const val REQUEST_IMAGE_CAPTURE = 3

const val PROBLEM_ID = "PROBLEM_ID"
const val STATUS = "STATUS"
const val REASON = "REASON"

object Utils {
    lateinit var location: Location

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
        } else {
            Log.d(DEBUG_TAG, "пытаюсь получить локацию")
            fusedLocationProviderClient
                    .getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(DEBUG_TAG, task.result.toString())
                            location = task.result
                        }
                    }
        }

    }

    fun getFileFromUri(contentResolver: ContentResolver, uri: Uri, directory: File): File {
        val file =
            File.createTempFile("suffix", "prefix", directory)
        file.outputStream().use {
            contentResolver.openInputStream(uri)?.copyTo(it)
        }

        return file
    }

    fun isInit() = this::location.isInitialized

    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }



}