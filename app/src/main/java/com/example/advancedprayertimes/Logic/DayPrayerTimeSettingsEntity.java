package com.example.advancedprayertimes.Logic;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;

public class DayPrayerTimeSettingsEntity
{
    private ESupportedAPIs _api;
    private int _minuteAdjustment;

    public DayPrayerTimeSettingsEntity(EPrayerTimeType prayerTimeType, ESupportedAPIs api, int minuteAdjustment)
    {
        this._prayerTimeType = prayerTimeType;
        this._api = api;
        this._minuteAdjustment = minuteAdjustment;
    }

    public ESupportedAPIs get_api()
    {
        return _api;
    }

    public void set_api(ESupportedAPIs _api)
    {
        this._api = _api;
    }

    public int get_minuteAdjustment()
    {
        return _minuteAdjustment;
    }

    public void set_minuteAdjustment(int _minuteAdjustment)
    {
        this._minuteAdjustment = _minuteAdjustment;
    }
}
