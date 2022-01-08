package com.example.advancedprayertimes.Logic.Entities;

public class CustomLocation
{
    // region fields

    private double _longitude;
    private double _latitude;

    // endregion fields

    // region constructors

    public CustomLocation(double longitude, double latitude)
    {
        this._longitude = longitude;
        this._latitude = latitude;
    }

    // endregion constructors

    // region getter & setter

    public double getLongitude()
    {
        return _longitude;
    }

    public void setLongitude(double longitude)
    {
        _longitude = longitude;
    }

    public double getLatitude()
    {
        return _latitude;
    }

    public void setLatitude(double latitude)
    {
        _latitude = latitude;
    }

    // endregion getter & setter
}
