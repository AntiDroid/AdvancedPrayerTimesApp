package com.example.advancedprayertimes.Logic.Entities;

import com.google.gson.annotations.SerializedName;

import java.sql.Time;
import java.util.Date;

public class MuwaqqitPrayerTimeDayEntity
{
    @SerializedName("fajr_date")
    private String _fajrDate;

    @SerializedName("fajr_time")
    private Time _fajrTime;

    @SerializedName("sunrise_time")
    private Time _sunrise_time;

    @SerializedName("zohr_time")
    private Time _dhuhrTime;

    @SerializedName("mithl_time")
    private Time _asrMithlTime;
    @SerializedName("mithlain_time")
    private Time _asrMithlaynTime;

    @SerializedName("sunset_time")
    private Time _maghribTime;

    @SerializedName("esha_time")
    private Time _ishaTime;

    public String getFajrDate()
    {
        return _fajrDate;
    }

    public void setFajrDate(String fajrDate)
    {
        _fajrDate = fajrDate;
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

    public void setSunrise_Time(Time sunrise_time)
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

    public Time getAsrMithlTime()
    {
        return _asrMithlTime;
    }

    public void setAsrMithlTime(Time asrMithlTime)
    {
        _asrMithlTime = asrMithlTime;
    }

    public Time getAsrMithlaynTime()
    {
        return _asrMithlaynTime;
    }

    public void setAsrMithlaynTime(Time asrMithlaynTime)
    {
        _asrMithlaynTime = asrMithlaynTime;
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
