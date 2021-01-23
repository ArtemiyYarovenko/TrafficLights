package com.example.trafficlights.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.telephony.PhoneNumberUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trafficlights.R
import com.example.trafficlights.REGISTRATION
import com.example.trafficlights.USER_ID
import com.example.trafficlights.`object`.User
import com.example.trafficlights.api.ApiService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        // грязный хак чтобы использовать интернет в мейн потоке
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // генерирование уникального индентификатора пользователя (если такого нет)
        // или получение уже сгенерированного из хранилища
        val sharedPreferences = getSharedPreferences("TrafficLights", AppCompatActivity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // проверка на регистрацию
        if (!sharedPreferences.getBoolean(REGISTRATION, false)) {
            registrationBox.visibility = View.VISIBLE
        } else {
            item.visibility = View.VISIBLE
            userId = sharedPreferences.getString(USER_ID, null)!!
        }

        //тестовая фигня для сброса регистрации
        editor.putBoolean(REGISTRATION, false).commit()

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
            putExtra(USER_ID, userId)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivityForResult(intent, 1)
    }

    fun clickOnRegistrationButton(view: View) {
        val sharedPreferences = getSharedPreferences("TrafficLights", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        var surnameIsNotEmpty = false
        var nameIsNotEmpty = false
        var fatherNameIsNotEmpty = false
        var phoneNumberIsNotEmpty = false
        var phoneNumberFormatIsNotCorrect = false

        //обработка поля фамилии
        if (editPersonSurname.text.isNotEmpty()) {
            surnameIsNotEmpty = true
        } else {
            Toast.makeText(this,
                    "Поле фамилии пустое!",
                    Toast.LENGTH_SHORT).show()
        }

        //обработка поля имени
        if (editPersonName.text.isNotEmpty()) {
            nameIsNotEmpty = true
        } else {
            Toast.makeText(this,
                    "Поле имени пустое!",
                    Toast.LENGTH_SHORT).show()
        }

        //обработка поля отчества
        if (editPersonFatherName.text.isNotEmpty()) {
            fatherNameIsNotEmpty = true
        } else {
            Toast.makeText(this,
                    "Поле отчества пустое!",
                    Toast.LENGTH_SHORT).show()
        }

        //обработка поля номера (на пустотсу и формат)
        if (editTextPhone.text.isNotEmpty()) {
            phoneNumberIsNotEmpty = true
            phoneNumberFormatIsNotCorrect = PhoneNumberUtils.formatNumberToE164(editTextPhone.text.toString(),
                    "RU").isNullOrEmpty()
            if (phoneNumberFormatIsNotCorrect) {
                Toast.makeText(this,
                        "Некорректный номер телефона",
                        Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this,
                    "Поле номера пустое!",
                    Toast.LENGTH_SHORT).show()
        }

        // все поля заполнены - регистрация прошла
        if (surnameIsNotEmpty && nameIsNotEmpty &&
            phoneNumberIsNotEmpty && fatherNameIsNotEmpty
                && !phoneNumberFormatIsNotCorrect ) {
            Toast.makeText(this,
                    "Регистрация успешно проведена",
                    Toast.LENGTH_SHORT).show()
            val registration = ApiService.userRequest(user = User(
                    editPersonSurname.text.toString(),
                    editPersonName.text.toString(),
                    editPersonFatherName.text.toString(),
                    editTextPhone.text.toString()
            ))

            if (registration.isSuccessful) {
                val response = registration.body()
                if (response!!.message != null) {
                    userId = response.message!!
                    Toast.makeText(this,
                        userId,
                        Toast.LENGTH_SHORT).show()
                    editor.putBoolean(REGISTRATION, true)
                    editor.putString(USER_ID, userId)
                    editor.apply()
                    registrationBox.visibility = View.INVISIBLE
                    item.visibility = View.VISIBLE
                }
            }

        }

    }

}