package com.example.advancedprayertimes.Logic.Entities;

public class SubPrayerEntity
{
    private PrayerEntity _parentPrayerEntity;
    private String _title;

    private SubPrayerEntity(){ }

    private SubPrayerEntity(PrayerEntity parentPrayerEntity, String title)
    {
        this._parentPrayerEntity = parentPrayerEntity;
        this._title = title;
    }
}
