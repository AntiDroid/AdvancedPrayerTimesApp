package com.example.advancedprayertimes.logic.api_entities

import com.example.advancedprayertimes.logic.api_entities.PrayerTimePackageAbstractClass
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalTime
import kotlin.NotImplementedError

class DiyanetPrayerTimeDayEntity : PrayerTimePackageAbstractClass()
{
    @SerializedName("MiladiTarihKisa")
    override var date: LocalDate? = null

    @SerializedName("Imsak")
    override var fajrTime: LocalTime? = null

    @SerializedName("Gunes")
    override var sunriseTime: LocalTime? = null

    @SerializedName("Ogle")
    override var dhuhrTime: LocalTime? = null

    @SerializedName("Ikindi")
    override var asrTime: LocalTime? = null

    @SerializedName("Aksam")
    override var maghribTime: LocalTime? = null

    @SerializedName("Yatsi")
    override var ishaTime: LocalTime? = null

    override var duhaTime: LocalTime?
        get() = null
        set(duhaTime)
        {
            throw NotImplementedError()
        }

    override var mithlaynTime: LocalTime?
        get() = null
        set(mithlaynTime)
        {
            throw NotImplementedError()
        }

    override var asrKarahaTime: LocalTime?
        get() = null
        set(asrKarahaTime)
        {
            throw NotImplementedError()
        }

    override var ishtibaqAnNujumTime: LocalTime?
        get() = null
        set(ishtibaqAnNujumTime)
        {
            throw NotImplementedError()
        }

    var ulkeNameEn: String? = null
    var sehirNameEn: String? = null
    var ilceNameEn: String? = null
}