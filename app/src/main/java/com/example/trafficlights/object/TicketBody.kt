package com.example.trafficlights.`object`

import com.google.gson.annotations.SerializedName

data class TicketBody (
        @SerializedName("traffic_light_id")
        val id: Int,
        @SerializedName("user_id")
        val user_id: String
        )