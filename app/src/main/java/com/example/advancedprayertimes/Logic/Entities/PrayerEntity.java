package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerPointInTimeType;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class PrayerEntity
{
    private String _title;

    private EPrayerPointInTimeType _beginningTimeType;
    private EPrayerPointInTimeType _endTimeType;

    private Date _beginningTime = new Date(0);
    private Date _endTime = new Date(0);

    private PrayerEntity(){ }

    private PrayerEntity(String prayerName, EPrayerPointInTimeType beginningType, EPrayerPointInTimeType endType)
    {
        this._title = prayerName;
        this._beginningTimeType = beginningType;
        this._endTimeType = endType;
    }

    public static ArrayList<PrayerEntity> prayers = new ArrayList<PrayerEntity>()
    {
        {
            add(new PrayerEntity("Fajr", EPrayerPointInTimeType.FajrBeginning, EPrayerPointInTimeType.FajrEnd));
            add(new PrayerEntity("Dhuhr", EPrayerPointInTimeType.DhuhrBeginning, EPrayerPointInTimeType.DhuhrEnd));
            add(new PrayerEntity("Asr", EPrayerPointInTimeType.AsrBeginning, EPrayerPointInTimeType.AsrEnd));
            add(new PrayerEntity("Maghrib", EPrayerPointInTimeType.MaghribBeginning, EPrayerPointInTimeType.MaghribEnd));
            add(new PrayerEntity("Isha", EPrayerPointInTimeType.IshaBeginning, EPrayerPointInTimeType.IshaEnd));
        }
    };

    public static PrayerEntity GetPrayerByTime(Time time)
    {
        Optional<PrayerEntity> targetPrayer = prayers.stream()
                .filter(x -> time.getTime() > x.getBeginningTime().getTime()
                                &&
                        time.getTime() < x.getEndTime().getTime() )
                .findFirst();

        if(targetPrayer.isPresent())
        {
            return targetPrayer.get();
        }
        // TODO: FIX ISHA
        else if(
                // before fajr beginning
                time.getTime() < prayers.get(0).getBeginningTime().getTime()
                &&
                // before isha end
                time.getTime() < prayers.get(4).getEndTime().getTime()
        )
        {
            return prayers.get(4);
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

    public EPrayerPointInTimeType getBeginningTimeType()
    {
        return _beginningTimeType;
    }

    public void setBeginningTimeType(EPrayerPointInTimeType beginningTimeType)
    {
        _beginningTimeType = beginningTimeType;
    }

    public EPrayerPointInTimeType getEndTimeType()
    {
        return _endTimeType;
    }

    public void setEndTimeType(EPrayerPointInTimeType endTimeType)
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
