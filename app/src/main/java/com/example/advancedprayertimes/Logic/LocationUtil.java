package com.example.advancedprayertimes.Logic;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.example.advancedprayertimes.Logic.Entities.CustomLocation;

import java.util.List;
import java.util.Locale;

public class LocationUtil
{
    public static Address RetrieveCityByLocation(Context context, CustomLocation location) throws Exception
    {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),5);

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
