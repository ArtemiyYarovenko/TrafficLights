package com.example.trafficlights.api

import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.trafficlights.`object`.TicketBody
import com.example.trafficlights.`object`.TicketResponse
import com.example.trafficlights.`object`.TokenResponse
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
import java.util.concurrent.TimeUnit


interface ApiService {

    @POST("/Ticket/")
    fun sendTicket (
            @Body ticketBody : TicketBody
    ): Call<TicketResponse>

    @GET("/Ticket/Check/")
    fun checkToken(
        @Query("token") token :String
    ) : Call<TokenResponse>

    companion object Ticket {
        private val BASE_URL = "http://84.22.135.132:5000"
        fun create():ApiService {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(ApiService::class.java)
        }

        fun ticketRequest (id: Int, uuid: String) {
            val apiService = create()
            val ticketBody = TicketBody(id, uuid)
            val call = apiService.sendTicket(ticketBody)
            call.enqueue(object : Callback<TicketResponse>{
                override fun onResponse(call: Call<TicketResponse>, response: Response<TicketResponse>) {
                    Log.d("api", response.message())
                    val ticketResponse: TicketResponse = response.body()!!
                    Log.d("api", ticketResponse.toString())

                    if (ticketResponse.token!=null){
                        val token = ticketResponse.token!!
                        val tokenWorkPeriodicRequest = PeriodicWorkRequestBuilder<PollingWorker>(
                            15, TimeUnit.MINUTES)
                            .addTag(token)
                            .setInputData(workDataOf("Token" to token))
                            .build()
                        8

                        WorkManager.getInstance()
                            .enqueue(tokenWorkPeriodicRequest)
                    }
                }

                override fun onFailure(call: Call<TicketResponse>, t: Throwable) {
                    Log.d("api", t.message!!)
                }
            })
        }
    }


}
