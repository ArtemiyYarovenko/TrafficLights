package com.example.trafficlights

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.trafficlights.`object`.TicketResponse
import com.example.trafficlights.`object`.TokenResponse
import com.example.trafficlights.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public fun clickOnImage(view: View) {
        val apiService = ApiService.create()
        val call = apiService.sendTicket(3, "414")
        val callTicket = apiService.checkToken("EA82E0E0gdfg04117C8A36F2EE266D7B4")

        call?.enqueue(object : Callback<TicketResponse?> {
            override fun onResponse(call: Call<TicketResponse?>, response: Response<TicketResponse?>) {
                Log.d("request", response.message())
                val ticketResponse: TicketResponse = response.body()!!
            }

            override fun onFailure(call: Call<TicketResponse?>, t: Throwable) {
                Log.d("request", t.message)
            }
        })

        callTicket?.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                Log.d("TicketResponse", response.message())
                val tokenResponse: TokenResponse = response.body()!!
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.d("TicketResponse", t.message)
            }
        })
    }
}