package com.example.advancedprayertimes.Logic;

import com.example.advancedprayertimes.Logic.DB.DBHelper;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.CustomPlaceEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerSettingsEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerTimeBeginningEndSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeMomentType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class AppEnvironment
{
    public static final String GLOBAL_SHARED_PREFERENCE_NAME = "globalSharedPreference";

    public static HashMap<EPrayerTimeType, PrayerSettingsEntity> prayerSettingsByPrayerType = new HashMap<EPrayerTimeType, PrayerSettingsEntity>()
    {
        {
            put(EPrayerTimeType.Fajr, new PrayerSettingsEntity());
            put(EPrayerTimeType.Duha, new PrayerSettingsEntity());
            put(EPrayerTimeType.Dhuhr, new PrayerSettingsEntity());
            put(EPrayerTimeType.Asr, new PrayerSettingsEntity());
            put(EPrayerTimeType.Maghrib, new PrayerSettingsEntity());
            put(EPrayerTimeType.Isha, new PrayerSettingsEntity());
        }
    };

//    public static HashMap<EPrayerTimeType, PrayerSettingsEntity> GetPrayerSettingsByPrayerType()
//    {
//        for(EPrayerTimeType prayerTimeType : EPrayerTimeType.values())
//        {
//            String key = DataManagementUtil.GetPrayerTimeEntityKeyForSharedPreference(prayerTimeType);
//            String storedValue = sharedPref.getString(key, null);
//
//            if(storedValue != null)
//            {
//                PrayerTimeEntity retrievedPrayer = gson.fromJson(storedValue, PrayerTimeEntity.class);
//
//                if(retrievedPrayer != null)
//                {
//                    PrayerTimeEntity.Prayers.set(i, retrievedPrayer);
//                }
//            }
//        }
//    }

    public static DBHelper dbHelper = null;
    public static CustomPlaceEntity PlaceEntity = null;

    public static Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> GetPrayerTimeSettingsByPrayerTimeTypeHashMap()
    {
        Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> resultMap = new HashMap<>();

        for(Map.Entry<EPrayerTimeType, PrayerSettingsEntity> entry : AppEnvironment.prayerSettingsByPrayerType.entrySet())
        {
            if(entry.getValue().getBeginningSettings() != null)
            {
                resultMap.put(new AbstractMap.SimpleEntry(entry.getKey(), EPrayerTimeMomentType.Beginning), entry.getValue().getBeginningSettings());
            }

            if(entry.getValue().getEndSettings() != null)
            {
                resultMap.put(new AbstractMap.SimpleEntry(entry.getKey(), EPrayerTimeMomentType.End), entry.getValue().getEndSettings());
            }
        }

        return resultMap;
    }

    public static Gson BuildGSON(String dateTimeFormatString)
    {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(dateTimeFormatString);

        JsonSerializer<LocalDateTime> ser = (src, typeOfSrc, context) ->
        {
            if (src == null)
            {
                return null;
            }

            return new JsonPrimitive(src.format(timeFormat));
        };

        JsonDeserializer<LocalDateTime> deser = (json, typeOfT, context) ->
        {
            if (json == null)
            {
                return null;
            }

            try
            {
                return LocalTime.parse(json.getAsString(), timeFormat).atDate(LocalDateTime.now().toLocalDate());
            }
            catch (Exception e)
            {
                return null;
            }
        };

        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, ser)
                .registerTypeAdapter(LocalDateTime.class, deser)
                .create();
    }
}
