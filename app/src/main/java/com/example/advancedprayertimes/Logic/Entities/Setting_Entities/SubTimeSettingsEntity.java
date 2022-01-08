package com.example.advancedprayertimes.Logic.Entities.Setting_Entities;

public class SubTimeSettingsEntity
{
    // region static fields

    // endregion static fields

    // region fields

    private boolean _isEnabled1;
    private boolean _isEnabled2;
    private Double _asrKarahaDegree;

    // endregion fields

    // region constructors

    public SubTimeSettingsEntity(boolean isEnabled1, boolean isEnabled2, Double asrKarahaDegree)
    {
        this._isEnabled1 = isEnabled1;
        this._isEnabled2 = isEnabled2;
        this._asrKarahaDegree = asrKarahaDegree;
    }

    // endregion constructors

    // region getter & setter

    public boolean isEnabled1()
    {
        return _isEnabled1;
    }

    public void setEnabled1(boolean enabled1)
    {
        _isEnabled1 = enabled1;
    }

    public boolean isEnabled2()
    {
        return _isEnabled2;
    }

    public void setEnabled2(boolean enabled2)
    {
        _isEnabled2 = enabled2;
    }

    public Double getAsrKarahaDegree()
    {
        return _asrKarahaDegree;
    }

    public void setAsrKarahaDegree(Double asrKarahaDegree)
    {
        _asrKarahaDegree = asrKarahaDegree;
    }

    // endregion getter & setter

    // region overidden

    // endregion overidden

    // region methods

    // endregion methods

    // region static methods

    // endregion static methods
}
