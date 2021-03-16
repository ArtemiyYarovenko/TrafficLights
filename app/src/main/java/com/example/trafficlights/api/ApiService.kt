package com.example.trafficlights.api

import android.content.Context
import android.util.Log
import com.example.trafficlights.DEBUG_TAG
import com.example.trafficlights.`object`.*
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


interface ApiService {

    //Создание заявки по QR
    @POST("CreateTicket/QRTrafficLight")
    fun sendTicket(
        @Body qrTicketBody: QrTicketBody
    ): Call<ApiResponse>

    //Регистрация пользователя
    @POST("MobileUser/Create")
    fun createUser(
        @Body user: User
    ): Call<ApiResponse>

    //Проверка статуса заявки
    @GET("Ticket/Check/")
    fun checkToken(
        @Query("ticket_id") token: Int
    ) : Call<ApiResponse>

    //Получение категорий
    @GET("TicketType")
    fun getTicketTypes(
    ) : Call<List<TicketType>>

    //Создание обычной заявки
    @POST("CreateTicket/TrafficLight")
    fun sendCustomTicketTrafficLight(
        @Body customTicketBody: CustomTicketBody
    ): Call<ApiResponse>

    @POST("CreateTicket/Graffiti")
    fun sendCustomTicketGraffiti(
            @Body customTicketBody: CustomTicketBody
    ): Call<ApiResponse>

    @POST("CreateTicket/Button")
    fun sendCustomTicketButton(
            @Body customTicketBody: CustomTicketBody
    ): Call<ApiResponse>

    @POST("CreateTicket/RoadSign")
    fun sendCustomTicketRoadSign(
            @Body customTicketBody: CustomTicketBody
    ): Call<ApiResponse>

    //Прикрепление фотографии к заявке
    @Multipart
    @POST("Photo/")
    fun attachFile(
        @Part("ticket_id") ticket_id: RequestBody,
        @Part("user_token") user_id: RequestBody,
        @Part photo: MultipartBody.Part
    ): Call<ApiResponse>

    companion object Ticket {
        const val BASE_URL = "http://84.22.135.132:5000/"

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

                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.d(DEBUG_TAG, t.message!!)
                }
            })
            return call.isExecuted
        }

        fun sendQrTicket2(qrTicketBody: QrTicketBody): Response<ApiResponse> {
            val apiService = create()
            val call = apiService.sendTicket(qrTicketBody)
            return call.execute()
        }

        fun userRequest(user: User):Response<ApiResponse>  {
            val apiService = create()
            val call = apiService.createUser(user)
            return call.execute()
            }

        fun sendCustomTicket(customTicketBody: CustomTicketBody, problemId: Int): Response<ApiResponse> {
            val apiService = create()
            var call: Call<ApiResponse>? = null
            when (problemId){
                1 -> call = apiService.sendCustomTicketTrafficLight(customTicketBody)
                2 -> call = apiService.sendCustomTicketGraffiti(customTicketBody)
                3 -> call = apiService.sendCustomTicketRoadSign(customTicketBody)
                4 -> call = apiService.sendCustomTicketButton(customTicketBody)
            }
            return call?.execute()!!
        }

        fun createUploadRequestBody(file: File, ticketId: Int, userId: String){
            val apiService = create()
            Log.d(DEBUG_TAG, file.exists().toString())
            Log.d(DEBUG_TAG, file.length().toString())
            val ticket_Id: RequestBody = RequestBody.create(MediaType.parse("text/plain"), ticketId.toString())
            val user_Id: RequestBody = RequestBody.create(MediaType.parse("text/plain"), userId)

            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

            val call = apiService.attachFile(ticket_Id, user_Id, body)
            val response = call.execute()
            Log.d(DEBUG_TAG, response.message() + " " + response.errorBody().toString())
        }

        fun getTicketTypes() : Response<List<TicketType>>{
            val apiService = create()
            val call = apiService.getTicketTypes()
            return call.execute()
        }
    }
}
