package com.example.trafficlights.`object`

import com.google.gson.annotations.SerializedName

data class TicketResponse (
    @SerializedName("error")
    val error: String?,
    @SerializedName("token")
    val token : String?
        )
