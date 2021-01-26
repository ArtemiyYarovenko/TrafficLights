package com.example.trafficlights.background

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.*
import com.example.trafficlights.USER_ID
import com.example.trafficlights.`object`.CustomTicketBody
import com.example.trafficlights.api.ApiService
import java.io.File
import java.util.concurrent.TimeUnit

class UploadPhotoWorker(appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val userId = inputData.getString(USER_ID)
        val resp = ApiService.sendCustomTicket(customTicketBody = CustomTicketBody("testtoken1", null, 42.5F, 44.5F))
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
            ApiService.createUploadRequestBody(realFile1, ticketId, "testtoken1")
        }
        if (file2Path != null){
            val realFile2 = getFileFromUri(applicationContext.contentResolver, Uri.parse(file2Path), applicationContext.cacheDir)
            ApiService.createUploadRequestBody(realFile2, ticketId, "testtoken1")
        }
        if (file3Path != null){
            val realFile3 = getFileFromUri(applicationContext.contentResolver, Uri.parse(file3Path), applicationContext.cacheDir)
            ApiService.createUploadRequestBody(realFile3, ticketId, "testtoken1")
        }

        return Result.success()
    }

    private fun getFileFromUri(contentResolver: ContentResolver, uri: Uri, directory: File): File {
        val file =
                File.createTempFile("suffix", "prefix", directory)
        file.outputStream().use {
            contentResolver.openInputStream(uri)?.copyTo(it)
        }

        return file
    }
}