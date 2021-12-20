package com.example.advancedprayertimes;

import java.util.Date;

public class DayPrayerTimeEntity
{
    private Date _fajrTime;
    private Date _dhuhrTime;
    private Date _asrTime;
    private Date _maghribTime;
    private Date _ishaTime;

    public DayPrayerTimeEntity(
            Date fajrTime,
            Date dhuhrTime,
            Date asrTime,
            Date maghribTime,
            Date ishaTime
    )
    {
        this._fajrTime = fajrTime;
        this._dhuhrTime = dhuhrTime;
        this._asrTime = asrTime;
        this._maghribTime = maghribTime;
        this._ishaTime = ishaTime;
    }

    public Date get_fajrTime() {
        return _fajrTime;
    }

    public void set_fajrTime(Date _fajrTime) {
        this._fajrTime = _fajrTime;
    }

    public Date get_dhuhrTime() {
        return _dhuhrTime;
    }

    public void set_dhuhrTime(Date _dhuhrTime) {
        this._dhuhrTime = _dhuhrTime;
    }

    public Date get_asrTime() {
        return _asrTime;
    }

    public void set_asrTime(Date _asrTime) {
        this._asrTime = _asrTime;
    }

    public Date get_maghribTime() {
        return _maghribTime;
    }

    public void set_maghribTime(Date _maghribTime) {
        this._maghribTime = _maghribTime;
    }

    public Date get_ishaTime() {
        return _ishaTime;
    }

    public void set_ishaTime(Date _ishaTime) {
        this._ishaTime = _ishaTime;
    }
}
