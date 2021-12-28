package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class PrayerEntity
{
    private String _title;

    private EPrayerTimeType _beginningTimeType;
    private EPrayerTimeType _endTimeType;

    private Date _beginningTime = new Date(0);
    private Date _endTime = new Date(0);

    private PrayerEntity(){ }

    private PrayerEntity(String prayerName, EPrayerTimeType beginningType, EPrayerTimeType endType)
    {
        this._title = prayerName;
        this._beginningTimeType = beginningType;
        this._endTimeType = endType;
    }

    public static ArrayList<PrayerEntity> prayers = new ArrayList<PrayerEntity>()
    {
        {
            add(new PrayerEntity("Fajr", EPrayerTimeType.FajrBeginning, EPrayerTimeType.FajrEnd));
            add(new PrayerEntity("Dhuhr", EPrayerTimeType.DhuhrBeginning, EPrayerTimeType.DhuhrEnd));
            add(new PrayerEntity("Asr", EPrayerTimeType.AsrBeginning, EPrayerTimeType.AsrEnd));
            add(new PrayerEntity("Maghrib", EPrayerTimeType.MaghribBeginning, EPrayerTimeType.MaghribEnd));
            add(new PrayerEntity("Isha", EPrayerTimeType.IshaBeginning, EPrayerTimeType.IshaEnd));
        }
    };

    public static PrayerEntity GetPrayerByTime(Time time)
    {
        Optional<PrayerEntity> targetPrayer = prayers.stream()
                .filter(x -> time.getTime() > x.getBeginningTime().getTime()
                                &&
                        // TODO: FIX ISHA
                        (time.getTime() < x.getEndTime().getTime() || x.getBeginningTime().getTime() > x.getEndTime().getTime()))
                .findFirst();

        if(targetPrayer.isPresent())
        {
            return targetPrayer.get();
        }

        return null;
    }

    // ###############################

    public String getTitle()
    {
        return _title;
    }

    public void setTitle(String title)
    {
        _title = title;
    }

    public EPrayerTimeType getBeginningTimeType()
    {
        return _beginningTimeType;
    }

    public void setBeginningTimeType(EPrayerTimeType beginningTimeType)
    {
        _beginningTimeType = beginningTimeType;
    }

    public EPrayerTimeType getEndTimeType()
    {
        return _endTimeType;
    }

    public void setEndTimeType(EPrayerTimeType endTimeType)
    {
        _endTimeType = endTimeType;
    }

    public Date getBeginningTime()
    {
        return _beginningTime;
    }

    public void setBeginningTime(Date beginningTime)
    {
        _beginningTime = beginningTime;
    }

    public Date getEndTime()
    {
        return _endTime;
    }

    public void setEndTime(Date endTime)
    {
        _endTime = endTime;
    }
}
