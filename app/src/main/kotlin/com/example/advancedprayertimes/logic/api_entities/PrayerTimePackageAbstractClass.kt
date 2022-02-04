package com.example.advancedprayertimes.logic.api_entities

import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import java.time.LocalDateTime
import java.util.AbstractMap

abstract class PrayerTimePackageAbstractClass
{
    abstract var date: String?
    abstract var fajrTime: LocalDateTime?
    abstract var sunriseTime: LocalDateTime?
    abstract var duhaTime: LocalDateTime?
    abstract var dhuhrTime: LocalDateTime?
    abstract var asrTime: LocalDateTime?
    abstract var mithlaynTime: LocalDateTime?
    abstract var asrKarahaTime: LocalDateTime?
    abstract var maghribTime: LocalDateTime?
    abstract var ishtibaqAnNujumTime: LocalDateTime?
    abstract var ishaTime: LocalDateTime?

    fun GetTimeByType(prayerType: EPrayerTimeType, prayerTypeTimeType: EPrayerTimeMomentType): LocalDateTime?
    {
        return when(AbstractMap.SimpleEntry(prayerType, prayerTypeTimeType))
        {
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.End),
            AbstractMap.SimpleEntry(EPrayerTimeType.Fajr, EPrayerTimeMomentType.Beginning) -> this.fajrTime

            AbstractMap.SimpleEntry(EPrayerTimeType.Fajr, EPrayerTimeMomentType.End) -> this.sunriseTime

            AbstractMap.SimpleEntry(EPrayerTimeType.Duha, EPrayerTimeMomentType.Beginning) -> this.duhaTime

            AbstractMap.SimpleEntry(EPrayerTimeType.Duha, EPrayerTimeMomentType.End) -> this.dhuhrTime?.minusHours(1)

            AbstractMap.SimpleEntry(EPrayerTimeType.Dhuhr, EPrayerTimeMomentType.Beginning) -> this.dhuhrTime

            AbstractMap.SimpleEntry(EPrayerTimeType.Dhuhr, EPrayerTimeMomentType.End),
            AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.Beginning) -> this.asrTime

            AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.End),
            AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.Beginning) -> this.maghribTime

            AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.End),
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.Beginning) -> this.ishaTime

            else -> null
        }
    }
}