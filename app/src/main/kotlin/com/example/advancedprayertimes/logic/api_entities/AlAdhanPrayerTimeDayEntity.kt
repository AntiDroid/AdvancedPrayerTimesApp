package com.example.advancedprayertimes.logic.api_entities

import java.time.LocalDate
import java.time.LocalTime

class AlAdhanPrayerTimeDayEntity(
    override var fajrTime: LocalTime?,
    override var sunriseTime: LocalTime?,
    override var dhuhrTime: LocalTime?,
    override var asrTime: LocalTime?,
    override var mithlaynTime: LocalTime?,
    override var maghribTime: LocalTime?,
    override var ishtibaqAnNujumTime: LocalTime?,
    override var ishaTime: LocalTime?,
    override var date: LocalDate?,
    var fajrAngle: Double? = 0.0,
    var ishaAngle: Double? = 0.0,
    var ishtibaqAngle: Double? = 0.0
) : PrayerTimePackageAbstractClass()
{
    override var duhaTime: LocalTime?
        get() = null
        set(duhaTime)
        {
            throw NotImplementedError()
        }

    override var asrKarahaTime: LocalTime?
        get() = null
        set(asrKarahaTime)
        {
            throw NotImplementedError()
        }
}