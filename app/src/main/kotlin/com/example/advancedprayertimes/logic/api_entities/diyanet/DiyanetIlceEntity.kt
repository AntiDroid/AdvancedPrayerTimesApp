package com.example.advancedprayertimes.logic.api_entities.diyanet

import com.google.gson.annotations.SerializedName

class DiyanetIlceEntity
{
    @SerializedName("IlceID")
    var ilceID: String? = null

    @SerializedName("IlceAdiEn")
    var ilceAdiEn: String? = null
}