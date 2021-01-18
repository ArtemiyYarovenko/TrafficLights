package com.example.trafficlights.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trafficlights.R
import java.util.*

public const val USER_ID = "USER_ID"

class MainActivity : AppCompatActivity() {

    lateinit var uuid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // генерирование уникального индентификатора (если такого нет)
        // или получение уже сгенерированного из хранилища
        val sharedPreferences = getSharedPreferences("TrafficLights", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        if (sharedPreferences.getString(USER_ID, null) == null) {
            uuid = UUID.randomUUID().toString()
            editor.putString(USER_ID, uuid)
            editor.apply()
            Log.v(USER_ID, "new generated uuid $uuid")
        } else {
            uuid = sharedPreferences.getString(USER_ID, null)!!
            Log.v(USER_ID, "uuid from shared preferences $uuid")
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var  requestStatus: Boolean = false
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                //success
                data.apply {
                    requestStatus = getBooleanExtra("QR-code scan result", false)
                }
            } else {
                //fail
            }
        }
        if (requestStatus) {
            Toast.makeText(this, "Ваша заявка успешно отправлена", Toast.LENGTH_SHORT).show()
            TODO("Сюда по хорошему пихнуть что-то типа стикера с указанием токена заявки" +
                    "и вообще чтобы выглядело красиво - солидно")
        } else {
            Toast.makeText(this, "Упс! Что-то пошло не так :(", Toast.LENGTH_SHORT).show()
        }
    }

    public fun clickOnItem(view: View) {
        val intent  = Intent(this, QrCodeActivity::class.java).apply {
            putExtra("ProblemId", "Какая проблема была выбрана (id)")
            putExtra(USER_ID, uuid)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivityForResult(intent, 1)
    }


}