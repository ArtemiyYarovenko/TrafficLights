package com.example.trafficlights.background

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.*
import com.example.trafficlights.USER_ID
import com.example.trafficlights.Utils.getFileFromUri
import com.example.trafficlights.`object`.CustomTicketBody
import com.example.trafficlights.api.ApiService
import java.util.concurrent.TimeUnit

class UploadPhotoWorker(appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val userId = inputData.getString(USER_ID)
        val lat = inputData.getDouble("lat", 0.0)
        val long = inputData.getDouble("long", 0.0)
        val resp = ApiService.sendCustomTicket(
            customTicketBody = CustomTicketBody(
                userId!!, null,
                long.toFloat(), lat.toFloat()))

        Log.d("debug", resp.body()!!.message)

        val ticketId = resp.body()?.message!!.toInt()

        val tokenWorkPeriodicRequest = PeriodicWorkRequestBuilder<PollingWorker>(
                15, TimeUnit.MINUTES)
                .addTag(ticketId.toString())
                .setInputData(workDataOf("Token" to ticketId))
                .build()

        WorkManager.getInstance()
                .enqueue(tokenWorkPeriodicRequest)
        Log.d("debug", "Запущен поллинг по заявке без QR")

        val file1Path = inputData.getString("file1Uri")
        val file2Path = inputData.getString("file2Uri")
        val file3Path = inputData.getString("file3Uri")
        if (file1Path != null){
            val realFile1 = getFileFromUri(applicationContext.contentResolver, Uri.parse(file1Path), applicationContext.cacheDir)
            ApiService.createUploadRequestBody(realFile1, ticketId, userId)
        }
        if (file2Path != null){
            val realFile2 = getFileFromUri(applicationContext.contentResolver, Uri.parse(file2Path), applicationContext.cacheDir)
            ApiService.createUploadRequestBody(realFile2, ticketId, userId)
        }
        if (file3Path != null){
            val realFile3 = getFileFromUri(applicationContext.contentResolver, Uri.parse(file3Path), applicationContext.cacheDir)
            ApiService.createUploadRequestBody(realFile3, ticketId, userId)
        }

        return Result.success()
    }


}