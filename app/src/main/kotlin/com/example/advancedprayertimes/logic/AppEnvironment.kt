package com.example.advancedprayertimes.logic

import com.example.advancedprayertimes.logic.api_entities.PrayerTimePackageAbstractClass
import com.example.advancedprayertimes.logic.db.DBHelper
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.enums.ESupportedAPIs
import com.example.advancedprayertimes.logic.extensions.parseToDate
import com.example.advancedprayertimes.logic.extensions.parseToDateTime
import com.example.advancedprayertimes.logic.extensions.parseToTime
import com.example.advancedprayertimes.logic.extensions.toStringByFormat
import com.example.advancedprayertimes.logic.setting_entities.PrayerTimeBeginningEndSettingsEntity
import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoField
import java.util.*

object AppEnvironment {

    const val GLOBAL_SHARED_PREFERENCE_NAME = "globalSharedPreference"

    var diyanetTimesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> = HashMap()
    var muwaqqitTimesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> = HashMap()
    var alAdhanTimesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass> = HashMap()

    var timeDate: LocalDateTime? = null

    @JvmField
    var prayerSettingsByPrayerType: HashMap<EPrayerTimeType, PrayerSettingsEntity> =
        object : HashMap<EPrayerTimeType, PrayerSettingsEntity>() {
            init {
                put(EPrayerTimeType.Fajr, PrayerSettingsEntity())
                put(EPrayerTimeType.Duha, PrayerSettingsEntity())
                put(EPrayerTimeType.Dhuhr, PrayerSettingsEntity())
                put(EPrayerTimeType.Asr, PrayerSettingsEntity())
                put(EPrayerTimeType.Maghrib, PrayerSettingsEntity())
                put(EPrayerTimeType.Isha, PrayerSettingsEntity())
            }
        }

    var dbHelper: DBHelper? = null

    @JvmField
    var PlaceEntity: CustomPlaceEntity? = null

    fun GetPrayerTimeSettingsByPrayerTimeTypeHashMap(): Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> {

        val resultMap: MutableMap<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> = HashMap()

        for ((key, value) in prayerSettingsByPrayerType) {

            if (value.beginningSettings != null) {
                resultMap[AbstractMap.SimpleEntry(key, EPrayerTimeMomentType.Beginning)] = value.beginningSettings!!
            }

            if (value.endSettings != null) {
                resultMap[AbstractMap.SimpleEntry(key, EPrayerTimeMomentType.End)] = value.endSettings!!
            }
        }

        return resultMap
    }



    /**
     * Based on the EPrayerTimeType, the correct beginning or ending time will be returned.
     */
    fun getPrayerBeginningOrEndTime(prayerType: EPrayerTimeType, isBeginning: Boolean): LocalTime? {

        val prayerSettings = this.prayerSettingsByPrayerType[prayerType]
            ?: return null

        val prayerBeginningEndSettings = prayerSettings.GetBeginningEndSettingByMomentType(isBeginning)
            ?: return null

        val prayerTypeTimeType: EPrayerTimeMomentType = if(isBeginning) EPrayerTimeMomentType.Beginning else EPrayerTimeMomentType.End
        val prayerTimeWithType = AbstractMap.SimpleEntry(prayerType, prayerTypeTimeType)

        val targetLocalDateTime =
            when(prayerBeginningEndSettings.api) {
                ESupportedAPIs.Muwaqqit -> muwaqqitTimesHashMap[prayerTimeWithType]?.getTimeByType(prayerType, prayerTypeTimeType)
                ESupportedAPIs.Diyanet -> diyanetTimesHashMap[prayerTimeWithType]?.getTimeByType(prayerType, prayerTypeTimeType)
                ESupportedAPIs.AlAdhan -> alAdhanTimesHashMap[prayerTimeWithType]?.getTimeByType(prayerType, prayerTypeTimeType)
                else -> null
            }

        val minuteAdjustment = prayerBeginningEndSettings.minuteAdjustment.toLong()
        return targetLocalDateTime?.plusMinutes(minuteAdjustment)
    }

    /**
     * Based on the EPrayerTimeType, the correct sub time will be returned.
     */
    fun getCorrectSubTime(prayerType: EPrayerTimeType, prayerTypeTimeType: EPrayerTimeMomentType): LocalTime? {

        if(prayerTypeTimeType == EPrayerTimeMomentType.Beginning || prayerTypeTimeType == EPrayerTimeMomentType.End) {
            throw IllegalArgumentException()
        }

        val prayerSettings = this.prayerSettingsByPrayerType[prayerType]
            ?: return null

        val subTimeSettings =
            when (prayerTypeTimeType) {
                EPrayerTimeMomentType.SubTimeOne,
                EPrayerTimeMomentType.SubTimeTwo,
                EPrayerTimeMomentType.SubTimeThree -> prayerSettings.subPrayer1Settings
                else -> null
            }
                ?: return null

        return when (val prayerTimeWithMomentType = AbstractMap.SimpleEntry(prayerType, prayerTypeTimeType)) {

            // ASR
            AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.SubTimeOne) -> {
                if(subTimeSettings.isEnabled1) muwaqqitTimesHashMap[prayerTimeWithMomentType]?.mithlaynTime else null
            }
            AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.SubTimeTwo) -> {
                if(subTimeSettings.isEnabled2) muwaqqitTimesHashMap[prayerTimeWithMomentType]?.asrKarahaTime else null
            }

