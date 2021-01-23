package com.example.trafficlights.`object`

import com.google.gson.annotations.SerializedName

data class TicketBody (
        @SerializedName("traffic_light_hash_code")
        val hashCode: String,
        @SerializedName("user_token")
        val user_id: String,
        @SerializedName("description")
        val description: String?
        )