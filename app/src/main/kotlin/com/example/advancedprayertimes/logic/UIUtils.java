package com.example.advancedprayertimes.logic;

public class UIUtils
{
//    public static void OnBeginningEndSettingsTabStop(boolean isBeginning, PrayerBeginningEndSettingPreferencesFragment prayerBeginningEndSettingPreferencesFragment, EPrayerTimeType prayerType)
//    {
//        ListPreference apiSelectionListPreference = prayerBeginningEndSettingPreferencesFragment.findPreference("apiSelection");
//        ListPreference minuteAdjustmentListPreference = prayerBeginningEndSettingPreferencesFragment.findPreference("minuteAdjustmentSelection");
//        ListPreference fajrDegreesListPreference = prayerBeginningEndSettingPreferencesFragment.findPreference("fajrCalculationDegree");
//        ListPreference ishaDegreesListPreference = prayerBeginningEndSettingPreferencesFragment.findPreference("ishaCalculationDegree");
//
//        if (apiSelectionListPreference == null
//                ||
//                minuteAdjustmentListPreference == null
//                ||
//                fajrDegreesListPreference == null
//                ||
//                ishaDegreesListPreference == null)
//        {
//            return;
//        }
//
//        ESupportedAPIs api = ESupportedAPIs.valueOf(apiSelectionListPreference.getValue());
//        int minuteAdjustment = Integer.parseInt(minuteAdjustmentListPreference.getValue());
//
//        Double fajrCalculationDegrees = null;
//        Double ishaCalculationDegrees = null;
//
//        if(fajrDegreesListPreference.isVisible())
//        {
//            fajrCalculationDegrees = Double.parseDouble(fajrDegreesListPreference.getValue());
//        }
//
//        if(ishaDegreesListPreference.isVisible())
//        {
//            ishaCalculationDegrees = Double.parseDouble(ishaDegreesListPreference.getValue());
//        }
//
//        if(isBeginning)
//        {
//            PrayerTimeBeginningEndSettingsEntity beginningSettings = new PrayerTimeBeginningEndSettingsEntity(api, minuteAdjustment, fajrCalculationDegrees, ishaCalculationDegrees);
//            AppEnvironment.prayerSettingsByPrayerType.get(prayerType).setBeginningSettings(beginningSettings);
//        }
//        else
//        {
//            PrayerTimeBeginningEndSettingsEntity endSettings = new PrayerTimeBeginningEndSettingsEntity(api, minuteAdjustment, fajrCalculationDegrees, ishaCalculationDegrees);
//            AppEnvironment.prayerSettingsByPrayerType.get(prayerType).setEndSettings(endSettings);
//        }
//    }

}
