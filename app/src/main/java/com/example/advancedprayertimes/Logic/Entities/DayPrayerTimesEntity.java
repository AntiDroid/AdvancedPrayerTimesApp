package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.sql.Time;
import java.util.Date;

public class DayPrayerTimesEntity
{
    private Time _fajrTime;
    private Time _sunRiseTime;

    private Time _dhuhrTime;
    private Time _asrTime;

    private Time _maghribTime;
    private Time _ishaTime;

    public DayPrayerTimesEntity(
            Time fajrTime,
            Time sunRiseTime,
            Time dhuhrTime,
            Time asrTime,
            Time maghribTime,
            Time ishaTime
    )
    {
        this._fajrTime = fajrTime;
        this._sunRiseTime = sunRiseTime;

        this._dhuhrTime = dhuhrTime;
        this._asrTime = asrTime;

        this._maghribTime = maghribTime;
        this._ishaTime = ishaTime;
    }

    public Time GetTimeByType(EPrayerTimeType prayerTimeType)
    {
        switch (prayerTimeType)
        {
            case IshaEnd:
            case FajrBeginning:
                return this.getFajrTime();

            case FajrEnd:
                return this.getSunRiseTime();

            case DhuhrBeginning:
                return this.getDhuhrTime();

            case DhuhrEnd:
            case AsrBeginning:
                return this.getAsrTime();

            case AsrEnd:
            case MaghribBeginning:
                return this.getMaghribTime();

            case MaghribEnd:
            case IshaBeginning:
                return this.getIshaTime();

            default:
                return null;
        }
    }

    // #########################################################

    public Time getFajrTime()
    {
        return _fajrTime;
    }

    public void setFajrTime(Time fajrTime)
    {
        _fajrTime = fajrTime;
    }

    public Time getSunRiseTime()
    {
        return _sunRiseTime;
    }

    public void setSunRiseTime(Time sunRiseTime)
    {
        _sunRiseTime = sunRiseTime;
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
