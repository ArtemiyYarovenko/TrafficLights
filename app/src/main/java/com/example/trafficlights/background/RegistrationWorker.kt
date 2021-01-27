package com.example.trafficlights.background

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.trafficlights.*
import com.example.trafficlights.`object`.User
import com.example.trafficlights.api.ApiService

class RegistrationWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPreferences.edit()

        val personSurname = inputData.getString(SURNAME)
        val personName = inputData.getString(NAME)
        val personFatherName = inputData.getString(FATHERNAME)
        val phoneNumber = inputData.getString(PHONENUMBER)



        val registration = ApiService.userRequest(
            user = User(
                personSurname!!,
                personName!!,
                personFatherName!!,
                phoneNumber!!)
        )
        Log.d(DEBUG_TAG, "Запрос на регистрацию успешен? " + registration.isSuccessful.toString())

        if (registration.isSuccessful) {
            val response = registration.body()
            Log.d(DEBUG_TAG, "Ошибка(если есть) " + response?.error)
            if (response?.message != null) {
                Log.d(DEBUG_TAG, "Ответ по регистрации " + response.message)
                val userId = response.message
                Log.d(DEBUG_TAG, "user_id = $userId")
                editor.putBoolean(REGISTRATION, true)
                editor.putString(USER_ID, userId)
                editor.commit()
            }
        } else{
            return Result.retry()
        }

        return Result.success()
    }

}