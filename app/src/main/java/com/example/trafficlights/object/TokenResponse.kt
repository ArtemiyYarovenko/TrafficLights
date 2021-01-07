package com.example.trafficlights.`object`

import com.google.gson.annotations.SerializedName

data class TokenResponse (
    @SerializedName ("error")
    val error : String?,
    @SerializedName ("message")
    val message : String?
        )
