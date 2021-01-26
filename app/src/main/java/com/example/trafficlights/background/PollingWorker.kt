package com.example.trafficlights.background

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.trafficlights.*
import com.example.trafficlights.`object`.ApiResponse
import com.example.trafficlights.api.ApiService

class PollingWorker (appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {


    override fun doWork(): Result {
        val token = inputData.getInt("Token", 0)

        val apiService = ApiService.create()
        val callTicket = apiService.checkToken(token)

        val status : String
        var lastStatus: String? = null

        Log.d(DEBUG_TAG, "Поллинг воркер начал работу")

        val response = callTicket.execute()
        Log.d(DEBUG_TAG, response.message())

        val tokenResponse: ApiResponse = response.body()!!
        Log.d(DEBUG_TAG, "Поллинг воркер получил респонс")

        if (tokenResponse.message != null) {
            status = tokenResponse.message
            Log.d(DEBUG_TAG, "Полученный статус = $status")
            if(status != lastStatus){
                when (status) {
                    RECEIVED, IN_PROGRESS ->{
                        lastStatus = status
                        Notification(applicationContext, token.toString(), status).createNotification()
                    }

                    DONE, CANCELLED -> {
                        lastStatus = status
                        cancelWork(token, status)
                    }

                    else -> {
                        WorkManager.getInstance(applicationContext)
                                .cancelAllWorkByTag(token.toString())
                    }
                }
            }

        } else {
            status = tokenResponse.error!!
            Log.d(DEBUG_TAG, "Полученный статус = $status")
            cancelWork(token, status)
        }

        Log.d(DEBUG_TAG, "Возращаю Result success")
        return Result.success()
    }

    private fun cancelWork(token: Int, status: String){
        WorkManager.getInstance(applicationContext)
                .cancelAllWorkByTag(token.toString())

        Log.d(DEBUG_TAG, "Отменил Работу" )
        Log.d(DEBUG_TAG,"Статус работы " +
                WorkManager.getInstance(applicationContext).
                getWorkInfosByTag(token.toString()).isCancelled.toString())
        Notification(applicationContext, token.toString(), status).createNotification()
    }

}