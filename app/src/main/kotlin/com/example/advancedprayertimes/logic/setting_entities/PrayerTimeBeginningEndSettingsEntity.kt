package com.example.advancedprayertimes.logic.setting_entities

import com.example.advancedprayertimes.logic.enums.ESupportedAPIs
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import java.util.AbstractMap
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream

class PrayerTimeBeginningEndSettingsEntity(
    var api: ESupportedAPIs = ESupportedAPIs.Undefined,
    var minuteAdjustment: Int = 0,
    var fajrCalculationDegree: Double? = null,
    var ishaCalculationDegree: Double? = null
)
{
    companion object {
        @JvmField
        var DEGREE_TYPES: java.util.HashSet<*> = Stream.of<AbstractMap.SimpleEntry<*, *>>(
            AbstractMap.SimpleEntry<Any?, Any?>(
                EPrayerTimeType.Fajr,
                EPrayerTimeMomentType.Beginning
            ),
            AbstractMap.SimpleEntry<Any?, Any?>(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.End),
            AbstractMap.SimpleEntry<Any?, Any?>(
                EPrayerTimeType.Isha,
                EPrayerTimeMomentType.Beginning
            ),
            AbstractMap.SimpleEntry<Any?, Any?>(EPrayerTimeType.Isha, EPrayerTimeMomentType.End)
        ).collect(
            Collectors.toCollection(
                Supplier { HashSet() })
        )
        @JvmField
        var ISHA_DEGREE_TYPES: java.util.HashSet<*> = Stream.of<AbstractMap.SimpleEntry<*, *>>(
            AbstractMap.SimpleEntry<Any?, Any?>(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.End),
            AbstractMap.SimpleEntry<Any?, Any?>(
                EPrayerTimeType.Isha,
                EPrayerTimeMomentType.Beginning
            )
        ).collect(
            Collectors.toCollection(
                Supplier { HashSet() })
        )
        @JvmField
        var FAJR_DEGREE_TYPES: java.util.HashSet<*> = Stream.of<AbstractMap.SimpleEntry<*, *>>(
            AbstractMap.SimpleEntry<Any?, Any?>(
                EPrayerTimeType.Fajr,
                EPrayerTimeMomentType.Beginning
            ),
            AbstractMap.SimpleEntry<Any?, Any?>(EPrayerTimeType.Isha, EPrayerTimeMomentType.End)
        ).collect(
            Collectors.toCollection(
                Supplier { HashSet() })
        )
    }
}