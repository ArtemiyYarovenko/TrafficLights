package com.example.trafficlights.background

import android.content.Context
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.trafficlights.USER_ID
import com.example.trafficlights.Utils.getFileFromUri
import com.example.trafficlights.api.ApiService

class UploadPhotoWorker(appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val userId = inputData.getString(USER_ID)
        val ticketId = inputData.getInt("ticket_id", 0)


        val file1Path = inputData.getString("file1Uri")
        val file2Path = inputData.getString("file2Uri")
        val file3Path = inputData.getString("file3Uri")
        if (file1Path != null){
            val realFile1 = getFileFromUri(applicationContext.contentResolver, Uri.parse(file1Path), applicationContext.cacheDir)
            ApiService.createUploadRequestBody(realFile1, ticketId, userId!!)
        }
        if (file2Path != null){
            val realFile2 = getFileFromUri(applicationContext.contentResolver, Uri.parse(file2Path), applicationContext.cacheDir)
            ApiService.createUploadRequestBody(realFile2, ticketId, userId!!)
        }
        if (file3Path != null){
            val realFile3 = getFileFromUri(applicationContext.contentResolver, Uri.parse(file3Path), applicationContext.cacheDir)
            ApiService.createUploadRequestBody(realFile3, ticketId, userId!!)
        }

        return Result.success()
    }


}