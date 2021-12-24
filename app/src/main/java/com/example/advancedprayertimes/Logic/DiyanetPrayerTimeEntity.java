package com.example.advancedprayertimes.Logic;

import com.google.gson.annotations.SerializedName;

public class DiyanetPrayerTimeEntity
{
    @SerializedName("MiladiTarihKisa")
    private String _date;

    @SerializedName("Imsak")
    private String _fajrTime;

    @SerializedName("Gunes")
    private String _sunrise_time;

    @SerializedName("Ogle")
    private String _dhuhrTime;

    @SerializedName("Ikindi")
    private String _asrTime;

    @SerializedName("Aksam")
    private String _maghribTime;

    @SerializedName("Yatsi")
    private String _ishaTime;

    public String getDate()
    {
        return _date;
    }

    public void setDate(String date)
    {
        _date = date;
    }

    public String getFajrTime()
    {
        return _fajrTime;
    }

    public void setFajrTime(String fajrTime)
    {
        _fajrTime = fajrTime;
    }

    public String getSunrise_time()
    {
        return _sunrise_time;
    }

    public void setSunrise_time(String sunrise_time)
    {
        _sunrise_time = sunrise_time;
    }

    public String getDhuhrTime()
    {
        return _dhuhrTime;
    }

    public void setDhuhrTime(String dhuhrTime)
    {
        _dhuhrTime = dhuhrTime;
    }

    public String getAsrTime()
    {
        return _asrTime;
    }

    public void setAsrTime(String asrTime)
    {
        _asrTime = asrTime;
    }

    public String getMaghribTime()
    {
        return _maghribTime;
    }

    public void setMaghribTime(String maghribTime)
    {
        _maghribTime = maghribTime;
    }

    public String getIshaTime()
    {
        return _ishaTime;
    }

    public void setIshaTime(String ishaTime)
    {
        _ishaTime = ishaTime;
    }
}
