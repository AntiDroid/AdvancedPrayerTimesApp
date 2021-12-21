package com.example.advancedprayertimes.Logic;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.util.HashMap;
import java.util.HashSet;

public class AppEnvironment
{
    private static AppEnvironment _instance = null;

    public static AppEnvironment Instance()
    {
        if(_instance == null)
        {
            _instance = new AppEnvironment();
        }

        return _instance;
    }

    public HashMap<EPrayerTimeType, DayPrayerTimeSettingsEntity> DayPrayerTimeSettings = new HashMap<EPrayerTimeType, DayPrayerTimeSettingsEntity>();
}
