package com.example.trafficlights.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.trafficlights.R
import java.util.*

class MainActivity : AppCompatActivity() {

    private val USER_ID = "USER_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences = getSharedPreferences("TrafficLights", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        if (sharedPreferences.getString(USER_ID, null) == null) {
            val uuid = UUID.randomUUID().toString()
            editor.putString(USER_ID, uuid)
            editor.commit()
            Log.v(USER_ID, "new generated uuid $uuid")
        } else {
            val uuid = sharedPreferences.getString(USER_ID, null)
            Log.v(USER_ID, "uuid from shared preferences $uuid")
        }
    }

    public fun clickOnImage(view: View) {
        val intent  = Intent(this, QrCodeActivity::class.java).apply {
            putExtra("ProblemId", "Какая проблема была выбрана (id)")
        }
        startActivity(intent)
    }
}