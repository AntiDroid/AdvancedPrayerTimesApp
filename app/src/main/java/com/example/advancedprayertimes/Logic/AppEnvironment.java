package com.example.advancedprayertimes.Logic;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.example.advancedprayertimes.Logic.Entities.CustomPlaceEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.google.android.libraries.places.api.model.Place;
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
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AppEnvironment
{
    public static HashMap<EPrayerTimeType, PrayerTimeSettingsEntity> PrayerTimeSettingsByPrayerTimeTypeHashMap = new HashMap<>();

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
                if(src == null)
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
                if(json == null)
                {
                    return null;
                }

                try
                {
                    return new Date(timeFormat.parse(json.getAsString()).getTime());
                }
                catch(Exception e)
                {
                    return null;
                }
            }
        };

        return new GsonBuilder()
                .registerTypeAdapter(Date.class, ser)
                .registerTypeAdapter(Date.class, deser).create();
    }
}
