package com.example.trafficlights.api

import com.example.trafficlights.`object`.TicketResponse
import com.example.trafficlights.`object`.TokenResponse
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {

    @GET("/Ticket/New")
    fun sendTicket(
        @Query("id") id: Int?,
        @Query("user_id") user_id: String?
    ): Call<TicketResponse>

    @GET("/Ticket/Check")
    fun checkToken(
        @Query("token") token :String
    ) : Call<TokenResponse>

    companion object Factory {
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
    }
}
