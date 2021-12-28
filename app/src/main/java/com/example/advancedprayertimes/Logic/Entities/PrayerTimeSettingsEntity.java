package com.example.advancedprayertimes.Logic.Entities;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.google.android.libraries.places.api.model.Place;

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

    public static HashSet<EPrayerTimeType> DEGREE_TYPES =
            Stream.of(EPrayerTimeType.FajrBeginning, EPrayerTimeType.MaghribEnd, EPrayerTimeType.IshaBeginning, EPrayerTimeType.IshaEnd)
                    .collect(Collectors.toCollection(HashSet::new));

    public static HashSet<EPrayerTimeType> ISHA_DEGREE_TYPES =
            Stream.of(EPrayerTimeType.MaghribEnd, EPrayerTimeType.IshaBeginning)
                    .collect(Collectors.toCollection(HashSet::new));

    public static HashSet<EPrayerTimeType> FAJR_DEGREE_TYPES =
            Stream.of(EPrayerTimeType.FajrBeginning, EPrayerTimeType.IshaEnd)
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
