package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Entities.API_Entities.DiyanetPrayerTimeDayEntity;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.MuwaqqitPrayerTimeDayEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeMomentType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.time.LocalDateTime;

public class DayPrayerTimesPackageEntityX
{
    // region static fields

    // endregion static fields

    // region fields

    private LocalDateTime _fajrTime;

    private LocalDateTime _sunRiseTime;
    private LocalDateTime _duhaTime;

    private LocalDateTime _dhuhrTime;
    private LocalDateTime _asrTime;
    private LocalDateTime _asrMitlhaynTime;
    private LocalDateTime _asrKarahaTime;

    private LocalDateTime _maghribTime;
    private LocalDateTime _ishtibaqAnNujumTime;
    private LocalDateTime _ishaTime;

    // endregion fields

    // region constructors

    public DayPrayerTimesPackageEntityX(MuwaqqitPrayerTimeDayEntity muwaqqitTime)
    {
        this(
                muwaqqitTime.getFajrTime(),
                muwaqqitTime.getSunriseTime(),
                muwaqqitTime.getDuhaTime(),
                muwaqqitTime.getDhuhrTime(),
                muwaqqitTime.getAsrTime(),
                muwaqqitTime.getMithlaynTime(),
                muwaqqitTime.getAsrKarahaTime(),
                muwaqqitTime.getMaghribTime(),
                null,
                muwaqqitTime.getIshaTime()
            );
    }

    public DayPrayerTimesPackageEntityX(DiyanetPrayerTimeDayEntity diyanetTime)
    {
        this(
                diyanetTime.getFajrTime(),
                diyanetTime.getSunriseTime(),
                null,
                diyanetTime.getDhuhrTime(),
                diyanetTime.getAsrTime(),
                null,
                null,
                diyanetTime.getMaghribTime(),
                null,
                diyanetTime.getIshaTime()
            );
    }

    public DayPrayerTimesPackageEntityX(
            LocalDateTime fajrTime,
            LocalDateTime sunRiseTime,
            LocalDateTime duhaTime,
            LocalDateTime dhuhrTime,
            LocalDateTime asrTime,
            LocalDateTime asrmitlhaynTime,
            LocalDateTime asrKarahaTime,
            LocalDateTime maghribTime,
            LocalDateTime ishtibaqAnNujumTime,
            LocalDateTime ishaTime
    )
    {
        this._fajrTime = fajrTime;

        this._sunRiseTime = sunRiseTime;
        this._duhaTime = duhaTime;

        this._dhuhrTime = dhuhrTime;
        this._asrTime = asrTime;
        this._asrMitlhaynTime = asrmitlhaynTime;
        this._asrKarahaTime = asrKarahaTime;

        this._maghribTime = maghribTime;
        this._ishtibaqAnNujumTime = ishtibaqAnNujumTime;
        this._ishaTime = ishaTime;
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

    public LocalDateTime getSunRiseTime()
    {
        return _sunRiseTime;
    }

    public void setSunRiseTime(LocalDateTime sunRiseTime)
    {
        _sunRiseTime = sunRiseTime;
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

    public LocalDateTime getAsrTime()
    {
        return _asrTime;
    }

    public void setAsrTime(LocalDateTime asrTime)
    {
        _asrTime = asrTime;
    }

    public LocalDateTime getAsrMitlhaynTime()
    {
        return _asrMitlhaynTime;
    }

    public void setAsrMitlhaynTime(LocalDateTime asrMitlhaynTime)
    {
        _asrMitlhaynTime = asrMitlhaynTime;
    }

    public LocalDateTime getAsrKarahaTime()
    {
        return _asrKarahaTime;
    }

    public void setAsrKarahaTime(LocalDateTime asrKarahaTime)
    {
        _asrKarahaTime = asrKarahaTime;
    }

    public LocalDateTime getMaghribTime()
    {
        return _maghribTime;
    }

    public void setMaghribTime(LocalDateTime maghribTime)
    {
        _maghribTime = maghribTime;
    }

    public LocalDateTime getIshtibaqAnNujumTime()
    {
        return _ishtibaqAnNujumTime;
    }

    public void setIshtibaqAnNujumTime(LocalDateTime ishtibaqAnNujumTime)
    {
        _ishtibaqAnNujumTime = ishtibaqAnNujumTime;
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

    public LocalDateTime GetTimeByType(EPrayerTimeType prayerType, EPrayerTimeMomentType prayerTypeTimeType)
    {
        if((prayerType == EPrayerTimeType.Isha && prayerTypeTimeType == EPrayerTimeMomentType.End)
                || (prayerType == EPrayerTimeType.Fajr && prayerTypeTimeType == EPrayerTimeMomentType.Beginning))
        {
            return this.getFajrTime();
        }
        else if(prayerType == EPrayerTimeType.Fajr && prayerTypeTimeType == EPrayerTimeMomentType.End)
        {
            return this.getSunRiseTime();
        }
        else if(prayerType == EPrayerTimeType.Duha && prayerTypeTimeType == EPrayerTimeMomentType.Beginning)
        {
            return this.getDuhaTime();
        }
        else if(prayerType == EPrayerTimeType.Duha && prayerTypeTimeType == EPrayerTimeMomentType.End)
        {
            LocalDateTime returnLocalDateTime = this.getDhuhrTime();
            returnLocalDateTime = returnLocalDateTime.minusHours(1);

            return returnLocalDateTime;
        }
        else if(prayerType == EPrayerTimeType.Dhuhr && prayerTypeTimeType == EPrayerTimeMomentType.Beginning)
        {
            return this.getDhuhrTime();
        }
        else if((prayerType == EPrayerTimeType.Dhuhr && prayerTypeTimeType == EPrayerTimeMomentType.End)
                || (prayerType == EPrayerTimeType.Asr && prayerTypeTimeType == EPrayerTimeMomentType.Beginning))
        {
            return this.getAsrTime();
        }
        else if((prayerType == EPrayerTimeType.Asr && prayerTypeTimeType == EPrayerTimeMomentType.End)
                || (prayerType == EPrayerTimeType.Maghrib && prayerTypeTimeType == EPrayerTimeMomentType.Beginning))
        {
            return this.getMaghribTime();
        }
        else if((prayerType == EPrayerTimeType.Maghrib && prayerTypeTimeType == EPrayerTimeMomentType.End)
                || (prayerType == EPrayerTimeType.Isha && prayerTypeTimeType == EPrayerTimeMomentType.Beginning))
        {
            return this.getIshaTime();
        }

        return null;
    }

    // endregion methods

    // region static methods

    // endregion static methods
}
