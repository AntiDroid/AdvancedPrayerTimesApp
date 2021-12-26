package com.example.advancedprayertimes.Logic.Entities;

import com.google.gson.annotations.SerializedName;

import java.sql.Time;

public class DiyanetPrayerTimeDayEntity
{
    @SerializedName("MiladiTarihKisa")
    private String _date;

    @SerializedName("Imsak")
    private Time _fajrTime;

    @SerializedName("Gunes")
    private Time _sunrise_time;

    @SerializedName("Ogle")
    private Time _dhuhrTime;

    @SerializedName("Ikindi")
    private Time _asrTime;

    @SerializedName("Aksam")
    private Time _maghribTime;

    @SerializedName("Yatsi")
    private Time _ishaTime;

    public String getDate ()
    {
        return _date;
    }

    public void setTime (String date)
    {
        _date = date ;
    }

    public Time getFajrTime()
    {
        return _fajrTime;
    }

    public void setFajrTime(Time fajrTime)
    {
        _fajrTime = fajrTime;
    }

    public Time getSunrise_time()
    {
        return _sunrise_time;
    }

    public void setSunrise_time(Time sunrise_time)
    {
        _sunrise_time = sunrise_time;
    }

    public Time getDhuhrTime()
    {
        return _dhuhrTime;
    }

    public void setDhuhrTime(Time dhuhrTime)
    {
        _dhuhrTime = dhuhrTime;
    }

    public Time getAsrTime()
    {
        return _asrTime;
    }

    public void setAsrTime(Time asrTime)
    {
        _asrTime = asrTime;
    }

    public Time getMaghribTime()
    {
        return _maghribTime;
    }

    public void setMaghribTime(Time maghribTime)
    {
        _maghribTime = maghribTime;
    }

    public Time getIshaTime()
    {
        return _ishaTime;
    }

    public void setIshaTime(Time ishaTime)
    {
        _ishaTime = ishaTime;
    }
}
