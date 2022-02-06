package com.example.advancedprayertimes.logic.api_entities.diyanet

import com.google.gson.annotations.SerializedName

class DiyanetUlkeEntity : AbstractDiyanetSubEntity() {

    @SerializedName("UlkeID")
    var ulkeID: String? = null

    @SerializedName("UlkeAdiEn")
    var ulkeNameEn: String? = null

    override val id: String?
        get() = ulkeID

    override val nameEn: String?
        get() = ulkeNameEn
}