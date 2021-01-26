package com.example.trafficlights.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficlights.*
import com.example.trafficlights.Utils.isNetworkAvailable
import com.example.trafficlights.background.RegistrationWorker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item.*

class MainActivity : AppCompatActivity() {

    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        // грязный хак чтобы использовать интернет в мейн потоке
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // генерирование уникального индентификатора пользователя (если такого нет)
        // или получение уже сгенерированного из хранилища
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPreferences.edit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //тестовая фигня для работы с регистрацией
        editor.putBoolean(REGISTRATION,true).apply()
        editor.putString(USER_ID, "TEST").apply()
        //editor.putBoolean(REGISTRATION, false).commit()

        // проверка на регистрацию
        if (!sharedPreferences.getBoolean(REGISTRATION, false)) {
            registrationBox.visibility = View.VISIBLE
        } else {

            itemLayout.visibility = View.VISIBLE
            userId = sharedPreferences.getString(USER_ID, null)!!
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var  requestStatus = false
        if (requestCode == REQUEST_CODE_ACTIVITY_QR) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                //success
                data.apply {
                    requestStatus = getBooleanExtra("QR-code scan result", false)
                }
            } else {
                //fail
            }
        }

        if (requestCode == REQUEST_CODE_ACTIVITY_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                data.apply {
                    requestStatus = getBooleanExtra("QR-code scan result", false)
                }
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

    fun clickOnQrOption(view: View) {
        val intent  = Intent(this, QrCodeActivity::class.java).apply {
            putExtra(PROBLEM_ID, "Какая проблема была выбрана (id)")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivityForResult(intent, REQUEST_CODE_ACTIVITY_QR)
    }

    fun clickOnPhotoOption(view: View) {
        val intent  = Intent(this, BasicTicketActivity::class.java).apply {
            putExtra(PROBLEM_ID, "Какая проблема была выбрана (id)")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivityForResult(intent, REQUEST_CODE_ACTIVITY_PHOTO)
    }

    fun clickOnRegistrationButton(view: View) {
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
            if (isNetworkAvailable(applicationContext)){
                val registrationWork = OneTimeWorkRequestBuilder<RegistrationWorker>()
                    .setInputData(
                        workDataOf(
                            SURNAME to editPersonSurname.text.toString(),
                            NAME to editPersonName.text.toString(),
                            FATHERNAME to editPersonFatherName.text.toString(),
                            PHONENUMBER to editTextPhone.text.toString()
                        )
                    )
                    .build()

                WorkManager.getInstance(applicationContext)
                    .enqueue(registrationWork)

                Log.d(DEBUG_TAG, "Запущен запрос на регистрацию в бекграунде")

                registrationBox.visibility = View.INVISIBLE
                itemLayout.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Проверьте Интернет соединение", Toast.LENGTH_LONG).show()
            }

        }

    }


}