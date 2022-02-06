package com.example.advancedprayertimes.logic.util

import com.example.advancedprayertimes.logic.AppEnvironment.BuildGSON
import com.google.gson.Gson
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import android.content.SharedPreferences
import android.location.Address
import com.example.advancedprayertimes.logic.*
import kotlin.Throws
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import com.example.advancedprayertimes.logic.setting_entities.PrayerTimeBeginningEndSettingsEntity
import com.example.advancedprayertimes.logic.api_entities.PrayerTimePackageAbstractClass
import com.example.advancedprayertimes.logic.enums.ESupportedAPIs
import com.example.advancedprayertimes.logic.extensions.notContains
import java.lang.Exception
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.AbstractMap
import java.util.HashMap

object DataManagementUtil {

    private val gson = BuildGSON("HH:mm")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    fun getPrayerTimeEntityKeyForSharedPreference(prayerTimeType: EPrayerTimeType): String {
        return "$prayerTimeType data"
    }

    @JvmStatic
    fun getTimeSettingsEntityKeyForSharedPreference(prayerTimeType: EPrayerTimeType): String {
        return "$prayerTimeType settings"
    }

    fun getSelectedPlaceKeyForSharedPreference(): String {
        return "place information"
    }

    fun getDisplayedDateKeyForSharedPreference(): String {
        return "displayedTime"
    }

    fun SaveLocalData(sharedPref: SharedPreferences) {
        val editor = sharedPref.edit()

        // SAVE LOCATION
        if (AppEnvironment.PlaceEntity != null) {
            editor.putString(
                getSelectedPlaceKeyForSharedPreference(),
                gson.toJson(AppEnvironment.PlaceEntity)
            )
        }

        // SAVE PRAYER TIME DATA
        for (prayerEntity in PrayerTimeEntity.Prayers) {
            editor.putString(
                getPrayerTimeEntityKeyForSharedPreference(prayerEntity.prayerTimeType),
                gson.toJson(prayerEntity)
            )
        }

        // SAVE ASSOCIATED DATE STRING
        if(AppEnvironment.timeDate != null)
        {
            editor.putLong(getDisplayedDateKeyForSharedPreference(), AppEnvironment.timeDate!!.toEpochSecond(ZoneOffset.UTC))
        }
        else
        {
            editor.remove(getDisplayedDateKeyForSharedPreference()).apply();
        }

        // SAVE PRAYER TIME SETTINGS
        for ((key, value) in AppEnvironment.prayerSettingsByPrayerType) {
            val jsonString = gson.toJson(value)
            editor.putString(getTimeSettingsEntityKeyForSharedPreference(key), jsonString)
        }
        editor.apply()
    }

    fun retrieveLocalData(sharedPref: SharedPreferences) {

        if (sharedPref.contains(getSelectedPlaceKeyForSharedPreference())) {

            AppEnvironment.PlaceEntity = gson.fromJson(
                sharedPref.getString(
                    getSelectedPlaceKeyForSharedPreference(), null
                ), CustomPlaceEntity::class.java
            )
        }

        // RETRIEVE PRAYER TIME DATA
        for (i in PrayerTimeEntity.Prayers.indices) {

            val key = getPrayerTimeEntityKeyForSharedPreference(PrayerTimeEntity.Prayers[i].prayerTimeType)

            val storedValue = sharedPref.getString(key, null)

            if (storedValue != null) {
                val retrievedPrayer = gson.fromJson(storedValue, PrayerTimeEntity::class.java)

                if (retrievedPrayer != null) {
                    PrayerTimeEntity.Prayers[i] = retrievedPrayer
                }
            }
        }

        // RETRIEVE ASSOCIATED DATE STRING
        AppEnvironment.timeDate = LocalDateTime.ofEpochSecond(sharedPref.getLong(
            getDisplayedDateKeyForSharedPreference(), 0), 0, ZoneOffset.UTC)

        // RETRIEVE ASSOCIATED DATE STRING
        val gson = Gson()
        for (prayerTimeType in AppEnvironment.prayerSettingsByPrayerType.keys) {
            val value = sharedPref.getString(
                getTimeSettingsEntityKeyForSharedPreference(prayerTimeType),
                null
            )
            if (value != null) {
                val settings = gson.fromJson(value, PrayerSettingsEntity::class.java)
                AppEnvironment.prayerSettingsByPrayerType[prayerTimeType] = settings
            }
        }
    }

