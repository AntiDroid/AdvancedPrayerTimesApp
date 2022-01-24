package com.example.advancedprayertimes.logic.api_entities

import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import java.time.LocalDateTime

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

    fun GetTimeByType(
        prayerType: EPrayerTimeType,
        prayerTypeTimeType: EPrayerTimeMomentType
    ): LocalDateTime?
    {
        if (prayerType == EPrayerTimeType.Isha && prayerTypeTimeType == EPrayerTimeMomentType.End
            || prayerType == EPrayerTimeType.Fajr && prayerTypeTimeType == EPrayerTimeMomentType.Beginning
        ) {
            return fajrTime
        } else if (prayerType == EPrayerTimeType.Fajr && prayerTypeTimeType == EPrayerTimeMomentType.End) {
            return sunriseTime
        } else if (prayerType == EPrayerTimeType.Duha && prayerTypeTimeType == EPrayerTimeMomentType.Beginning) {
            return duhaTime
        } else if (prayerType == EPrayerTimeType.Duha && prayerTypeTimeType == EPrayerTimeMomentType.End) {
            var returnLocalDateTime = dhuhrTime!!
            returnLocalDateTime = returnLocalDateTime.minusHours(1)
            return returnLocalDateTime
        } else if (prayerType == EPrayerTimeType.Dhuhr && prayerTypeTimeType == EPrayerTimeMomentType.Beginning) {
            return dhuhrTime
        } else if (prayerType == EPrayerTimeType.Dhuhr && prayerTypeTimeType == EPrayerTimeMomentType.End
            || prayerType == EPrayerTimeType.Asr && prayerTypeTimeType == EPrayerTimeMomentType.Beginning
        ) {
            return asrTime
        } else if (prayerType == EPrayerTimeType.Asr && prayerTypeTimeType == EPrayerTimeMomentType.End
            || prayerType == EPrayerTimeType.Maghrib && prayerTypeTimeType == EPrayerTimeMomentType.Beginning
        ) {
            return maghribTime
        } else if (prayerType == EPrayerTimeType.Maghrib && prayerTypeTimeType == EPrayerTimeMomentType.End
            || prayerType == EPrayerTimeType.Isha && prayerTypeTimeType == EPrayerTimeMomentType.Beginning
        ) {
            return ishaTime
        }

        return null
    }
}