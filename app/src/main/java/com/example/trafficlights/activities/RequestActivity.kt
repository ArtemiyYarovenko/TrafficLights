package com.example.trafficlights.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trafficlights.R


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

                 call.enqueue(object : Callback<ApiResponse?> {
                   override fun onResponse(call: Call<ApiResponse?>, response: Response<ApiResponse?>) {
                       Log.d("request", response.message())
                       val ticketResponse: ApiResponse = response.body()!!
                       Log.d("request", ticketResponse.toString())
                   }

                   override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                       Log.d("request", t.message)
                   }
               })

/*             callTicket.enqueue(object : Callback<TokenResponse> {
                   override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                       Log.d("ApiResponse", response.message())
                       val tokenResponse: TokenResponse = response.body()!!
                       Log.d("request", tokenResponse.toString())
                   }

                   override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                       Log.d("ApiResponse", t.message)
                   }
               }) */
    }*/
}