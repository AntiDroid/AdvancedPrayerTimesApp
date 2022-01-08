package com.example.advancedprayertimes.Logic.Entities.Setting_Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeMomentType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrayerTimeBeginningEndSettingsEntity
{
    // region static fields

    public static HashSet DEGREE_TYPES =
            Stream.of(
                    new AbstractMap.SimpleEntry(EPrayerTimeType.Fajr, EPrayerTimeMomentType.Beginning),
                    new AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.End),
                    new AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.Beginning),
                    new AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.End)
            ).collect(Collectors.toCollection(HashSet::new));

    public static HashSet ISHA_DEGREE_TYPES =
            Stream.of(
                    new AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.End),
                    new AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.Beginning)
            ).collect(Collectors.toCollection(HashSet::new));

    public static HashSet FAJR_DEGREE_TYPES =
            Stream.of(
                    new AbstractMap.SimpleEntry(EPrayerTimeType.Fajr, EPrayerTimeMomentType.Beginning),
                    new AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.End)
            ).collect(Collectors.toCollection(HashSet::new));

    // endregion static fields

    // region fields

    private ESupportedAPIs _api = ESupportedAPIs.Undefined;
    private int _minuteAdjustment = 0;
    private Double _fajrCalculationDegree = null;
    private Double _ishaCalculationDegree = null;

    // endregion fields

    // region constructors

    public PrayerTimeBeginningEndSettingsEntity(ESupportedAPIs api, int minuteAdjustment, Double fajrCalculationDegree, Double ishaCalculationDegree)
    {
        this._api = api;
        this._minuteAdjustment = minuteAdjustment;
        this._fajrCalculationDegree = fajrCalculationDegree;
        this._ishaCalculationDegree = ishaCalculationDegree;
    }

    // endregion constructors

    // region getter & setter

    public Double getFajrCalculationDegree() { return _fajrCalculationDegree; }

    public void setFajrCalculationDegree(Double fajrCalculationDegree) { _fajrCalculationDegree = fajrCalculationDegree; }

    public Double getIshaCalculationDegree() { return _ishaCalculationDegree; }

    public void setIshaCalculationDegree(Double ishaCalculationDegree) { _ishaCalculationDegree = ishaCalculationDegree; }

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

    // endregion getter & setter

    // region overidden

    // endregion overidden

    // region methods

    // endregion methods

    // region static methods

    // endregion static methods
}
