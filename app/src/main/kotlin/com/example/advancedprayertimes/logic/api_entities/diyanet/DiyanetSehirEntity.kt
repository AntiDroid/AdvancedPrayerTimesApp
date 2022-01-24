package com.example.advancedprayertimes.logic.api_entities.diyanet

import com.google.gson.annotations.SerializedName

class DiyanetSehirEntity
{
    @SerializedName("SehirID")
    var sehirID: String? = null

    @SerializedName("SehirAdiEn")
    var sehirAdiEn: String? = null
}