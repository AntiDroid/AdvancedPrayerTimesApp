package com.example.advancedprayertimes.Logic.Entities;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class DiyanetPrayerTimeDayEntity
{
    @SerializedName("MiladiTarihKisa")
    private String _date;

    @SerializedName("Imsak")
    private Date _fajrTime;

    @SerializedName("Gunes")
    private Date _sunRiseTime;

    @SerializedName("Ogle")
    private Date _dhuhrTime;

    @SerializedName("Ikindi")
    private Date _asrTime;

    @SerializedName("Aksam")
    private Date _maghribTime;

    @SerializedName("Yatsi")
    private Date _ishaTime;

    public String getDate()
    {
        return _date;
    }

    public void setDate (String date)
    {
        _date = date ;
    }

    public Date getFajrTime()
    {
        return _fajrTime;
    }

    public void setFajrTime(Date fajrTime)
    {
        _fajrTime = fajrTime;
    }

    public Date getSunriseTime()
    {
        return _sunRiseTime;
    }

    public void setSunRiseTime(Date sunRiseTime)
    {
        _sunRiseTime = sunRiseTime;
    }

    public Date getDhuhrTime()
    {
        return _dhuhrTime;
    }

    public void setDhuhrTime(Date dhuhrTime)
    {
        _dhuhrTime = dhuhrTime;
    }

    public Date getAsrTime()
    {
        return _asrTime;
    }

    public void setAsrTime(Date asrTime)
    {
        _asrTime = asrTime;
    }

    public Date getMaghribTime()
    {
        return _maghribTime;
    }

    public void setMaghribTime(Date maghribTime)
    {
        _maghribTime = maghribTime;
    }

    public Date getIshaTime()
    {
        return _ishaTime;
    }

    public void setIshaTime(Date ishaTime)
    {
        _ishaTime = ishaTime;
    }
}