            // MAGHRIB
            AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.SubTimeOne) -> {
                if(subTimeSettings.isEnabled1) alAdhanTimesHashMap[prayerTimeWithMomentType]?.ishtibaqAnNujumTime else null
            }

            // ISHA
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.SubTimeOne),
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.SubTimeTwo) -> {

                if(PrayerTimeEntity.NightDurationMS == null || !subTimeSettings.isEnabled1) {
                    null
                } else {

                    val thirdOfNight = PrayerTimeEntity.NightDurationMS!! / 3
                    val thirdsCount = if (prayerTypeTimeType == EPrayerTimeMomentType.SubTimeOne) 1 else 2

                    PrayerTimeEntity.Maghrib.beginningTime!!.plus(
                        thirdsCount * thirdOfNight,
                        ChronoField.MILLI_OF_DAY.baseUnit
                    )
                }
            }
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.SubTimeThree) -> {

                if(PrayerTimeEntity.NightDurationMS == null || !subTimeSettings.isEnabled2) {
                    null
                } else {
                    val halfOfNight = PrayerTimeEntity.NightDurationMS!! / 2
                    PrayerTimeEntity.Maghrib.beginningTime!!.plus(halfOfNight, ChronoField.MILLI_OF_DAY.baseUnit)
                }
            }

            else -> null
        }
    }

    fun mapTimeDataToTimesEntities() {
        for (prayerTimeEntity in PrayerTimeEntity.Prayers) {

            val beginningTime = this.getPrayerBeginningOrEndTime(prayerTimeEntity.prayerTimeType, isBeginning = true)
            val endTime = this.getPrayerBeginningOrEndTime(prayerTimeEntity.prayerTimeType, isBeginning = false)

            val subtimeOneTime = this.getCorrectSubTime(prayerTimeEntity.prayerTimeType, EPrayerTimeMomentType.SubTimeOne)
            val subtimeTwoTime = this.getCorrectSubTime(prayerTimeEntity.prayerTimeType, EPrayerTimeMomentType.SubTimeTwo)
            val subtimeThreeTime = this.getCorrectSubTime(prayerTimeEntity.prayerTimeType, EPrayerTimeMomentType.SubTimeThree)

            prayerTimeEntity.beginningTime = beginningTime
            prayerTimeEntity.endTime = endTime
            prayerTimeEntity.subtime1BeginningTime = beginningTime
            prayerTimeEntity.subtime1EndTime = subtimeOneTime
            prayerTimeEntity.subtime2BeginningTime = subtimeOneTime
            prayerTimeEntity.subtime2EndTime = subtimeTwoTime
            prayerTimeEntity.subtime3BeginningTime = subtimeTwoTime
            prayerTimeEntity.subtime3EndTime = subtimeThreeTime
        }
    }

    @JvmStatic
    fun buildGSON(
        dateFormatString: String,
        timeFormatString: String,
        dateTimeFormatString: String
    ): Gson {

        val serToDate = JsonSerializer { src: LocalDate, _: Type?, _: JsonSerializationContext? ->
                JsonPrimitive(src.toStringByFormat(dateFormatString))
            }
        val deserToDate =
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                json.asString.parseToDate(dateFormatString)
            }

        val serToTime = JsonSerializer { src: LocalTime, _: Type?, _: JsonSerializationContext? ->
                JsonPrimitive(src.toStringByFormat(timeFormatString))
            }
        val deserToTime =
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                json.asString.parseToTime(timeFormatString)
            }

        val serToDateTime = JsonSerializer { src: LocalDateTime, _: Type?, _: JsonSerializationContext? ->
                JsonPrimitive(src.toStringByFormat(dateTimeFormatString))
            }
        val deserToDateTime =
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                json.asString.parseToDateTime(dateTimeFormatString)
            }

        return GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate::class.java, serToDate)
            .registerTypeAdapter(LocalDate::class.java, deserToDate)
            .registerTypeAdapter(LocalTime::class.java, serToTime)
            .registerTypeAdapter(LocalTime::class.java, deserToTime)
            .registerTypeAdapter(LocalDateTime::class.java, serToDateTime)
            .registerTypeAdapter(LocalDateTime::class.java, deserToDateTime)
            .create()
    }
}