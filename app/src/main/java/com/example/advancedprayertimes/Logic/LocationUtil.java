package com.example.advancedprayertimes.Logic;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class LocationUtil
{


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

    public static Address RetrieveCityByLocation(Context context, Location location) throws Exception
    {
        Geocoder geocoder = new Geocoder(context);

        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

        if (Geocoder.isPresent() && addresses.size() > 0)
        {
            return addresses.get(0);
        }

        return null;
    }

    public static List<Address> RetrieveCitiesByName(Context context, String name) throws Exception
    {
        Geocoder geocoder = new Geocoder(context);
        return geocoder.getFromLocationName(name, 5);
    }
}
