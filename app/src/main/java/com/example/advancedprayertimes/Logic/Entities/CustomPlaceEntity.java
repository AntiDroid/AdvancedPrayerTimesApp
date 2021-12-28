package com.example.advancedprayertimes.Logic.Entities;

import androidx.annotation.NonNull;

import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.crashlytics.buildtools.reloc.org.checkerframework.checker.nullness.compatqual.NonNullType;

public class CustomPlaceEntity
{
    private String _id;

    private double _latitude;
    private double _longitude;

    private String _name;

    public CustomPlaceEntity(@NonNullType Place place)
    {
        this(place.getId(), place.getLatLng().latitude, place.getLatLng().longitude, place.getName());
    }

    public CustomPlaceEntity(String id, double latitude, double longitude, String name)
    {
        this._id = id;
        this._latitude = latitude;
        this._longitude = longitude;
        this._name = name;
    }

    @NonNull
    @Override
    public String toString()
    {
        return this.getName();
    }

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public double getLatitude()
    {
        return _latitude;
    }

    public void setLatitude(double latitude)
    {
        _latitude = latitude;
    }

    public double getLongitude()
    {
        return _longitude;
    }

    public void setLongitude(double longitude)
    {
        _longitude = longitude;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }
}
