package com.example.advancedprayertimes.Logic.Entities.API_Entities;

import android.location.Address;

import androidx.annotation.NonNull;

import com.example.advancedprayertimes.Logic.Entities.CustomLocation;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.crashlytics.buildtools.reloc.org.checkerframework.checker.nullness.compatqual.NonNullType;

public class CustomPlaceEntity
{
    // region static fields

    // endregion static fields

    // region fields

    private String _id = null;
    private String _name = null;

    private CustomLocation _location = null;

    // endregion fields

    // region constructors

    public CustomPlaceEntity(@NonNullType Place place)
    {
        this(place.getId(), place.getLatLng().latitude, place.getLatLng().longitude, place.getName());
    }

    public CustomPlaceEntity(Address address)
    {
        this(null, address.getLatitude(), address.getLongitude(), address.getLocality());
    }

    public CustomPlaceEntity(String id, double latitude, double longitude, String name)
    {
        this._id = id;
        this._location = new CustomLocation(longitude, latitude, "");
        this._name = name;
    }

    // endregion constructors

    // region getter & setter

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public CustomLocation getLocation()
    {
        return _location;
    }

    public void setLocation(CustomLocation location)
    {
        _location = location;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    // endregion getter & setter

    // region overidden

    @NonNull
    @Override
    public String toString()
    {
        return this.getName();
    }

    // endregion overidden

    // region methods

    // endregion methods

    // region static methods

    // endregion static methods
}
