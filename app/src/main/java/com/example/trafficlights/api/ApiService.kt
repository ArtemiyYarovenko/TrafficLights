package com.example.trafficlights.api

import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficlights.`object`.ApiResponse
import com.example.trafficlights.`object`.TicketBody
import com.example.trafficlights.`object`.User
import com.example.trafficlights.background.PollingWorker
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


interface ApiService {

    @POST("/Ticket/")
    fun sendTicket (
            @Body ticketBody : TicketBody
    ): Call<ApiResponse>

    @POST("/MobileUser/Create")
    fun createUser(
            @Body user: User
    ): Call<ApiResponse>

    @GET("/Ticket/Check/")
    fun checkToken(
        @Query("token") token :String
    ) : Call<ApiResponse>

    companion object Ticket {
        private const val BASE_URL = "http://84.22.135.132:5000"
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

        fun sendTicket (ticketBody: TicketBody) {
            val apiService = create()
            val call = apiService.sendTicket(ticketBody)
            call.enqueue(object : Callback<ApiResponse>{
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    Log.d("api", response.message())
                    val apiResponse: ApiResponse = response.body()!!
                    Log.d("api", apiResponse.toString())

                    if (apiResponse.message != null){
                        val token = apiResponse.message
                        val tokenWorkPeriodicRequest = PeriodicWorkRequestBuilder<PollingWorker>(
                            15, TimeUnit.MINUTES)
                            .addTag(token)
                            .setInputData(workDataOf("Token" to token))
                            .build()

                        WorkManager.getInstance()
                            .enqueue(tokenWorkPeriodicRequest)
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.d("api", t.message!!)
                }
            })
        }

        fun userRequest (user: User):Response<ApiResponse>  {
            val apiService = create()
            val call = apiService.createUser(user)
            return call.execute()
            }
    }


}
