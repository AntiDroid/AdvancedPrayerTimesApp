package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeMomentType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import kotlin.NotImplementedError;

public class PrayerTimeEntity
{
    // region static fields

    public static ArrayList<PrayerTimeEntity> Prayers = new ArrayList<PrayerTimeEntity>()
    {
        {
            add(new PrayerTimeEntity(EPrayerTimeType.Fajr, "Fajr"));
            add(new PrayerTimeEntity(EPrayerTimeType.Duha, "Duha"));
            add(new PrayerTimeEntity(EPrayerTimeType.Dhuhr, "Dhuhr"));
            add(new PrayerTimeEntity(EPrayerTimeType.Asr, "Asr"));
            add(new PrayerTimeEntity(EPrayerTimeType.Maghrib, "Maghrib"));
            add(new PrayerTimeEntity(EPrayerTimeType.Isha, "Isha"));
        }
    };

    // endregion static fields

    // region fields

    private EPrayerTimeType _prayerTimeType;

    private String _title;

    private LocalDateTime _beginningTime = null;
    private LocalDateTime _endTime = null;

    private LocalDateTime _subtime1BeginningTime = null;
    private LocalDateTime _subtime1EndTime = null;
    private LocalDateTime _subtime2BeginningTime = null;
    private LocalDateTime _subtime2EndTime = null;
    private LocalDateTime _subtime3BeginningTime = null;
    private LocalDateTime _subtime3EndTime = null;

    // endregion fields

    // region constructors

    private PrayerTimeEntity(EPrayerTimeType prayerTimeType, String prayerName)
    {
        this._prayerTimeType = prayerTimeType;
        this._title = prayerName;
    }

    // endregion constructors

    // region getter & setter

    public EPrayerTimeType getPrayerTimeType()
    {
        return _prayerTimeType;
    }

    public void setPrayerTimeType(EPrayerTimeType prayerTimeType)
    {
        _prayerTimeType = prayerTimeType;
    }

    public String getTitle()
    {
        return _title;
    }

    public void setTitle(String title)
    {
        _title = title;
    }

    public LocalDateTime getBeginningTime()
    {
        return _beginningTime;
    }

    public void setBeginningTime(LocalDateTime beginningTime)
    {
        _beginningTime = beginningTime;
    }

    public LocalDateTime getEndTime()
    {
        return _endTime;
    }

    public void setEndTime(LocalDateTime endTime)
    {
        _endTime = endTime;
    }

    public LocalDateTime getSubtime1BeginningTime()
    {
        return _subtime1BeginningTime;
    }

    public void setSubtime1BeginningTime(LocalDateTime subtime1BeginningTime)
    {
        _subtime1BeginningTime = subtime1BeginningTime;
    }

    public LocalDateTime getSubtime1EndTime()
    {
        return _subtime1EndTime;
    }

    public void setSubtime1EndTime(LocalDateTime subtime1EndTime)
    {
        _subtime1EndTime = subtime1EndTime;
    }

    public LocalDateTime getSubtime2BeginningTime()
    {
        return _subtime2BeginningTime;
    }

    public void setSubtime2BeginningTime(LocalDateTime subtime2BeginningTime)
    {
        _subtime2BeginningTime = subtime2BeginningTime;
    }

    public LocalDateTime getSubtime2EndTime()
    {
        return _subtime2EndTime;
    }

    public void setSubtime2EndTime(LocalDateTime subtime2EndTime)
    {
        _subtime2EndTime = subtime2EndTime;
    }

    public LocalDateTime getSubtime3BeginningTime()
    {
        return _subtime3BeginningTime;
    }

    public void setSubtime3BeginningTime(LocalDateTime subtime3BeginningTime)
    {
        _subtime3BeginningTime = subtime3BeginningTime;
    }

    public LocalDateTime getSubtime3EndTime()
    {
        return _subtime3EndTime;
    }

    public void setSubtime3EndTime(LocalDateTime subtime3EndTime)
    {
        _subtime3EndTime = subtime3EndTime;
    }

    // endregion getter & setter

    // region overidden

    // endregion overidden

    // region methods

    public long getDuration()
    {
        if(this.getEndTime() == null || this.getBeginningTime() == null)
        {
            return 0;
        }

        long duration = ChronoUnit.MILLIS.between(this.getBeginningTime(), this.getEndTime());

        // TODO: Fix Isha
        if(this.getEndTime().isBefore(this.getBeginningTime()))
        {
            long timeOfOneDay = new Date(0, 0, 1, 0, 0, 0).getTime() - new Date(0, 0, 0, 0, 0, 0).getTime();;

            duration += timeOfOneDay;
        }

        return duration;
    }

    public LocalDateTime GetTimeByMomentType(EPrayerTimeMomentType prayerTimeMomentType)
    {
        switch(prayerTimeMomentType)
        {
            case Beginning:
                return this.getBeginningTime();
            case End:
                return this.getEndTime();

            case SubTimeOne:
                return this.getSubtime1EndTime();
            case SubTimeTwo:
                return this.getSubtime2EndTime();
            case SubTimeThree:
                return this.getSubtime3EndTime();

            default:
                return null;
        }
    }

    public void SetTimeByMomentType(EPrayerTimeMomentType prayerTimeMomentType, LocalDateTime date)
    {
        switch(prayerTimeMomentType)
        {
            case Beginning:
                this.setBeginningTime(date);
                break;
            case End:
                this.setEndTime(date);
                break;

            case SubTimeOne:
                this.setSubtime1EndTime(date);
                break;
            case SubTimeTwo:
                this.setSubtime2EndTime(date);
                break;
            case SubTimeThree:
                this.setSubtime3EndTime(date);
                break;

            default:
                throw new NotImplementedError();
        }
    }

    // endregion methods

    // region static methods

    public static PrayerTimeEntity GetPrayerByTime(LocalDateTime time)
    {
        if(time == null)
        {
            return null;
        }

        Optional<PrayerTimeEntity> targetPrayer = Prayers.stream()
                .filter(x -> x.getBeginningTime() != null && x.getEndTime() != null &&
                        time.isAfter(x.getBeginningTime())
                        &&
                        time.isBefore(x.getEndTime()))
                .findFirst();

        if(targetPrayer.isPresent())
        {
            return targetPrayer.get();
        }
        // TODO: FIX ISHA
        else if(
            // before fajr beginning
                time.isAfter(Prayers.get(5).getBeginningTime())
                ||
                // before isha end
                time.isBefore(Prayers.get(5).getEndTime())
        )
        {
            return Prayers.get(5);
        }

        return null;
    }

    // endregion static methods
}