package com.example.advancedprayertimes.Logic.Entities.API_Entities;

import com.example.advancedprayertimes.Logic.Entities.PrayerTimePackageAbstractClass;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

import kotlin.NotImplementedError;

public class DiyanetPrayerTimeDayEntity extends PrayerTimePackageAbstractClass
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

    @Override
    public String getDate()
    {
        return _date;
    }

    @Override
    public void setDate(String date)
    {
        _date = date ;
    }

    @Override
    public LocalDateTime getFajrTime()
    {
        return _fajrTime;
    }

    @Override
    public void setFajrTime(LocalDateTime fajrTime)
    {
        _fajrTime = fajrTime;
    }

    @Override
    public LocalDateTime getSunriseTime()
    {
        return _sunRiseTime;
    }

    @Override
    public void setSunriseTime(LocalDateTime sunriseTime)
    {
        _sunRiseTime = sunriseTime;
    }

    @Override
    public LocalDateTime getDuhaTime()
    {
        return null;
    }

    @Override
    public void setDuhaTime(LocalDateTime duhaTime)
    {
        throw new NotImplementedError();
    }

    @Override
    public LocalDateTime getDhuhrTime()
    {
        return _dhuhrTime;
    }

    @Override
    public void setDhuhrTime(LocalDateTime dhuhrTime)
    {
        _dhuhrTime = dhuhrTime;
    }

    @Override
    public LocalDateTime getAsrTime()
    {
        return _asrTime;
    }

    @Override
    public void setAsrTime(LocalDateTime asrTime)
    {
        _asrTime = asrTime;
    }

    @Override
    public LocalDateTime getMithlaynTime()
    {
        return null;
    }

    @Override
    public void setMithlaynTime(LocalDateTime mithlaynTime)
    {
        throw new NotImplementedError();
    }

    @Override
    public LocalDateTime getAsrKarahaTime()
    {
        return null;
    }

    @Override
    public void setAsrKarahaTime(LocalDateTime asrKarahaTime)
    {
        throw new NotImplementedError();
    }

    @Override
    public LocalDateTime getMaghribTime()
    {
        return _maghribTime;
    }

    @Override
    public void setMaghribTime(LocalDateTime maghribTime)
    {
        _maghribTime = maghribTime;
    }

    @Override
    public LocalDateTime getIshtibaqAnNujumTime()
    {
        return null;
    }

    @Override
    public void setIshtibaqAnNujumTime(LocalDateTime ishtibaqAnNujumTime)
    {
        throw new NotImplementedError();
    }

    @Override
    public LocalDateTime getIshaTime()
    {
        return _ishaTime;
    }

    @Override
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
