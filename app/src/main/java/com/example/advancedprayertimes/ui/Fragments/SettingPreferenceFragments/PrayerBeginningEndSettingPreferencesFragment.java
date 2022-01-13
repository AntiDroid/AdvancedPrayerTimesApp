package com.example.advancedprayertimes.UI.Fragments.SettingPreferenceFragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.DataManagementUtil;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerSettingsEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerTimeBeginningEndSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeMomentType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.R;
import com.google.gson.Gson;

import java.util.AbstractMap;
import java.util.stream.Stream;

public class PrayerBeginningEndSettingPreferencesFragment extends PreferenceFragmentCompat
{
    // region static fields

    private static String PREFERENCE_CATEGORY_API_SETTINGS = "apiSettingsPreferenceCategory";
    private static String PREFERENCE_NAME_API_SELECTION = "apiSelection";
    private static String PREFERENCE_NAME_MINUTE_ADJUSTMENT_SELECTION = "minuteAdjustmentSelection";
    private static String PREFERENCE_NAME_FAJR_DEGREE_SELECTION = "fajrCalculationDegree";
    private static String PREFERENCE_NAME_ISHA_DEGREE_SELECTION = "ishaCalculationDegree";

    // endregion static fields

    // region fields

    private SharedPreferences.OnSharedPreferenceChangeListener _preferenceChangeListener;

    private PreferenceCategory apiSettingsPreferenceCategory = null;
    private ListPreference apiSelectionListPreference = null;
    private ListPreference minuteAdjustmentListPreference = null;
    private ListPreference fajrDegreesListPreference = null;
    private ListPreference ishaDegreesListPreference = null;

    AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType> prayerTypeWithMomentType;
    private boolean _isBeginning;

    // endregion fields

    public PrayerBeginningEndSettingPreferencesFragment(EPrayerTimeType prayerType, boolean isBeginning)
    {
        super();

        _isBeginning = isBeginning;
        prayerTypeWithMomentType = new AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(prayerType, isBeginning ? EPrayerTimeMomentType.Beginning : EPrayerTimeMomentType.End);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.prayer_beginning_end_settings_preferences, rootKey);

        apiSettingsPreferenceCategory = this.findPreference(PREFERENCE_CATEGORY_API_SETTINGS);

        apiSelectionListPreference = this.findPreference(PREFERENCE_NAME_API_SELECTION);
        minuteAdjustmentListPreference = this.findPreference(PREFERENCE_NAME_MINUTE_ADJUSTMENT_SELECTION);
        fajrDegreesListPreference = this.findPreference(PREFERENCE_NAME_FAJR_DEGREE_SELECTION);
        ishaDegreesListPreference = this.findPreference(PREFERENCE_NAME_ISHA_DEGREE_SELECTION);

        if (apiSelectionListPreference == null || minuteAdjustmentListPreference == null || fajrDegreesListPreference == null
                || ishaDegreesListPreference == null || apiSettingsPreferenceCategory == null)
        {
            return;
        }

        configureAPISelector(apiSelectionListPreference);

        ESupportedAPIs selectedAPI = ESupportedAPIs.Undefined;
        int minuteAdjustmentValue = 0;
        double fajrDegreeValue = -12.0;
        double ishaDegreeValue = -12.0;

        PrayerTimeBeginningEndSettingsEntity settings = AppEnvironment.prayerSettingsByPrayerType.get(prayerTypeWithMomentType.getKey()).GetBeginningEndSettingByMomentType(_isBeginning);

        if(settings == null)
        {
            settings = new PrayerTimeBeginningEndSettingsEntity(selectedAPI, minuteAdjustmentValue, fajrDegreeValue, ishaDegreeValue);
            AppEnvironment.prayerSettingsByPrayerType.get(prayerTypeWithMomentType.getKey()).SetBeginningEndSettingByMomentType(_isBeginning, settings);
        }
        else
        {
            selectedAPI = settings.get_api();
            minuteAdjustmentValue = settings.get_minuteAdjustment();

            if(settings.getFajrCalculationDegree() != null)
            {
                fajrDegreeValue = settings.getFajrCalculationDegree();
            }

            if(settings.getIshaCalculationDegree() != null)
            {
                ishaDegreeValue = settings.getIshaCalculationDegree();
            }
        }

