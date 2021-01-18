/* package com.example.trafficlights.background

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.trafficlights.api.ApiService

class SyncRequestWorker (appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val id = inputData.getInt("id", -1)
        val userId = inputData.getString("user_id")
        val apiService = ApiService.create()
        var answer: String?
        val response = apiService.sendTicket(id, userId).execute()
        if (response.body()?.token != null) {
            answer = response.body()?.token
        } else {
            answer = response.body()?.error
        }
        val result : Data = workDataOf("token" to answer)
        return Result.success(result)

    }


}*/