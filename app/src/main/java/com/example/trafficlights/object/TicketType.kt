package com.example.trafficlights.`object`

import com.google.gson.annotations.SerializedName

data class TicketType (
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("qRable")
        val QRable: Boolean,
        @SerializedName("hint")
        val hint: String?,
        @SerializedName("description")
        val description: String?,
        @SerializedName("url")
        val url: String
        )

