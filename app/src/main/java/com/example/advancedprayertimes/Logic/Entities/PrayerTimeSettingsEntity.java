package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerPointInTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrayerTimeSettingsEntity
{
    private ESupportedAPIs _api = ESupportedAPIs.Undefined;
    private int _minuteAdjustment = 0;
    private Double _fajrCalculationDegree = null;
    private Double _ishaCalculationDegree = null;

    public PrayerTimeSettingsEntity(ESupportedAPIs api, int minuteAdjustment, Double fajrCalculationDegree, Double ishaCalculationDegree)
    {
        this._api = api;
        this._minuteAdjustment = minuteAdjustment;
        this._fajrCalculationDegree = fajrCalculationDegree;
        this._ishaCalculationDegree = ishaCalculationDegree;
    }

    public static HashSet<EPrayerPointInTimeType> DEGREE_TYPES =
            Stream.of(EPrayerPointInTimeType.FajrBeginning, EPrayerPointInTimeType.MaghribEnd, EPrayerPointInTimeType.IshaBeginning, EPrayerPointInTimeType.IshaEnd)
                    .collect(Collectors.toCollection(HashSet::new));

    public static HashSet<EPrayerPointInTimeType> ISHA_DEGREE_TYPES =
            Stream.of(EPrayerPointInTimeType.MaghribEnd, EPrayerPointInTimeType.IshaBeginning)
                    .collect(Collectors.toCollection(HashSet::new));

    public static HashSet<EPrayerPointInTimeType> FAJR_DEGREE_TYPES =
            Stream.of(EPrayerPointInTimeType.FajrBeginning, EPrayerPointInTimeType.IshaEnd)
                    .collect(Collectors.toCollection(HashSet::new));

    // ############################

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

    public void set_minuteAdjustment(int _minuteAdjustment) { this._minuteAdjustment = _minuteAdjustment; }
}
