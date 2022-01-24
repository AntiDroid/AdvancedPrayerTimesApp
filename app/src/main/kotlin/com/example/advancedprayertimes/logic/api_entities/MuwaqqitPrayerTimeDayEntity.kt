package com.example.advancedprayertimes.logic.api_entities

import com.example.advancedprayertimes.logic.api_entities.PrayerTimePackageAbstractClass
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import kotlin.NotImplementedError

class MuwaqqitPrayerTimeDayEntity(
    @field:SerializedName("fajr_time") override var fajrTime: LocalDateTime?,
    @field:SerializedName(
        "sunrise_time"
    ) override var sunriseTime: LocalDateTime?,
    @field:SerializedName("duha_time") override var duhaTime: LocalDateTime?,
    @field:SerializedName(
        "zohr_time"
    ) override var dhuhrTime: LocalDateTime?,
    @field:SerializedName("mithl_time") override var asrTime: LocalDateTime?,
    @field:SerializedName(
        "mithlain_time"
    ) override var mithlaynTime: LocalDateTime?,
    @field:SerializedName("sunset_time") override var maghribTime: LocalDateTime?,
    @field:SerializedName(
        "esha_time"
    ) override var ishaTime: LocalDateTime?,
    @field:SerializedName("fajr_date") override var date: String?
) : PrayerTimePackageAbstractClass()
{
    @SerializedName("fajr_angle")
    var fajrAngle = 0.0

    @SerializedName("karaha_time")
    override var asrKarahaTime: LocalDateTime? = null

    @SerializedName("karaha_angle")
    var asrKarahaAngle = 0.0

    @SerializedName("esha_angle")
    var ishaAngle = 0.0

    override var ishtibaqAnNujumTime: LocalDateTime?
        get() = null
        set(ishtibaqAnNujumTime)
        {
            throw NotImplementedError()
        }
}