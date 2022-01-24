package com.example.advancedprayertimes.logic.api_entities

import com.example.advancedprayertimes.logic.api_entities.PrayerTimePackageAbstractClass
import java.time.LocalDateTime
import kotlin.NotImplementedError

class AlAdhanPrayerTimeDayEntity(
    override var fajrTime: LocalDateTime?,
    override var sunriseTime: LocalDateTime?,
    override var dhuhrTime: LocalDateTime?,
    override var asrTime: LocalDateTime?,
    override var mithlaynTime: LocalDateTime?,
    override var maghribTime: LocalDateTime?,
    override var ishtibaqAnNujumTime: LocalDateTime?,
    override var ishaTime: LocalDateTime?
) : PrayerTimePackageAbstractClass()
{
    override var date: String? = null

    override var duhaTime: LocalDateTime?
        get() = null
        set(duhaTime)
        {
            throw NotImplementedError()
        }

    override var asrKarahaTime: LocalDateTime?
        get() = null
        set(asrKarahaTime)
        {
            throw NotImplementedError()
        }
}