package com.example.advancedprayertimes.Logic;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class MuwaqqitPrayerTimeEntity
{
    @SerializedName("fajr_date")
    private String _fajrDate;

    @SerializedName("fajr_time")
    private Date _fajrTime;

    @SerializedName("sunrise_time")
    private Date _sunrise_time;

    @SerializedName("zohr_time")
    private Date _dhuhrTime;

    @SerializedName("mithl_time")
    private Date _asrMithlTime;
    @SerializedName("mithlain_time")
    private Date _asrMithlaynTime;

    @SerializedName("sunset_time")
    private Date _maghribTime;

    @SerializedName("esha_time")
    private Date _ishaTime;

    public String getFajrDate()
    {
        return _fajrDate;
    }

    public void setFajrDate(String fajrDate)
    {
        _fajrDate = fajrDate;
    }

    public Date getFajrTime()
    {
        return _fajrTime;
    }

    public void setFajrTime(Date fajrTime)
    {
        _fajrTime = fajrTime;
    }

    public Date getSunrise_time()
    {
        return _sunrise_time;
    }

    public void setSunrise_time(Date sunrise_time)
    {
        _sunrise_time = sunrise_time;
    }

    public Date getDhuhrTime()
    {
        return _dhuhrTime;
    }

    public void setDhuhrTime(Date dhuhrTime)
    {
        _dhuhrTime = dhuhrTime;
    }

    public Date getAsrMithlTime()
    {
        return _asrMithlTime;
    }

    public void setAsrMithlTime(Date asrMithlTime)
    {
        _asrMithlTime = asrMithlTime;
    }

    public Date getAsrMithlaynTime()
    {
        return _asrMithlaynTime;
    }

    public void setAsrMithlaynTime(Date asrMithlaynTime)
    {
        _asrMithlaynTime = asrMithlaynTime;
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
