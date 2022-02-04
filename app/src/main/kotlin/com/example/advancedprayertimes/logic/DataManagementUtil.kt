package com.example.advancedprayertimes.logic

import com.example.advancedprayertimes.logic.AppEnvironment.BuildGSON
import com.google.gson.Gson
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import android.content.SharedPreferences
import android.location.Address
import android.widget.TextView
import kotlin.Throws
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import com.example.advancedprayertimes.logic.setting_entities.PrayerTimeBeginningEndSettingsEntity
import com.example.advancedprayertimes.logic.api_entities.PrayerTimePackageAbstractClass
import com.example.advancedprayertimes.logic.enums.ESupportedAPIs
import java.lang.Exception
import java.time.format.DateTimeFormatter
import java.util.AbstractMap
import java.util.HashMap
import java.util.stream.Collectors

object DataManagementUtil {

    private val gson = BuildGSON("HH:mm")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    fun GetPrayerTimeEntityKeyForSharedPreference(prayerTimeType: EPrayerTimeType): String {
        return "$prayerTimeType data"
    }

    @JvmStatic
    fun GetTimeSettingsEntityKeyForSharedPreference(prayerTimeType: EPrayerTimeType): String {
        return "$prayerTimeType settings"
    }

    fun GetSelectedPlaceKeyForSharedPreference(): String {
        return "place information"
    }

    fun SaveLocalData(sharedPref: SharedPreferences, displayedDateText: String?) {
        val editor = sharedPref.edit()

        // SAVE LOCATION
        if (AppEnvironment.PlaceEntity != null) {
            editor.putString(
                GetSelectedPlaceKeyForSharedPreference(),
                gson.toJson(AppEnvironment.PlaceEntity)
            )
        }

        // SAVE PRAYER TIME DATA
        for (prayerEntity in PrayerTimeEntity.Prayers) {
            editor.putString(
                GetPrayerTimeEntityKeyForSharedPreference(prayerEntity.prayerTimeType),
                gson.toJson(prayerEntity)
            )
        }

        // SAVE ASSOCIATED DATE STRING
        editor.putString("displayedTime", displayedDateText)

        // SAVE PRAYER TIME SETTINGS
        for ((key, value) in AppEnvironment.prayerSettingsByPrayerType) {
            val jsonString = gson.toJson(value)
            editor.putString(GetTimeSettingsEntityKeyForSharedPreference(key), jsonString)
        }
        editor.apply()
    }

    fun RetrieveLocalData(sharedPref: SharedPreferences, displayedDateTextLabel: TextView) {

        if (sharedPref.contains(GetSelectedPlaceKeyForSharedPreference())) {

            AppEnvironment.PlaceEntity = gson.fromJson(
                sharedPref.getString(
                    GetSelectedPlaceKeyForSharedPreference(), null
                ), CustomPlaceEntity::class.java
            )
        }

        // RETRIEVE PRAYER TIME DATA
        for (i in PrayerTimeEntity.Prayers.indices) {

            val key = GetPrayerTimeEntityKeyForSharedPreference(PrayerTimeEntity.Prayers[i].prayerTimeType)

            val storedValue = sharedPref.getString(key, null)

            if (storedValue != null) {
                val retrievedPrayer = gson.fromJson(storedValue, PrayerTimeEntity::class.java)
                if (retrievedPrayer != null) {
                    PrayerTimeEntity.Prayers[i] = retrievedPrayer
                }
            }
        }

        // RETRIEVE ASSOCIATED DATE STRING
        displayedDateTextLabel.text = sharedPref.getString("displayedTime", "xx.xx.xxxx")

        // RETRIEVE ASSOCIATED DATE STRING
        val gson = Gson()
        for (prayerTimeType in AppEnvironment.prayerSettingsByPrayerType.keys) {
            val value = sharedPref.getString(
                GetTimeSettingsEntityKeyForSharedPreference(prayerTimeType),
                null
            )
            if (value != null) {
                val settings = gson.fromJson(value, PrayerSettingsEntity::class.java)
                AppEnvironment.prayerSettingsByPrayerType[prayerTimeType] = settings
            }
        }
    }

