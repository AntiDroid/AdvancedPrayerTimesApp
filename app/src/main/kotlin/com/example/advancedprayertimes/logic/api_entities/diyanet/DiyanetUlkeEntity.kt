package com.example.advancedprayertimes.logic.api_entities.diyanet

import com.google.gson.annotations.SerializedName

class DiyanetUlkeEntity
{
    @SerializedName("UlkeID")
    var ulkeID: String? = null

    @SerializedName("UlkeAdiEn")
    var ulkeAdiEn: String? = null
}