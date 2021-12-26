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

import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
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
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class AppEnvironment
{
    public static HashMap<EPrayerTimeType, PrayerTimeSettingsEntity> DayPrayerTimeSettings = new HashMap<>();

    public static Location RetrieveLocation(Context context)
    {
        //TODO: Refactoring of location retrieval
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            new Handler(Looper.getMainLooper()).post(() ->
                    new AlertDialog.Builder(context)
                            .setTitle("MISSING PERMISSION")
                            .setMessage("Location permission was not granted!")
                            .show());
        }

        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(loc == null)
        {
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return loc;
    }

    public static Gson BuildGSON(String timeFormatString)
    {
        DateFormat timeFormat = new SimpleDateFormat(timeFormatString);

        JsonSerializer<Time> ser = new JsonSerializer<Time>()
        {
            @Override
            public JsonElement serialize(Time src, Type typeOfSrc, JsonSerializationContext context)
            {
                if(src == null)
                {
                    return null;
                }

                return new JsonPrimitive(timeFormat.format(src.getTime()));
            }
        };

        JsonDeserializer<Time> deser = new JsonDeserializer<Time>()
        {
            @Override
            public Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                if(json == null)
                {
                    return null;
                }

                try
                {
                    return new Time(timeFormat.parse(json.getAsString()).getTime());
                }
                catch(Exception e)
                {
                    return null;
                }
            }
        };

        return new GsonBuilder()
                .registerTypeAdapter(Time.class, ser)
                .registerTypeAdapter(Time.class, deser).create();
    }
}
