package com.example.trafficlights.api

import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficlights.DEBUG_TAG
import com.example.trafficlights.`object`.ApiResponse
import com.example.trafficlights.`object`.CustomTicketBody
import com.example.trafficlights.`object`.QrTicketBody
import com.example.trafficlights.`object`.User
import com.example.trafficlights.background.PollingWorker
import com.google.gson.GsonBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


interface ApiService {

    @POST("CreateTicket/QR")
    fun sendTicket(
        @Body qrTicketBody: QrTicketBody
    ): Call<ApiResponse>

    @POST("MobileUser/Create")
    fun createUser(
        @Body user: User
    ): Call<ApiResponse>

    @GET("Ticket/Check/")
    fun checkToken(
        @Query("ticket_id") token: Int
    ) : Call<ApiResponse>

    @POST("CreateTicket/Custom")
    fun sendCustomTicket(
        @Body customTicketBody: CustomTicketBody
    ): Call<ApiResponse>

    @Multipart
    @POST("Photo/")
    fun attachFile(
        @Part("ticket_id") ticket_id: RequestBody,
        @Part("user_token") user_id: RequestBody,
        @Part photo: MultipartBody.Part
    ): Call<ApiResponse>

    companion object Ticket {
        private const val BASE_URL = "http://84.22.135.132:5000/"

        fun create():ApiService {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                    .callbackExecutor(Executors.newSingleThreadExecutor())
                .build()

            return retrofit.create(ApiService::class.java)
        }

        fun sendQrTicket(qrTicketBody: QrTicketBody, context: Context): Boolean {
            val apiService = create()
            val call = apiService.sendTicket(qrTicketBody)
            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    Log.d(DEBUG_TAG, response.message())
                    val apiResponse: ApiResponse = response.body()!!
                    Log.d(DEBUG_TAG, apiResponse.toString())

                    if (apiResponse.message != null) {
                        val token = apiResponse.message
                        val tokenWorkPeriodicRequest = PeriodicWorkRequestBuilder<PollingWorker>(
                            15, TimeUnit.MINUTES, 3, TimeUnit.MINUTES
                        )
                            .addTag(token)
                            .setInputData(workDataOf("Token" to token.toInt()))
                            .build()

                        WorkManager.getInstance(context)
                            .enqueue(tokenWorkPeriodicRequest)
                        Log.d(DEBUG_TAG, "Запущен поллинг")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.d(DEBUG_TAG, t.message!!)
                }
            })
            return call.isExecuted
        }

        fun userRequest(user: User):Response<ApiResponse>  {
            val apiService = create()
            val call = apiService.createUser(user)
            return call.execute()
            }

        fun sendCustomTicket(customTicketBody: CustomTicketBody): Response<ApiResponse> {
            val apiService = create()
            val call = apiService.sendCustomTicket(customTicketBody)
            return call.execute()
        }

        fun createUploadRequestBody(file: File, ticketId: Int, userId: String){
            val apiService = create()
            Log.d(DEBUG_TAG, file.exists().toString())
            Log.d(DEBUG_TAG, file.length().toString())
            val ticketId: RequestBody = RequestBody.create(MediaType.parse("text/plain"), ticketId.toString())
            val userId: RequestBody = RequestBody.create(MediaType.parse("text/plain"), userId)

            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

            val call = apiService.attachFile(ticketId, userId, body)
            val response = call.execute()
            Log.d(DEBUG_TAG, response.message() + " " + response.errorBody().toString())
        }
    }
}
