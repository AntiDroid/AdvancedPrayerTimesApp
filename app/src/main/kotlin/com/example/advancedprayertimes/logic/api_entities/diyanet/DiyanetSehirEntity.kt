package com.example.advancedprayertimes.logic.api_entities.diyanet

import com.google.gson.annotations.SerializedName

class DiyanetSehirEntity : AbstractDiyanetSubEntity() {

    @SerializedName("SehirID")
    var sehirID: String? = null

    @SerializedName("SehirAdiEn")
    var sehirNameEn: String? = null

    override val id: String?
        get() = sehirID

    override val nameEn: String?
        get() = sehirNameEn
}