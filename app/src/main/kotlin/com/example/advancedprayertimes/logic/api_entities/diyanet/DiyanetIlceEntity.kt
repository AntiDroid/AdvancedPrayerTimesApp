package com.example.advancedprayertimes.logic.api_entities.diyanet

import com.google.gson.annotations.SerializedName

class DiyanetIlceEntity : AbstractDiyanetSubEntity() {

    @SerializedName("IlceID")
    var ilceID: String? = null

    @SerializedName("IlceAdiEn")
    var ilceNameEn: String? = null

    override val id: String?
        get() = ilceID

    override val nameEn: String?
        get() = ilceNameEn
}