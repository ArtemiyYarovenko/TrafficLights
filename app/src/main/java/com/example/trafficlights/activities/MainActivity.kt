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
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

public const val USER_ID = "USER_ID"
public const val REGISTRATION = "REGISTRATION"

class MainActivity : AppCompatActivity() {

    lateinit var uuid: String

    override fun onCreate(savedInstanceState: Bundle?) {


        val sharedPreferences = getSharedPreferences("TrafficLights", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        // генерирование уникального индентификатора пользователя (если такого нет)
        // или получение уже сгенерированного из хранилища
        if (sharedPreferences.getString(USER_ID, null) == null) {
            uuid = UUID.randomUUID().toString()
            editor.putString(USER_ID, uuid)
            editor.apply()
            Log.d(USER_ID, "new generated uuid $uuid")
        } else {
            uuid = sharedPreferences.getString(USER_ID, null)!!
            Log.d(USER_ID, "uuid from shared preferences $uuid")
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // проверка на регистрацию
        if (!sharedPreferences.getBoolean(REGISTRATION, false)) {
            registrationBox.visibility = View.VISIBLE
        } else {
            item.visibility = View.VISIBLE
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
            /* "Сюда по хорошему пихнуть что-то типа стикера с указанием токена заявки" +
                    "и вообще чтобы выглядело красиво - солидно" */
        } else {
            Toast.makeText(this, "Упс! Что-то пошло не так :(", Toast.LENGTH_SHORT).show()
        }
    }

    public final fun clickOnItem(view: View) {
        val intent  = Intent(this, QrCodeActivity::class.java).apply {
            putExtra("ProblemId", "Какая проблема была выбрана (id)")
            putExtra(USER_ID, uuid)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivityForResult(intent, 1)
    }


}