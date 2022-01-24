package com.example.advancedprayertimes.logic.api_entities

import com.example.advancedprayertimes.logic.api_entities.PrayerTimePackageAbstractClass
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import kotlin.NotImplementedError

class DiyanetPrayerTimeDayEntity : PrayerTimePackageAbstractClass()
{
    @SerializedName("MiladiTarihKisa")
    override var date: String? = null

    @SerializedName("Imsak")
    override var fajrTime: LocalDateTime? = null

    @SerializedName("Gunes")
    override var sunriseTime: LocalDateTime? = null

    @SerializedName("Ogle")
    override var dhuhrTime: LocalDateTime? = null

    @SerializedName("Ikindi")
    override var asrTime: LocalDateTime? = null

    @SerializedName("Aksam")
    override var maghribTime: LocalDateTime? = null

    @SerializedName("Yatsi")
    override var ishaTime: LocalDateTime? = null

    override var duhaTime: LocalDateTime?
        get() = null
        set(duhaTime)
        {
            throw NotImplementedError()
        }

    override var mithlaynTime: LocalDateTime?
        get() = null
        set(mithlaynTime)
        {
            throw NotImplementedError()
        }

    override var asrKarahaTime: LocalDateTime?
        get() = null
        set(asrKarahaTime)
        {
            throw NotImplementedError()
        }

    override var ishtibaqAnNujumTime: LocalDateTime?
        get() = null
        set(ishtibaqAnNujumTime)
        {
            throw NotImplementedError()
        }
}