        apiSelectionListPreference.setValue(selectedAPI.toString());
        minuteAdjustmentListPreference.setValue(String.valueOf(minuteAdjustmentValue));
        fajrDegreesListPreference.setValue(fajrDegreeValue + "");
        ishaDegreesListPreference.setValue(ishaDegreeValue + "");

        updatePreferenceVisibility();

        // change listener for all preference value changes
        _preferenceChangeListener = this::onPreferenceChange;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    private void onPreferenceChange(SharedPreferences sharedPreferences, String key)
    {
        PrayerSettingsEntity prayerSettings = AppEnvironment.prayerSettingsByPrayerType.get(prayerTypeWithMomentType.getKey());

       if(prayerSettings == null)
        {
            return;
        }

       try
       {
           if(key.equals(apiSelectionListPreference.getKey()) && sharedPreferences.contains(apiSelectionListPreference.getKey()))
           {
               ESupportedAPIs api = ESupportedAPIs.valueOf(sharedPreferences.getString(apiSelectionListPreference.getKey(), null));
               prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning).set_api(api);

               updatePreferenceVisibility();
           }
           else if(key.equals(minuteAdjustmentListPreference.getKey()) && sharedPreferences.contains(minuteAdjustmentListPreference.getKey()))
           {
               int minuteAdjustment = sharedPreferences.getInt(minuteAdjustmentListPreference.getKey(), 0);
               prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning).set_minuteAdjustment(minuteAdjustment);
           }
           else if(key.equals(fajrDegreesListPreference.getKey()) && sharedPreferences.contains(fajrDegreesListPreference.getKey()))
           {
               Double fajrCalculationDegrees = (double) sharedPreferences.getFloat(fajrDegreesListPreference.getKey(), 0.0f);
               prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning).setFajrCalculationDegree(fajrCalculationDegrees);
           }
           else if(key.equals(ishaDegreesListPreference.getKey()) && sharedPreferences.contains(ishaDegreesListPreference.getKey()))
           {
               Double ishaCalculationDegrees = (double) sharedPreferences.getFloat(ishaDegreesListPreference.getKey(), 0.0f);
               prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning).setIshaCalculationDegree(ishaCalculationDegrees);
           }

           Gson gson = new Gson();

           String jsonString = gson.toJson(prayerSettings);
           this.getActivity().getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putString(DataManagementUtil.GetTimeSettingsEntityKeyForSharedPreference(prayerTypeWithMomentType.getKey()), jsonString).commit();
       }
       catch(Exception e)
       {
            e.printStackTrace();
       }
    }

    // region methods

    private void configureAPISelector(ListPreference apiSelectionListPreference)
    {
        String[] apiNamesArray = null;

        if(prayerTypeWithMomentType.getKey() == EPrayerTimeType.Duha)
        {
            apiNamesArray = Stream.of(ESupportedAPIs.values()).filter(x -> x == ESupportedAPIs.Undefined || x == ESupportedAPIs.Muwaqqit).map(ESupportedAPIs::name).toArray(String[]::new);
        }
        else
        {
            apiNamesArray = Stream.of(ESupportedAPIs.values()).map(ESupportedAPIs::name).toArray(String[]::new);
        }

        apiSelectionListPreference.setEntries(apiNamesArray);
        apiSelectionListPreference.setEntryValues(apiNamesArray);
    }

    private void updatePreferenceVisibility()
    {
        boolean isDegreeAPISelected =
                ESupportedAPIs.valueOf(apiSelectionListPreference.getValue()) == ESupportedAPIs.Muwaqqit
                ||
                ESupportedAPIs.valueOf(apiSelectionListPreference.getValue()) == ESupportedAPIs.AlAdhan;

        boolean showFajrDegreeSelector = isDegreeAPISelected && PrayerTimeBeginningEndSettingsEntity.FAJR_DEGREE_TYPES.contains(prayerTypeWithMomentType);
        boolean showIshaDegreeSelector = isDegreeAPISelected && PrayerTimeBeginningEndSettingsEntity.ISHA_DEGREE_TYPES.contains(prayerTypeWithMomentType);

        fajrDegreesListPreference.setVisible(showFajrDegreeSelector);
        ishaDegreesListPreference.setVisible(showIshaDegreeSelector);

        apiSettingsPreferenceCategory.setVisible(fajrDegreesListPreference.isVisible() || ishaDegreesListPreference.isVisible());
    }

    // endregion methods

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(_preferenceChangeListener);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(_preferenceChangeListener);
    }
}
