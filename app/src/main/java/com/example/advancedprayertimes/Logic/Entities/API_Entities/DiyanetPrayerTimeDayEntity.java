package com.example.advancedprayertimes.Logic.Entities.API_Entities;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class DiyanetPrayerTimeDayEntity
{
    // region static fields

    // endregion static fields

    // region fields

    @SerializedName("MiladiTarihKisa")
    private String _date;

    @SerializedName("Imsak")
    private LocalDateTime _fajrTime;

    @SerializedName("Gunes")
    private LocalDateTime _sunRiseTime;

    @SerializedName("Ogle")
    private LocalDateTime _dhuhrTime;

    @SerializedName("Ikindi")
    private LocalDateTime _asrTime;

    @SerializedName("Aksam")
    private LocalDateTime _maghribTime;

    @SerializedName("Yatsi")
    private LocalDateTime _ishaTime;

    // endregion fields

    // region constructors

    // endregion constructors

    // region getter & setter

    public String getDate()
    {
        return _date;
    }

    public void setLocalDateTime (String date)
    {
        _date = date ;
    }

    public LocalDateTime getFajrTime()
    {
        return _fajrTime;
    }

    public void setFajrTime(LocalDateTime fajrTime)
    {
        _fajrTime = fajrTime;
    }

    public LocalDateTime getSunriseTime()
    {
        return _sunRiseTime;
    }

    public void setSunRiseTime(LocalDateTime sunRiseTime)
    {
        _sunRiseTime = sunRiseTime;
    }

    public LocalDateTime getDhuhrTime()
    {
        return _dhuhrTime;
    }

    public void setDhuhrTime(LocalDateTime dhuhrTime)
    {
        _dhuhrTime = dhuhrTime;
    }

    public LocalDateTime getAsrTime()
    {
        return _asrTime;
    }

    public void setAsrTime(LocalDateTime asrTime)
    {
        _asrTime = asrTime;
    }

    public LocalDateTime getMaghribTime()
    {
        return _maghribTime;
    }

    public void setMaghribTime(LocalDateTime maghribTime)
    {
        _maghribTime = maghribTime;
    }

    public LocalDateTime getIshaTime()
    {
        return _ishaTime;
    }

    public void setIshaTime(LocalDateTime ishaTime)
    {
        _ishaTime = ishaTime;
    }

    // endregion getter & setter

    // region overidden

    // endregion overidden

    // region methods

    // endregion methods

    // region static methods

    // endregion static methods
}
