package com.example.advancedprayertimes.Logic;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;

import java.util.HashMap;
import java.util.HashSet;

public class AppEnvironment
{
    public static HashMap<EPrayerTimeType, DayPrayerTimeSettingsEntity> DayPrayerTimeSettings = new HashMap<EPrayerTimeType, DayPrayerTimeSettingsEntity>();
}
