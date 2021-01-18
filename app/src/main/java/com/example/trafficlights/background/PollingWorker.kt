package com.example.trafficlights.background

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.trafficlights.`object`.TokenResponse
import com.example.trafficlights.api.ApiService

class PollingWorker (appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {


    override fun doWork(): Result {
        val token = inputData.getString("Token")?: return Result.failure()
        val apiService = ApiService.create()
        val callTicket = apiService.checkToken(token)
        var message : String
        Log.d("background", "Начал работу")

        val response = callTicket.execute()
        Log.d("api", response.message())
        val tokenResponse: TokenResponse = response.body()!!
        Log.d("api", "Получил респонс")
        if (tokenResponse.message != null) {
            message = tokenResponse.message
        } else {
            message = tokenResponse.error!!
        }
         if (message == "Выполнена") {
             WorkManager.getInstance(applicationContext)
                 .cancelAllWorkByTag(token)
             Log.d("background", "отменил Работу" )
             Log.d("background","Статус работы " + WorkManager.getInstance(applicationContext).getWorkInfosByTag(token).isCancelled.toString())
             Notification(applicationContext, token).createNotification()
    }

        Log.d("background", "Возращаю Result")
        return Result.success()
    }

}