    @Throws(Exception::class)
    fun RetrieveDiyanetTimeData(
        toBeCalculatedPrayerTimes: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity>,
        cityAddress: Address?
    ): Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> {

        val diyanetPrayerTimeTypesHashMap = toBeCalculatedPrayerTimes
                .filter { x -> x.value.api == ESupportedAPIs.Diyanet }

        // ADD ALL DIYANET TIME CALCULATIONS
        return if (diyanetPrayerTimeTypesHashMap.isNotEmpty()) {
            try {
                val diyanetTime = HttpAPIRequestUtil.RetrieveDiyanetTimes(cityAddress)

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
    fun RetrieveAlAdhanTimeData(
        toBeCalculatedPrayerTimes: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity>,
        location: CustomLocation?
    ): Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> {

        val alAdhanPrayerTimeTypesHashMap = toBeCalculatedPrayerTimes
                .filter { x -> x.value.api == ESupportedAPIs.AlAdhan }

        val alAdhanTimesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> = HashMap()

        // ADD ALL ALADHAN TIME CALCULATIONS
        if (alAdhanPrayerTimeTypesHashMap.isNotEmpty()) {
            try {
                for ((key, value) in alAdhanPrayerTimeTypesHashMap) {

                    val alAdhanTime = HttpAPIRequestUtil.RetrieveAlAdhanTimes(
                        location,
                        value.fajrCalculationDegree,
                        value.ishaCalculationDegree,
                        null
                    )

                    if (alAdhanTime != null) {
                        alAdhanTimesHashMap[key] = alAdhanTime
                    }
                }
            } catch (e: Exception) {
                throw Exception(
                    "An error has occured while trying to retrieve AlAdhan prayer time data!",
                    e
                )
            }
        }

        var ishtibaqDegree: Double? =
            if (AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Maghrib]?.subPrayer1Settings?.isEnabled1 == true) {
                AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Maghrib]!!.subPrayer1Settings!!.ishtibaqDegree
            }
            else {
                null
            }

        if (ishtibaqDegree != null) {

            // any other muwaqqit request will suffice
            var ishtibaqTimePackage = HttpAPIRequestUtil.RetrieveAlAdhanTimes(
                        location,
                        10.0,
                        10.0,
                        ishtibaqDegree
                    )

            if (ishtibaqTimePackage != null) {

                // mithlayn
                alAdhanTimesHashMap[AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(
                    EPrayerTimeType.Maghrib,
                    EPrayerTimeMomentType.SubTimeOne
                )] = ishtibaqTimePackage
            }
        }

        return alAdhanTimesHashMap
    }

    @Throws(Exception::class)
    fun RetrieveMuwaqqitTimeData(
        toBeCalculatedPrayerTimes: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity>,
        location: CustomLocation
    ): Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> {

        val muwaqqitTimesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> = HashMap()

        val toBeCalculatedMuwaqqitTimesMap = toBeCalculatedPrayerTimes
            .filter { x -> x.value.api == ESupportedAPIs.Muwaqqit }

        val toBeCalculatedMuwaqqitDegreeTimesMap = toBeCalculatedMuwaqqitTimesMap
            .filter { x -> PrayerTimeBeginningEndSettingsEntity.DEGREE_TYPES.contains(x.key) }
            .toMutableMap()

        val toBeCalculatedMuwaqqitNonDegreeTimesMap = toBeCalculatedMuwaqqitTimesMap
            .filter { x -> !PrayerTimeBeginningEndSettingsEntity.DEGREE_TYPES.contains(x.key) }
            .toMutableMap()

        var asrKarahaDegree: Double? =
            if (AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Asr]?.subPrayer1Settings?.isEnabled2 == true) {
                AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Asr]!!.subPrayer1Settings!!.asrKarahaDegree
            }
            else {
                null
            }

        if (toBeCalculatedMuwaqqitDegreeTimesMap.isNotEmpty()) {

            val fajrDegreeMuwaqqitTimesHashMap = toBeCalculatedMuwaqqitDegreeTimesMap
                    .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> ->
                        PrayerTimeBeginningEndSettingsEntity.FAJR_DEGREE_TYPES.contains(x.key)
                    }.toMutableMap()

            val ishaDegreeMuwaqqitTimesHashMap = toBeCalculatedMuwaqqitDegreeTimesMap
                    .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> ->
                        PrayerTimeBeginningEndSettingsEntity.ISHA_DEGREE_TYPES.contains(
                            x.key
                        )
                    }.toMutableMap()

            // CALCULATIONS FOR MERGABLE DEGREE TIMES
            while (fajrDegreeMuwaqqitTimesHashMap.isNotEmpty() && ishaDegreeMuwaqqitTimesHashMap.isNotEmpty()) {

                val fajrDegreeEntry = fajrDegreeMuwaqqitTimesHashMap.entries.first()
                val ishaDegreeEntry = ishaDegreeMuwaqqitTimesHashMap.entries.first()

                val degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(
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

                val degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(
                    location,
                    settingsEntity.fajrCalculationDegree,
                    settingsEntity.ishaCalculationDegree,
                    asrKarahaDegree
                )
                    ?: throw Exception(
                        "Could not retrieve Non-Fajr/Isha Muwaqqit prayer time data for an unknown reason!",
                        null
                    )

                muwaqqitTimesHashMap[key] = degreeMuwaqqitTimeEntity
            }
        }

        // ADD CALCULATIONS FOR NON DEGREE TIMES
        if (toBeCalculatedMuwaqqitNonDegreeTimesMap.isNotEmpty()) {

            // any other muwaqqit request will suffice
            var nonDegreeMuwaqqitTimeEntity = muwaqqitTimesHashMap.values.firstOrNull()
                ?: HttpAPIRequestUtil.RetrieveMuwaqqitTimes(location, null, null, asrKarahaDegree)

            if (nonDegreeMuwaqqitTimeEntity != null) {
                for (prayerTimeType in toBeCalculatedMuwaqqitNonDegreeTimesMap.keys) {
                    muwaqqitTimesHashMap[prayerTimeType] = nonDegreeMuwaqqitTimeEntity
                }
            }
        }

        if (asrKarahaDegree != null) {

            // any other muwaqqit request will suffice
           var asrKarahaTimePackage =
                muwaqqitTimesHashMap.values.firstOrNull()
                    ?: HttpAPIRequestUtil.RetrieveMuwaqqitTimes(location, null, null, asrKarahaDegree)

            if (asrKarahaTimePackage != null) {

                // mithlayn
                muwaqqitTimesHashMap[AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(
                    EPrayerTimeType.Asr,
                    EPrayerTimeMomentType.SubTimeOne
                )] = asrKarahaTimePackage

                // karaha
                muwaqqitTimesHashMap[AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(
                    EPrayerTimeType.Asr,
                    EPrayerTimeMomentType.SubTimeTwo
                )] = asrKarahaTimePackage
            }
        }

        return muwaqqitTimesHashMap
    }
}