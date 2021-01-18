package com.example.trafficlights.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.trafficlights.`object`.TicketBody
import com.example.trafficlights.R
import com.example.trafficlights.`object`.TicketResponse
import com.example.trafficlights.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RequestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

/*    public fun clickOnImage(view: View) {
        val apiService = ApiService.create()
        val ticketBody = TicketBody(117, "23т")
        val call = apiService.sendTicket(ticketBody)
        val callTicket = apiService.checkToken("4D9B081C1C5F44743EB38E9FC6BD4E4B")

                 call.enqueue(object : Callback<TicketResponse?> {
                   override fun onResponse(call: Call<TicketResponse?>, response: Response<TicketResponse?>) {
                       Log.d("request", response.message())
                       val ticketResponse: TicketResponse = response.body()!!
                       Log.d("request", ticketResponse.toString())
                   }

                   override fun onFailure(call: Call<TicketResponse?>, t: Throwable) {
                       Log.d("request", t.message)
                   }
               })

/*             callTicket.enqueue(object : Callback<TokenResponse> {
                   override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                       Log.d("TicketResponse", response.message())
                       val tokenResponse: TokenResponse = response.body()!!
                       Log.d("request", tokenResponse.toString())
                   }

                   override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                       Log.d("TicketResponse", t.message)
                   }
               }) */
    }*/
}