package com.example.advancedprayertimes.logic.api_entities

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalTime

class MuwaqqitPrayerTimeDayEntity(
    @field:SerializedName("fajr_time") override var fajrTime: LocalTime?,
    @field:SerializedName("sunrise_time") override var sunriseTime: LocalTime?,
    @field:SerializedName("duha_time") override var duhaTime: LocalTime?,
    @field:SerializedName("zohr_time") override var dhuhrTime: LocalTime?,
    @field:SerializedName("mithl_time") override var asrTime: LocalTime?,
    @field:SerializedName("mithlain_time") override var mithlaynTime: LocalTime?,
    @field:SerializedName("sunset_time") override var maghribTime: LocalTime?,
    @field:SerializedName("esha_time") override var ishaTime: LocalTime?,
    @field:SerializedName("fajr_date") override var date: LocalDate?,

    @field:SerializedName("fajr_angle") var fajrAngle: Double = 0.0,
    @field:SerializedName("karaha_angle") var asrKarahaAngle: Double = 0.0,
    @field:SerializedName("esha_angle") var ishaAngle: Double = 0.0,

    @field:SerializedName("karaha_time") override var asrKarahaTime: LocalTime? = null
) : PrayerTimePackageAbstractClass()
{
    override var ishtibaqAnNujumTime: LocalTime?
        get() = null
        set(ishtibaqAnNujumTime)
        {
            throw NotImplementedError()
        }
}