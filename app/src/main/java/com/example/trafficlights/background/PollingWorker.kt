package com.example.trafficlights.background

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.trafficlights.`object`.TokenResponse
import com.example.trafficlights.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PollingWorker (appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {


    override fun doWork(): Result {
        val token = inputData.getString("Token")?: return Result.failure()
        val apiService = ApiService.create()
        val callTicket = apiService.checkToken(token)
        var message : String
        Log.d("background", "Начал работу")

        val response = callTicket.execute()
        Log.d("TicketResponse", response.message())
        val tokenResponse: TokenResponse = response.body()!!
        Log.d("background", "Получил респонс")
        if (tokenResponse.message != null) {
            message = tokenResponse.message
        } else {
            message = tokenResponse.error!!
        }
         if (message == "finish") {
             WorkManager.getInstance(applicationContext)
                 .cancelAllWorkByTag(token)
             Log.d("background", "отменил Работу" )
             Log.d("background","Статус работы " + WorkManager.getInstance(applicationContext).getWorkInfosByTag(token).isCancelled.toString())

    }

        Log.d("background", "Возращаю Result")
        return Result.success()
    }

}