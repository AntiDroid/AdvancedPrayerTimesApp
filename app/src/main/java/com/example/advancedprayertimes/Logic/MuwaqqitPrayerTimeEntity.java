package com.example.advancedprayertimes.Logic;

import com.google.gson.annotations.SerializedName;

public class MuwaqqitPrayerTimeEntity
{
    @SerializedName("fajr_date")
    private String _fajrDate;

    @SerializedName("fajr_time")
    private String _fajrTime;

    @SerializedName("sunrise_time")
    private String _sunrise_time;

    @SerializedName("zohr_time")
    private String _dhuhrTime;

    @SerializedName("mithl_time")
    private String _asrMithlTime;
    @SerializedName("mithlain_time")
    private String _asrMithlaynTime;

    @SerializedName("sunset_time")
    private String _maghribTime;

    @SerializedName("esha_time")
    private String _ishaTime;

    public String getFajrDate()
    {
        return _fajrDate;
    }

    public void setFajrDate(String fajrDate)
    {
        _fajrDate = fajrDate;
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

    public String getAsrMithlTime()
    {
        return _asrMithlTime;
    }

    public void setAsrMithlTime(String asrMithlTime)
    {
        _asrMithlTime = asrMithlTime;
    }

    public String getAsrMithlaynTime()
    {
        return _asrMithlaynTime;
    }

    public void setAsrMithlaynTime(String asrMithlaynTime)
    {
        _asrMithlaynTime = asrMithlaynTime;
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
