package com.example.advancedprayertimes.Logic.Entities.Setting_Entities;

public class PrayerSettingsEntity
{
    // region static fields

    // endregion static fields

    // region fields

    private PrayerTimeBeginningEndSettingsEntity _beginningSettings;
    private PrayerTimeBeginningEndSettingsEntity _endSettings;

    private SubTimeSettingsEntity _subPrayer1Settings;
    private SubTimeSettingsEntity _subPrayer2Settings;
    private SubTimeSettingsEntity _subPrayer3Settings;

    // endregion fields

    // region constructors

    public PrayerSettingsEntity()
    {

    }

    public PrayerSettingsEntity(
            PrayerTimeBeginningEndSettingsEntity beginningSettings,
            PrayerTimeBeginningEndSettingsEntity endSettings,
            SubTimeSettingsEntity subPrayer1Settings,
            SubTimeSettingsEntity subPrayer2Settings,
            SubTimeSettingsEntity subPrayer3Settings
            )
    {
        this._beginningSettings = beginningSettings;
        this._endSettings = endSettings;
        this._subPrayer1Settings = subPrayer1Settings;
        this._subPrayer2Settings = subPrayer2Settings;
        this._subPrayer3Settings = subPrayer3Settings;
    }

    // endregion constructors

    // region getter & setter

    public PrayerTimeBeginningEndSettingsEntity getBeginningSettings()
    {
        return _beginningSettings;
    }

    public void setBeginningSettings(PrayerTimeBeginningEndSettingsEntity beginningSettings)
    {
        _beginningSettings = beginningSettings;
    }

    public PrayerTimeBeginningEndSettingsEntity getEndSettings()
    {
        return _endSettings;
    }

    public void setEndSettings(PrayerTimeBeginningEndSettingsEntity endSettings)
    {
        _endSettings = endSettings;
    }

    public SubTimeSettingsEntity getSubPrayer1Settings()
    {
        return _subPrayer1Settings;
    }

    public void setSubPrayer1Settings(SubTimeSettingsEntity subPrayer1Settings)
    {
        _subPrayer1Settings = subPrayer1Settings;
    }

    public SubTimeSettingsEntity getSubPrayer2Settings()
    {
        return _subPrayer2Settings;
    }

    public void setSubPrayer2Settings(SubTimeSettingsEntity subPrayer2Settings)
    {
        _subPrayer2Settings = subPrayer2Settings;
    }

    public SubTimeSettingsEntity getSubPrayer3Settings()
    {
        return _subPrayer3Settings;
    }

    public void setSubPrayer3Settings(SubTimeSettingsEntity subPrayer3Settings)
    {
        _subPrayer3Settings = subPrayer3Settings;
    }

    // endregion getter & setter

    // region overidden

    // endregion overidden

    // region methods

    public PrayerTimeBeginningEndSettingsEntity GetBeginningEndSettingByMomentType(boolean isBeginning)
    {
        if(isBeginning)
        {
            return this.getBeginningSettings();
        }
        else
        {
            return this.getEndSettings();
        }
    }

    public void SetBeginningEndSettingByMomentType(boolean isBeginning, PrayerTimeBeginningEndSettingsEntity beginningEndSetting)
    {
        if(isBeginning)
        {
            this.setBeginningSettings(beginningEndSetting);
        }
        else
        {
            this.setEndSettings(beginningEndSetting);
        }
    }

    // endregion methods

    // region static methods

    // endregion static methods
}
