package com.example.trafficlights

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public fun clickOnImage (view: View) {
        Toast.makeText(this, "Тут будут дальнейшие действия после выбора проблемы", Toast.LENGTH_SHORT).show()
    }
}