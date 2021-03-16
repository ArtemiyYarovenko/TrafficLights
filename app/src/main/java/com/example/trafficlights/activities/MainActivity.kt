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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficlights.*
import com.example.trafficlights.RecyclerView.TicketTypeAdapter
import com.example.trafficlights.Utils.isNetworkAvailable
import com.example.trafficlights.api.ApiService
import com.example.trafficlights.background.RegistrationWorker
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        // грязный хак чтобы использовать интернет в мейн потоке
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

    /*    if (AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(applicationContext)){
            val didAutoStartWorked = AutoStartPermissionHelper.getInstance().getAutoStartPermission(applicationContext)
            Log.d(DEBUG_TAG, "Автостарт для китайцев сработал?: " +
                    didAutoStartWorked.toString())
            if (!didAutoStartWorked){
                val intent1 = Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT)
                val intent2 = Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT)
                val intent3 =  Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"))
                startActivity(intent3)
            }
        }*/

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPreferences.edit()

        val prikol = ApiService.getTicketTypes()
        val items = prikol.body()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewTicketType.layoutManager = LinearLayoutManager(this)
        recyclerViewTicketType.adapter = TicketTypeAdapter(this, items!!)

        //тестовая фигня для работы с регистрацией
        //editor.putBoolean(REGISTRATION, true).commit()
        //editor.putString(USER_ID, "TEST").commit()
        //editor.putBoolean(REGISTRATION, false).commit()

        // проверка на регистрацию
        val reg = sharedPreferences.getBoolean(REGISTRATION, false)
        if (!sharedPreferences.getBoolean(REGISTRATION, false)) {
            registrationBox.visibility = View.VISIBLE
        } else {

            recyclerViewTicketType.visibility = View.VISIBLE
            userId = sharedPreferences.getString(USER_ID, null)!!
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var requestStatus = false
        var reason:String? = null
        var ticket:String? = null
        if (requestCode == REQUEST_CODE_ACTIVITY_QR || requestCode == REQUEST_CODE_ACTIVITY_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                //success
                data.apply {
                    requestStatus = getBooleanExtra(STATUS, false)
                    if (!requestStatus){
                        reason = getStringExtra(REASON)
                        Toast.makeText(applicationContext, reason, Toast.LENGTH_SHORT).show()
                    } else {
                        ticket = getStringExtra("ticket_id")
                        Toast.makeText(
                            applicationContext,
                            "Ваша заявка $ticket успешно отправлена",
                            Toast.LENGTH_SHORT
                        ).show()
                        /* "Сюда по хорошему пихнуть что-то типа стикера с указанием токена заявки" +
                                "и вообще чтобы выглядело красиво - солидно" */
                    }
                }
            } else {
                //fail
            }
        }


    }

    fun clickOnQrOption(view: View) {
        val intent  = Intent(this, QrCodeActivity::class.java).apply {
            putExtra(PROBLEM_ID, "Какая проблема была выбрана (id)")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivityForResult(intent, REQUEST_CODE_ACTIVITY_QR)
    }

    fun clickOnPhotoOption(view: View, id: Int) {
        val intent  = Intent(this, BasicTicketActivity::class.java).apply {
            putExtra(PROBLEM_ID, id)
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
            Toast.makeText(
                this,
                "Поле фамилии пустое!",
                Toast.LENGTH_SHORT
            ).show()
        }

        //обработка поля имени
        if (editPersonName.text.isNotEmpty()) {
            nameIsNotEmpty = true
        } else {
            Toast.makeText(
                this,
                "Поле имени пустое!",
                Toast.LENGTH_SHORT
            ).show()
        }

        //обработка поля отчества
        if (editPersonFatherName.text.isNotEmpty()) {
            fatherNameIsNotEmpty = true
        } else {
            Toast.makeText(
                this,
                "Поле отчества пустое!",
                Toast.LENGTH_SHORT
            ).show()
        }

        //обработка поля номера (на пустотсу и формат)
        if (editTextPhone.text.isNotEmpty()) {
            phoneNumberIsNotEmpty = true
            phoneNumberFormatIsNotCorrect = PhoneNumberUtils.formatNumberToE164(
                editTextPhone.text.toString(),
                "RU"
            ).isNullOrEmpty()
            if (phoneNumberFormatIsNotCorrect) {
                Toast.makeText(
                    this,
                    "Некорректный номер телефона",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Поле номера пустое!",
                Toast.LENGTH_SHORT
            ).show()
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
                recyclerViewTicketType.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Проверьте Интернет соединение", Toast.LENGTH_LONG).show()
            }

        }

    }
    companion object

}