package com.example.advancedprayertimes.Logic.Entities;

public class CustomLocation
{
    // region fields

    private double _longitude;
    private double _latitude;
    private String _timezone;

    // endregion fields

    // region constructors

    public CustomLocation(double longitude, double latitude, String timezone)
    {
        this._longitude = longitude;
        this._latitude = latitude;
        this._timezone = timezone;
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

    public String getTimezone()
    {
        return _timezone;
    }

    public void setTimezone(String timezone)
    {
        _timezone = timezone;
    }

    // endregion getter & setter
}
