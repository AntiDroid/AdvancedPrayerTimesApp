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

        val DEGREE_TYPES
            get() = ISHA_DEGREE_TYPES + FAJR_DEGREE_TYPES

        val ISHA_DEGREE_TYPES = listOf(
                AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.End),
                AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.Beginning)
            )

        val FAJR_DEGREE_TYPES = listOf(
                AbstractMap.SimpleEntry(EPrayerTimeType.Fajr, EPrayerTimeMomentType.Beginning),
                AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.End)
            )
        }
}