    @Throws(Exception::class)
    fun retrieveDiyanetTimeData(
        toBeCalculatedPrayerTimes: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity>,
        cityAddress: Address
    ){

        val diyanetPrayerTimeTypesHashMap = toBeCalculatedPrayerTimes.filter { x -> x.value.api == ESupportedAPIs.Diyanet }

        // ADD ALL DIYANET TIME CALCULATIONS
        AppEnvironment.diyanetTimesHashMap = if (diyanetPrayerTimeTypesHashMap.isNotEmpty()) {

            try {

                val diyanetTime = HttpRequestUtil.retrieveDiyanetTimes(cityAddress)

                if (diyanetTime != null) {
                    diyanetPrayerTimeTypesHashMap.keys.associateWith { diyanetTime }
                } else {
                    throw Exception(
                        "Could not retrieve Diyanet prayer time data for an unknown reason!",
                        null
                    )
                }

            } catch (e: Exception) {
                throw Exception(
                    "An error has occured while trying to retrieve Diyanet prayer time data!",
                    e
                )
            }
        } else HashMap()
    }

    @Throws(Exception::class)
    fun retrieveAlAdhanTimeData(
        toBeCalculatedPrayerTimes: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity>,
        location: CustomLocation
    ){

        val alAdhanPrayerTimeTypesHashMap = toBeCalculatedPrayerTimes.filter { x -> x.value.api == ESupportedAPIs.AlAdhan }

        val alAdhanPrayerTimesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> = HashMap()

        // ADD ALL ALADHAN TIME CALCULATIONS
        if (alAdhanPrayerTimeTypesHashMap.isNotEmpty()) {

            try {
                for ((key, value) in alAdhanPrayerTimeTypesHashMap) {

                    val alAdhanTime = HttpRequestUtil.retrieveAlAdhanTimes(
                        location,
                        value.fajrCalculationDegree,
                        value.ishaCalculationDegree,
                        null
                    )

                    if (alAdhanTime != null) {
                        alAdhanPrayerTimesHashMap[key] = alAdhanTime
                    }
                }

            } catch (e: Exception) {
                throw Exception(
                    "An error has occured while trying to retrieve AlAdhan prayer time data!",
                    e
                )
            }
        }

        val ishtibaqDegree: Double? =
            if (AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Maghrib]?.subPrayer1Settings?.isEnabled1 == true) {
                AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Maghrib]!!.subPrayer1Settings!!.ishtibaqDegree
            }
            else {
                null
            }

        if (ishtibaqDegree != null) {

            // any other muwaqqit request will suffice
            val ishtibaqTimePackage = HttpRequestUtil.retrieveAlAdhanTimes(
                location,
                10.0,
                10.0,
                ishtibaqDegree
            )

            if (ishtibaqTimePackage != null) {

                // mithlayn
                alAdhanPrayerTimesHashMap[AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(
                    EPrayerTimeType.Maghrib,
                    EPrayerTimeMomentType.SubTimeOne
                )] = ishtibaqTimePackage
            }
        }

        AppEnvironment.alAdhanTimesHashMap = alAdhanPrayerTimesHashMap
    }

    @Throws(Exception::class)
    fun retrieveMuwaqqitTimeData(
        toBeCalculatedPrayerTimes: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity>,
        location: CustomLocation
    ){

        val muwaqqitTimesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> = HashMap()

        val toBeCalculatedMuwaqqitTimesMap = toBeCalculatedPrayerTimes
            .filter { x -> x.value.api == ESupportedAPIs.Muwaqqit }

        val toBeCalculatedMuwaqqitDegreeTimesMap = toBeCalculatedMuwaqqitTimesMap
            .filter { x -> PrayerTimeBeginningEndSettingsEntity.DEGREE_TYPES.contains(x.key) }
            .toMutableMap()

        val toBeCalculatedMuwaqqitNonDegreeTimesMap = toBeCalculatedMuwaqqitTimesMap
            .filter { x -> !PrayerTimeBeginningEndSettingsEntity.DEGREE_TYPES.notContains(x.key) }
            .toMutableMap()

        val asrKarahaDegree: Double? =
            if (AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Asr]?.subPrayer1Settings?.isEnabled2 == true) {
                AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Asr]!!.subPrayer1Settings!!.asrKarahaDegree
            }
            else { null }

        if (toBeCalculatedMuwaqqitDegreeTimesMap.any()) {

            val fajrDegreeMuwaqqitTimesHashMap = toBeCalculatedMuwaqqitDegreeTimesMap
                .filter { x -> PrayerTimeBeginningEndSettingsEntity.FAJR_DEGREE_TYPES.contains(x.key)}
                .toMutableMap()

            val ishaDegreeMuwaqqitTimesHashMap = toBeCalculatedMuwaqqitDegreeTimesMap
                .filter { x -> PrayerTimeBeginningEndSettingsEntity.ISHA_DEGREE_TYPES.contains(x.key)}
                .toMutableMap()

            // CALCULATIONS FOR MERGABLE DEGREE TIMES
            while (fajrDegreeMuwaqqitTimesHashMap.isNotEmpty() && ishaDegreeMuwaqqitTimesHashMap.isNotEmpty()) {

                val fajrDegreeEntry = fajrDegreeMuwaqqitTimesHashMap.entries.first()
                val ishaDegreeEntry = ishaDegreeMuwaqqitTimesHashMap.entries.first()

                val degreeMuwaqqitTimeEntity = HttpRequestUtil.retrieveMuwaqqitTimes(
                    location,
                    fajrDegreeEntry.value.fajrCalculationDegree,
                    ishaDegreeEntry.value.ishaCalculationDegree,
                    asrKarahaDegree
                )
                    ?: throw Exception(
                        "Could not retrieve Fajr/Isha Muwaqqit prayer time data for an unknown reason!",
                        null
                    )

                muwaqqitTimesHashMap[fajrDegreeEntry.key] = degreeMuwaqqitTimeEntity
                muwaqqitTimesHashMap[ishaDegreeEntry.key] = degreeMuwaqqitTimeEntity

                // remove handled entries from the lists
                fajrDegreeMuwaqqitTimesHashMap.remove(fajrDegreeEntry.key)
                ishaDegreeMuwaqqitTimesHashMap.remove(ishaDegreeEntry.key)
                toBeCalculatedMuwaqqitDegreeTimesMap.remove(fajrDegreeEntry.key)
                toBeCalculatedMuwaqqitDegreeTimesMap.remove(ishaDegreeEntry.key)
            }

            // CALCULATIONS FOR NON MERGABLE DEGREE TIMES
            for ((key, settingsEntity) in toBeCalculatedMuwaqqitDegreeTimesMap) {

                muwaqqitTimesHashMap[key] = HttpRequestUtil.retrieveMuwaqqitTimes(
                    location,
                    settingsEntity.fajrCalculationDegree,
                    settingsEntity.ishaCalculationDegree,
                    asrKarahaDegree
                )
                    ?: throw Exception(
                        "Could not retrieve Non-Fajr/Isha Muwaqqit prayer time data for an unknown reason!",
                        null
                    )
            }
        }

        // ADD CALCULATIONS FOR NON DEGREE TIMES
        if (toBeCalculatedMuwaqqitNonDegreeTimesMap.isNotEmpty()) {

            // any other muwaqqit request will suffice
            val nonDegreeMuwaqqitTimeEntity = muwaqqitTimesHashMap.values.firstOrNull()
                ?: HttpRequestUtil.retrieveMuwaqqitTimes(location, null, null, asrKarahaDegree)

            if (nonDegreeMuwaqqitTimeEntity != null) {
                for (prayerTimeType in toBeCalculatedMuwaqqitNonDegreeTimesMap.keys) {
                    muwaqqitTimesHashMap[prayerTimeType] = nonDegreeMuwaqqitTimeEntity
                }
            }
        }

        if (asrKarahaDegree != null) {

            // any other muwaqqit request will suffice
           val asrKarahaTimePackage =
                muwaqqitTimesHashMap.values.firstOrNull()
                    ?: HttpRequestUtil.retrieveMuwaqqitTimes(
                        location,
                        null,
                        null,
                        asrKarahaDegree
                    )

            if (asrKarahaTimePackage != null) {
                // mithlayn
                muwaqqitTimesHashMap[AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.SubTimeOne)] = asrKarahaTimePackage
                // karaha
                muwaqqitTimesHashMap[AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.SubTimeTwo)] = asrKarahaTimePackage
            }
        }

        AppEnvironment.muwaqqitTimesHashMap =  muwaqqitTimesHashMap
    }
}