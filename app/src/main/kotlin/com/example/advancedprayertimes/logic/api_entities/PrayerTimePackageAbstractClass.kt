package com.example.advancedprayertimes.logic.api_entities

import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import java.time.LocalDate
import java.time.LocalTime
import java.util.AbstractMap

abstract class PrayerTimePackageAbstractClass
{
    abstract var date: LocalDate?
    abstract var fajrTime: LocalTime?
    abstract var sunriseTime: LocalTime?
    abstract var duhaTime: LocalTime?
    abstract var dhuhrTime: LocalTime?
    abstract var asrTime: LocalTime?
    abstract var mithlaynTime: LocalTime?
    abstract var asrKarahaTime: LocalTime?
    abstract var maghribTime: LocalTime?
    abstract var ishtibaqAnNujumTime: LocalTime?
    abstract var ishaTime: LocalTime?

    fun getTimeByType(prayerType: EPrayerTimeType, prayerTypeTimeType: EPrayerTimeMomentType): LocalTime? {

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