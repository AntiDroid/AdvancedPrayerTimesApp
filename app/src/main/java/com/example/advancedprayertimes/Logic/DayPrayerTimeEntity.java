package com.example.advancedprayertimes.Logic;

import java.util.Date;

public class DayPrayerTimeEntity
{
    private Date _fajrTimeBeginning;
    private Date _fajrTimeEnd;

    private Date _dhuhrTimeBeginning;
    private Date _dhuhrTimeEnd;

    private Date _asrTimeBeginning;
    private Date _asrTimeEnd;

    private Date _maghribTimeBeginning;
    private Date _maghribTimeEnd;

    private Date _ishaTimeBeginning;
    private Date _ishaTimeEnd;

    public DayPrayerTimeEntity(
            Date fajrTimeBeginning,
            Date fajrTimeEnd,
            Date dhuhrTimeBeginning,
            Date dhuhrTimeEnd,
            Date asrTimeBeginning,
            Date asrTimeEnd,
            Date maghribTimeBeginning,
            Date maghribTimeEnd,
            Date ishaTimeBeginning,
            Date ishaTimeEnd
    )
    {
        this._fajrTimeBeginning = fajrTimeBeginning;
        this._fajrTimeEnd = fajrTimeEnd;

        this._dhuhrTimeBeginning = dhuhrTimeBeginning;
        this._dhuhrTimeEnd = dhuhrTimeEnd;

        this._asrTimeBeginning = asrTimeBeginning;
        this._asrTimeEnd = asrTimeEnd;

        this._maghribTimeBeginning = maghribTimeBeginning;
        this._maghribTimeEnd = maghribTimeEnd;

        this._ishaTimeBeginning = ishaTimeBeginning;
        this._ishaTimeEnd = ishaTimeEnd;
    }

    public Date get_fajrTimeBeginning()
    {
        return _fajrTimeBeginning;
    }

    public void set_fajrTimeBeginning(Date _fajrTimeBeginning)
    {
        this._fajrTimeBeginning = _fajrTimeBeginning;
    }

    public Date get_fajrTimeEnd()
    {
        return _fajrTimeEnd;
    }

    public void set_fajrTimeEnd(Date _fajrTimeEnd)
    {
        this._fajrTimeEnd = _fajrTimeEnd;
    }

    public Date get_dhuhrTimeBeginning()
    {
        return _dhuhrTimeBeginning;
    }

    public void set_dhuhrTimeBeginning(Date _dhuhrTimeBeginning)
    {
        this._dhuhrTimeBeginning = _dhuhrTimeBeginning;
    }

    public Date get_dhuhrTimeEnd()
    {
        return _dhuhrTimeEnd;
    }

    public void set_dhuhrTimeEnd(Date _dhuhrTimeEnd)
    {
        this._dhuhrTimeEnd = _dhuhrTimeEnd;
    }

    public Date get_asrTimeBeginning()
    {
        return _asrTimeBeginning;
    }

    public void set_asrTimeBeginning(Date _asrTimeBeginning)
    {
        this._asrTimeBeginning = _asrTimeBeginning;
    }

    public Date get_asrTimeEnd()
    {
        return _asrTimeEnd;
    }

    public void set_asrTimeEnd(Date _asrTimeEnd)
    {
        this._asrTimeEnd = _asrTimeEnd;
    }

    public Date get_maghribTimeBeginning()
    {
        return _maghribTimeBeginning;
    }

    public void set_maghribTimeBeginning(Date _maghribTimeBeginning)
    {
        this._maghribTimeBeginning = _maghribTimeBeginning;
    }

    public Date get_maghribTimeEnd()
    {
        return _maghribTimeEnd;
    }

    public void set_maghribTimeEnd(Date _maghribTimeEnd)
    {
        this._maghribTimeEnd = _maghribTimeEnd;
    }

    public Date get_ishaTimeBeginning()
    {
        return _ishaTimeBeginning;
    }

    public void set_ishaTimeBeginning(Date _ishaTimeBeginning)
    {
        this._ishaTimeBeginning = _ishaTimeBeginning;
    }

    public Date get_ishaTimeEnd()
    {
        return _ishaTimeEnd;
    }

    public void set_ishaTimeEnd(Date _ishaTimeEnd)
    {
        this._ishaTimeEnd = _ishaTimeEnd;
    }
}
