package com.example.advancedprayertimes.Logic.Entities.API_Entities;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class MuwaqqitPrayerTimeDayEntity
{
    // region static fields

    // endregion static fields

    // region fields

    @SerializedName("fajr_time")
    private LocalDateTime _fajrTime;

    @SerializedName("fajr_angle")
    private double _fajrAngle;

    @SerializedName("sunrise_time")
    private LocalDateTime _sunriseTime;
    @SerializedName("duha_time")
    private LocalDateTime _duhaTime;

    @SerializedName("zohr_time")
    private LocalDateTime _dhuhrTime;

    @SerializedName("mithl_time")
    private LocalDateTime _asrMithlTime;
    @SerializedName("mithlain_time")
    private LocalDateTime _asrMithlaynTime;

    @SerializedName("karaha_time")
    private LocalDateTime _asrKarahaTime;
    @SerializedName("karaha_angle")
    private double _asrKarahaAngle;

    @SerializedName("sunset_time")
    private LocalDateTime _maghribTime;

    @SerializedName("esha_time")
    private LocalDateTime _ishaTime;

    @SerializedName("esha_angle")
    private double _ishaAngle;

    @SerializedName("fajr_date")
    private String _fajrDate;

    // endregion fields

    // region constructors

    public MuwaqqitPrayerTimeDayEntity(LocalDateTime fajrTime, LocalDateTime sunriseTime, LocalDateTime duhaTime, LocalDateTime dhuhrTime, LocalDateTime asrMithlTime, LocalDateTime asrMithlaynTime, LocalDateTime maghribTime, LocalDateTime ishaTime, String fajrDate)
    {
        this._fajrTime = fajrTime;

        this._sunriseTime = sunriseTime;
        this._duhaTime = duhaTime;

        this._dhuhrTime = dhuhrTime;
        this._asrMithlTime = asrMithlTime;
        this._asrMithlaynTime = asrMithlaynTime;

        this._maghribTime = maghribTime;
        this._ishaTime = ishaTime;

        this._fajrDate = fajrDate;
    }

    // endregion constructors

    // region getter & setter

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
        return _sunriseTime;
    }

    public void setSunriseTime(LocalDateTime sunrise_time)
    {
        _sunriseTime = sunrise_time;
    }

    public LocalDateTime getDuhaTime()
    {
        return _duhaTime;
    }

    public void setDuhaTime(LocalDateTime duhaTime)
    {
        _duhaTime = duhaTime;
    }

    public LocalDateTime getDhuhrTime()
    {
        return _dhuhrTime;
    }

    public void setDhuhrTime(LocalDateTime dhuhrTime)
    {
        _dhuhrTime = dhuhrTime;
    }

    public LocalDateTime getAsrMithlTime()
    {
        return _asrMithlTime;
    }

    public void setAsrMithlTime(LocalDateTime asrMithlTime)
    {
        _asrMithlTime = asrMithlTime;
    }

    public LocalDateTime getAsrMithlaynTime()
    {
        return _asrMithlaynTime;
    }

    public void setAsrMithlaynTime(LocalDateTime asrMithlaynTime)
    {
        _asrMithlaynTime = asrMithlaynTime;
    }

    public LocalDateTime getAsrKarahaTime()
    {
        return _asrKarahaTime;
    }

    public void setAsrKarahaTime(LocalDateTime asrKarahaTime)
    {
        _asrKarahaTime = asrKarahaTime;
    }

    public double getAsrKarahaAngle()
    {
        return _asrKarahaAngle;
    }

    public void setAsrKarahaAngle(double asrKarahaAngle)
    {
        _asrKarahaAngle = asrKarahaAngle;
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

    public String getFajrDate()
    {
        return _fajrDate;
    }

    public void setFajrDate(String fajrDate)
    {
        _fajrDate = fajrDate;
    }

    public double getFajrAngle()
    {
        return _fajrAngle;
    }

    public void setFajrAngle(double fajrAngle)
    {
        _fajrAngle = fajrAngle;
    }

    public double getIshaAngle()
    {
        return _ishaAngle;
    }

    public void setIshaAngle(double ishaAngle)
    {
        _ishaAngle = ishaAngle;
    }

    // endregion getter & setter

    // region overidden

    // endregion overidden

    // region methods

    // endregion methods

    // region static methods

    // endregion static methods
}
