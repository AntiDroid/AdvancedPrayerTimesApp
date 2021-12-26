package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.sql.Time;
import java.util.ArrayList;

public class PrayerEntity
{
    private String _title;

    private EPrayerTimeType _beginningTimeType;
    private EPrayerTimeType _endTimeType;

    private Time _beginningTime = new Time(0);
    private Time _endTime = new Time(0);

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
        PrayerEntity targetPrayer = prayers.stream().filter(x -> time.getTime() > x.getBeginningTime().getTime() && time.getTime() < x.getEndTime().getTime()).findFirst().get();

        return targetPrayer;
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

    public Time getBeginningTime()
    {
        return _beginningTime;
    }

    public void setBeginningTime(Time beginningTime)
    {
        _beginningTime = beginningTime;
    }

    public Time getEndTime()
    {
        return _endTime;
    }

    public void setEndTime(Time endTime)
    {
        _endTime = endTime;
    }
}
