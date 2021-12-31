package com.example.advancedprayertimes.Logic;

import android.content.Context;

import com.example.advancedprayertimes.Logic.Entities.CustomPlaceEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerPointInTimeType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AppEnvironment
{
    public static HashMap<EPrayerPointInTimeType, PrayerTimeSettingsEntity> PrayerTimeSettingsByPrayerTimeTypeHashMap = new HashMap<>();

    public static Context context = null;

    public static CustomPlaceEntity place;

    public static Gson BuildGSON(String dateTimeFormatString)
    {
        DateFormat timeFormat = new SimpleDateFormat(dateTimeFormatString);

        JsonSerializer<Date> ser = new JsonSerializer<Date>()
        {
            @Override
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context)
            {
                if (src == null)
                {
                    return null;
                }

                return new JsonPrimitive(timeFormat.format(src.getTime()));
            }
        };

        JsonDeserializer<Date> deser = new JsonDeserializer<Date>()
        {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                if (json == null)
                {
                    return null;
                }

                try
                {
                    return new Date(timeFormat.parse(json.getAsString()).getTime());
                } catch (Exception e)
                {
                    return null;
                }
            }
        };

        return new GsonBuilder()
                .registerTypeAdapter(Date.class, ser)
                .registerTypeAdapter(Date.class, deser).create();
    }

    public static EPrayerPointInTimeType GetPointInTimeByPrayerType(EPrayerTimeType prayerTimeType, boolean isBeginning)
    {
        switch (prayerTimeType)
        {
            case Fajr:
                return isBeginning ?
                        EPrayerPointInTimeType.FajrBeginning :
                        EPrayerPointInTimeType.FajrEnd;

            case Dhuhr:
                return isBeginning ?
                        EPrayerPointInTimeType.DhuhrBeginning :
                        EPrayerPointInTimeType.DhuhrEnd;

            case Asr:
                return isBeginning ?
                        EPrayerPointInTimeType.AsrBeginning :
                        EPrayerPointInTimeType.AsrEnd;

            case Maghrib:
                return isBeginning ?
                        EPrayerPointInTimeType.MaghribBeginning :
                        EPrayerPointInTimeType.MaghribEnd;

            case Isha:
                return isBeginning ?
                        EPrayerPointInTimeType.IshaBeginning :
                        EPrayerPointInTimeType.IshaEnd;
        }

        return null;
    }

    public static EPrayerTimeType GetPrayerByPointInTime(EPrayerPointInTimeType pointInTimeType)
    {
        switch (pointInTimeType)
        {
            case FajrBeginning:
            case FajrEnd:
                return EPrayerTimeType.Fajr;

            case DhuhrBeginning:
            case DhuhrEnd:
                return EPrayerTimeType.Dhuhr;

            case AsrBeginning:
            case AsrEnd:
                return EPrayerTimeType.Asr;

            case MaghribBeginning:
            case MaghribEnd:
                return EPrayerTimeType.Maghrib;

            case IshaBeginning:
            case IshaEnd:
                return EPrayerTimeType.Isha;
        }

        return null;
    }
}
