package com.example.advancedprayertimes.logic

import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.db.DBHelper
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import com.example.advancedprayertimes.logic.setting_entities.PrayerTimeBeginningEndSettingsEntity
import com.google.gson.Gson
import com.google.gson.JsonSerializer
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonPrimitive
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext
import com.google.gson.GsonBuilder
import java.lang.Exception
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.AbstractMap
import java.util.HashMap

object AppEnvironment {

    const val GLOBAL_SHARED_PREFERENCE_NAME = "globalSharedPreference"

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

    @JvmField
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

    @JvmStatic
    fun BuildGSON(dateTimeFormatString: String?): Gson {

        val timeFormat = DateTimeFormatter.ofPattern(dateTimeFormatString)
        val ser = JsonSerializer { src: LocalDateTime?, _: Type?, _: JsonSerializationContext? ->
                if (src == null) {
                    null
                }
                JsonPrimitive(src!!.format(timeFormat))
            }

        val deser =
            JsonDeserializer<LocalDateTime> { json: JsonElement?, _: Type?, _: JsonDeserializationContext? ->
                if (json == null) {
                    null
                }

                try {
                    LocalTime.parse(json!!.asString, timeFormat)
                        .atDate(LocalDateTime.now().toLocalDate())
                } catch (e: Exception) {
                    null
                }
            }

        return GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime::class.java, ser)
            .registerTypeAdapter(LocalDateTime::class.java, deser)
            .create()
    }
}