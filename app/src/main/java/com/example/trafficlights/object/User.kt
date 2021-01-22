package com.example.trafficlights.`object`

import com.google.gson.annotations.SerializedName

data class User (
        @SerializedName("surname")
        val surname: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("father_name")
        val fatherName: String,
        @SerializedName("phone")
        val phone: String
        )