package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeMomentType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.time.LocalDateTime;

public abstract class PrayerTimePackageAbstractClass
{
    public abstract String getDate();

    public abstract void setDate (String date);

    public abstract LocalDateTime getFajrTime();
    public abstract void setFajrTime(LocalDateTime fajrTime);

    public abstract LocalDateTime getSunriseTime();
    public abstract void setSunriseTime(LocalDateTime sunriseTime);

    public abstract LocalDateTime getDuhaTime();
    public abstract void setDuhaTime(LocalDateTime duhaTime);

    public abstract LocalDateTime getDhuhrTime();
    public abstract void setDhuhrTime(LocalDateTime dhuhrTime);

    public abstract LocalDateTime getAsrTime();
    public abstract void setAsrTime(LocalDateTime asrTime);

    public abstract LocalDateTime getMithlaynTime();
    public abstract void setMithlaynTime(LocalDateTime mithlaynTime);

    public abstract LocalDateTime getAsrKarahaTime();

    public abstract void setAsrKarahaTime(LocalDateTime asrKarahaTime);

    public abstract  LocalDateTime getMaghribTime();
    public abstract void setMaghribTime(LocalDateTime maghribTime);

    public abstract LocalDateTime getIshtibaqAnNujumTime();
    public abstract void setIshtibaqAnNujumTime(LocalDateTime ishtibaqAnNujum);

    public abstract LocalDateTime getIshaTime();
    public abstract void setIshaTime(LocalDateTime ishaTime);

    public LocalDateTime GetTimeByType(EPrayerTimeType prayerType, EPrayerTimeMomentType prayerTypeTimeType)
    {
        if((prayerType == EPrayerTimeType.Isha && prayerTypeTimeType == EPrayerTimeMomentType.End)
                || (prayerType == EPrayerTimeType.Fajr && prayerTypeTimeType == EPrayerTimeMomentType.Beginning))
        {
            return this.getFajrTime();
        }
        else if(prayerType == EPrayerTimeType.Fajr && prayerTypeTimeType == EPrayerTimeMomentType.End)
        {
            return this.getSunriseTime();
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
}
