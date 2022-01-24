package com.example.advancedprayertimes.logic

import com.example.advancedprayertimes.logic.setting_entities.PrayerTimeBeginningEndSettingsEntity
import com.example.advancedprayertimes.logic.setting_entities.SubTimeSettingsEntity

class PrayerSettingsEntity(
    var beginningSettings: PrayerTimeBeginningEndSettingsEntity?,
    var endSettings: PrayerTimeBeginningEndSettingsEntity?,
    var subPrayer1Settings: SubTimeSettingsEntity?,
    var subPrayer2Settings: SubTimeSettingsEntity?,
    var subPrayer3Settings: SubTimeSettingsEntity?
)
{
    constructor() : this (null, null, null, null, null)
    {

    }

    fun GetBeginningEndSettingByMomentType(isBeginning: Boolean): PrayerTimeBeginningEndSettingsEntity?
    {
        return if (isBeginning)
        {
            beginningSettings
        }
        else
        {
            endSettings
        }
    }

    fun SetBeginningEndSettingByMomentType(
        isBeginning: Boolean,
        beginningEndSetting: PrayerTimeBeginningEndSettingsEntity?
    )
    {
        if (isBeginning)
        {
            beginningSettings = beginningEndSetting
        }
        else
        {
            endSettings = beginningEndSetting
        }
    }
}