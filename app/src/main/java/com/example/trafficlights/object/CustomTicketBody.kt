package com.example.trafficlights.`object`

import com.google.gson.annotations.SerializedName

data class CustomTicketBody (
    @SerializedName("user_token")
    val user_id: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("long_")
    val long: Double,
    @SerializedName("lat")
    val lat: Double
)