/*package com.example.trafficlights.activities


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.work.*
import com.example.trafficlights.R
import com.example.trafficlights.`object`.ApiResponse
import com.example.trafficlights.api.ApiService
import com.example.trafficlights.background.PollingWorker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class BackgroundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

  /*  public fun clickOnImage (view: View) {
       val intent  = Intent(this, QrCodeActivity::class.java).apply {
            putExtra("ProblemId", "Какая проблема была выбрана (id)")
        }
        startActivity(intent)
        val apiService = ApiService.create()
      //  val call = apiService.sendTicket(2, "32141")
        var token: String

        call.enqueue(object : Callback<ApiResponse?> {
            override fun onResponse(call: Call<ApiResponse?>, response: Response<ApiResponse?>) {
                Log.d("request", response.message())
                val ticketResponse: ApiResponse = response.body()!!
                if (ticketResponse.token!=null){
                    token = ticketResponse.token
                    val tokenWorkRequest = OneTimeWorkRequestBuilder<PollingWorker>()
                        .setInputData(workDataOf("Token" to token))
                        .build()
                    val tokenWorkPeriodicRequest = PeriodicWorkRequestBuilder<PollingWorker>(
                        15, TimeUnit.MINUTES)
                        .addTag(token)
                        .setInputData(workDataOf("Token" to token))
                        .build()

                    WorkManager.getInstance(applicationContext)
                        .enqueue(tokenWorkPeriodicRequest)
                }


            }

            override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                Log.d("request", t.message)
            }
        })


    }
}*/