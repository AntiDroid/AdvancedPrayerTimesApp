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

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.util.HashMap;

public class AppEnvironment
{
    public static HashMap<EPrayerTimeType, DayPrayerTimeSettingsEntity> DayPrayerTimeSettings = new HashMap<>();

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
}
