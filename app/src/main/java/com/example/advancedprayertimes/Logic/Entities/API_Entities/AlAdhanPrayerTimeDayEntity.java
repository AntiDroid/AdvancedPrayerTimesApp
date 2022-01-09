package com.example.advancedprayertimes.Logic.Entities.API_Entities;

import com.example.advancedprayertimes.Logic.Entities.PrayerTimePackageAbstractClass;

import java.time.LocalDateTime;

import kotlin.NotImplementedError;

public class AlAdhanPrayerTimeDayEntity extends PrayerTimePackageAbstractClass
{
    // region static fields

    // endregion static fields

    // region fields

    private LocalDateTime _fajrTime;
    private LocalDateTime _sunriseTime;

    private LocalDateTime _dhuhrTime;
    private LocalDateTime _asrTime;
    private LocalDateTime _mitlhaynTime;

    private LocalDateTime _maghribTime;
    private LocalDateTime _ishtibaqAnNujumTime;
    private LocalDateTime _ishaTime;

    private String _date;

    // endregion fields

    // region constructors

    public AlAdhanPrayerTimeDayEntity(
        LocalDateTime fajrTime,
        LocalDateTime sunriseTime,
        LocalDateTime dhuhrTime,
        LocalDateTime asrTime,
        LocalDateTime mitlhaynTime,
        LocalDateTime maghribTime,
        LocalDateTime ishtibaqAnNujumTime,
        LocalDateTime ishaTime
        )
    {
        this._fajrTime = fajrTime;
        this._sunriseTime = sunriseTime;
        this._dhuhrTime = dhuhrTime;
        this._asrTime = asrTime;
        this._mitlhaynTime = mitlhaynTime;
        this._maghribTime = maghribTime;
        this._ishtibaqAnNujumTime = ishtibaqAnNujumTime;
        this._ishaTime = ishaTime;
    }

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
        _date = date;
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
        return _sunriseTime;
    }

    @Override
    public void setSunriseTime(LocalDateTime sunriseTime)
    {
        _sunriseTime = sunriseTime;
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
        return _mitlhaynTime;
    }

    @Override
    public void setMithlaynTime(LocalDateTime mithlaynTime)
    {
        _mitlhaynTime = mithlaynTime;
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
        return this._ishtibaqAnNujumTime;
    }

    @Override
    public void setIshtibaqAnNujumTime(LocalDateTime ishtibaqAnNujum)
    {
        this._ishtibaqAnNujumTime = ishtibaqAnNujum;
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
