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
import java.util.function.Function
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

            val key = GetPrayerTimeEntityKeyForSharedPreference(
                PrayerTimeEntity.Prayers[i].prayerTimeType
            )

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

        val diyanetPrayerTimeTypesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> =
            toBeCalculatedPrayerTimes.entries.stream()
                .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.value.api === ESupportedAPIs.Diyanet }
                .collect(
                    Collectors.toMap(
                        { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.key },
                        { y: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> y.value })
                )

        // ADD ALL DIYANET TIME CALCULATIONS
        return if (diyanetPrayerTimeTypesHashMap.size > 0) {
            try {
                val diyanetTime = HttpAPIRequestUtil.RetrieveDiyanetTimes(cityAddress)
                if (diyanetTime != null) {
                    diyanetPrayerTimeTypesHashMap.entries.stream().collect(
                        Collectors.toMap(
                            { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.key },
                            { y: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> diyanetTime })
                    )
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
        val alAdhanPrayerTimeTypesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> =
            toBeCalculatedPrayerTimes.entries.stream()
                .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.value.api === ESupportedAPIs.AlAdhan }
                .collect(
                    Collectors.toMap(
                        { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.key },
                        { y: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> y.value })
                )
        val alAdhanTimesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> =
            HashMap()

        // ADD ALL DIYANET TIME CALCULATIONS
        if (alAdhanPrayerTimeTypesHashMap.size > 0) {
            try {
                for ((key, value) in alAdhanPrayerTimeTypesHashMap) {
                    val fajrDegree = value.fajrCalculationDegree
                    //Double ishtibaqAnNujumDegree = prayerTimeWithMoment.getValue().getFajrCalculationDegree();
                    val ishaDegree = value.ishaCalculationDegree
                    val alAdhanTime = HttpAPIRequestUtil.RetrieveAlAdhanTimes(
                        location,
                        fajrDegree,
                        ishaDegree,
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
        return alAdhanTimesHashMap
    }

    @Throws(Exception::class)
    fun RetrieveMuwaqqitTimeData(
        toBeCalculatedPrayerTimes: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity>,
        location: CustomLocation
    ): Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> {
        val muwaqqitTimesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> =
            HashMap()
        val muwaqqitPrayerTimeTypesHashMap = toBeCalculatedPrayerTimes.entries.stream()
            .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.value.api === ESupportedAPIs.Muwaqqit }
            .collect(
                Collectors.toMap(
                    { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.key },
                    { y: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> y.value })
            )
        val fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> =
            muwaqqitPrayerTimeTypesHashMap.entries.stream()
                .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> ->
                    PrayerTimeBeginningEndSettingsEntity.DEGREE_TYPES.contains(
                        x.key
                    )
                }
                .collect(
                    Collectors.toMap(
                        { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.key },
                        { y: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> y.value })
                )
        val nonFajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> =
            muwaqqitPrayerTimeTypesHashMap.entries.stream()
                .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> ->
                    !PrayerTimeBeginningEndSettingsEntity.DEGREE_TYPES.contains(
                        x.key
                    )
                }
                .collect(
                    Collectors.toMap(
                        { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.key },
                        { y: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> y.value })
                )
        var asrKarahaDegree: Double? = null
        if (AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Asr]!!.subPrayer1Settings != null
            &&
            AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Asr]!!.subPrayer1Settings!!.isEnabled2
        ) {
            asrKarahaDegree =
                AppEnvironment.prayerSettingsByPrayerType[EPrayerTimeType.Asr]!!.subPrayer1Settings!!.asrKarahaDegree
        }
        if (fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.size > 0) {
            val fajrDegreeMuwaqqitTimesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> =
                fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.entries.stream()
                    .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> ->
                        PrayerTimeBeginningEndSettingsEntity.FAJR_DEGREE_TYPES.contains(
                            x.key
                        )
                    }
                    .collect(
                        Collectors.toMap(
                            { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.key },
                            { y: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> y.value })
                    )
            val ishaDegreeMuwaqqitTimesHashMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> =
                fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.entries.stream()
                    .filter { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> ->
                        PrayerTimeBeginningEndSettingsEntity.ISHA_DEGREE_TYPES.contains(
                            x.key
                        )
                    }
                    .collect(
                        Collectors.toMap(
                            { x: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> x.key },
                            { y: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> -> y.value })
                    )

            // CALCULATIONS FOR MERGABLE DEGREE TIMES
            while (fajrDegreeMuwaqqitTimesHashMap.size > 0 && ishaDegreeMuwaqqitTimesHashMap.size > 0) {
                val fajrDegreeEntry: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> =
                    fajrDegreeMuwaqqitTimesHashMap.entries.stream().findFirst().get()
                val ishaDegreeEntry: Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> =
                    ishaDegreeMuwaqqitTimesHashMap.entries.stream().findFirst().get()
                val fajrDegreeSettingsEntity = fajrDegreeEntry.value
                val ishaDegreeSettingsEntity = ishaDegreeEntry.value
                val fajrDegree = fajrDegreeSettingsEntity.fajrCalculationDegree
                val ishaDegree = ishaDegreeSettingsEntity.ishaCalculationDegree
                val degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(
                    location,
                    fajrDegree,
                    ishaDegree,
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
                fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.remove(fajrDegreeEntry.key)
                fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.remove(ishaDegreeEntry.key)
            }

            // CALCULATIONS FOR NON MERGABLE DEGREE TIMES
            for ((key, settingsEntity) in fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap) {
                val fajrDegree = settingsEntity.fajrCalculationDegree
                val ishaDegree = settingsEntity.ishaCalculationDegree
                val degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(
                    location,
                    fajrDegree,
                    ishaDegree,
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
        if (nonFajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.size > 0) {
            val nonDegreeMuwaqqitTimeEntity: PrayerTimePackageAbstractClass?

            // any other muwaqqit request will suffice
            nonDegreeMuwaqqitTimeEntity = if (muwaqqitTimesHashMap.size > 0) {
                muwaqqitTimesHashMap.values.stream().findFirst().get()
            } else {
                HttpAPIRequestUtil.RetrieveMuwaqqitTimes(location, null, null, asrKarahaDegree)
            }
            if (nonDegreeMuwaqqitTimeEntity != null) {
                for (prayerTimeType in nonFajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.keys) {
                    muwaqqitTimesHashMap[prayerTimeType] = nonDegreeMuwaqqitTimeEntity
                }
            }
        }
        if (asrKarahaDegree != null) {
            var asrKarahaTimePackage: PrayerTimePackageAbstractClass? = null

            // any other muwaqqit request will suffice
            asrKarahaTimePackage = if (muwaqqitTimesHashMap.values.stream().findFirst().isPresent) {
                muwaqqitTimesHashMap.values.stream().findFirst().get()
            } else {
                HttpAPIRequestUtil.RetrieveMuwaqqitTimes(location, null, null, asrKarahaDegree)
            }
            if (asrKarahaTimePackage != null) {
                muwaqqitTimesHashMap[AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(
                    EPrayerTimeType.Asr,
                    EPrayerTimeMomentType.SubTimeOne
                )] = asrKarahaTimePackage
                muwaqqitTimesHashMap[AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(
                    EPrayerTimeType.Asr,
                    EPrayerTimeMomentType.SubTimeTwo
                )] = asrKarahaTimePackage
            }
        }
        return muwaqqitTimesHashMap
    }
}