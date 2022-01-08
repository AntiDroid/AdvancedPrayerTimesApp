package com.example.advancedprayertimes.UI.Fragments.SettingPreferenceFragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
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

        // change listener for all preference value changes
        _preferenceChangeListener = this::onPreferenceChange;

        configureAPISelector(apiSelectionListPreference);
        configureMinuteAdjustmentSelector(minuteAdjustmentListPreference);
        configureDegreeSelectors(fajrDegreesListPreference, ishaDegreesListPreference);

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
    }

    private void onPreferenceChange(SharedPreferences sharedPreferences, String key)
    {
        PrayerSettingsEntity prayerSettings = AppEnvironment.prayerSettingsByPrayerType.get(prayerTypeWithMomentType.getKey());

       if(prayerSettings == null)
        {
            return;
        }

        if(key.equals(apiSelectionListPreference.getKey()))
        {
            ESupportedAPIs api = ESupportedAPIs.valueOf(apiSelectionListPreference.getValue());
            prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning).set_api(api);

            updatePreferenceVisibility();
        }
        else if(key.equals(minuteAdjustmentListPreference.getKey()))
        {
            int minuteAdjustment = Integer.parseInt(minuteAdjustmentListPreference.getValue());
            prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning).set_minuteAdjustment(minuteAdjustment);
        }
        else if(key.equals(fajrDegreesListPreference.getKey()))
        {
            Double fajrCalculationDegrees = Double.parseDouble(fajrDegreesListPreference.getValue());
            prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning).setFajrCalculationDegree(fajrCalculationDegrees);
        }
        else if(key.equals(ishaDegreesListPreference.getKey()))
        {
            Double ishaCalculationDegrees = Double.parseDouble(ishaDegreesListPreference.getValue());
            prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning).setIshaCalculationDegree(ishaCalculationDegrees);
        }

        Gson gson = new Gson();

        String jsonString = gson.toJson(prayerSettings);
        this.getActivity().getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putString(DataManagementUtil.GetTimeSettingsEntityKeyForSharedPreference(prayerTypeWithMomentType.getKey()), jsonString).commit();
    }

    // region methods

    private void configureAPISelector(ListPreference apiSelectionListPreference)
    {
        String[] apiNamesArray = Stream.of(ESupportedAPIs.values()).map(ESupportedAPIs::name).toArray(String[]::new);

        apiSelectionListPreference.setEntries(apiNamesArray);
        apiSelectionListPreference.setEntryValues(apiNamesArray);
        apiSelectionListPreference.setValue(ESupportedAPIs.Undefined.toString());
    }

    private void configureMinuteAdjustmentSelector(ListPreference minuteAdjustmentListPreference)
    {
        ArrayList<String> minuteAdjustmentValuesArrayList = new ArrayList<>();

        for (int i = -15; i < 16; i++)
        {
            minuteAdjustmentValuesArrayList.add("" + i);
        }

        minuteAdjustmentListPreference.setEntries(minuteAdjustmentValuesArrayList.toArray(new String[0]));
        minuteAdjustmentListPreference.setEntryValues(minuteAdjustmentValuesArrayList.toArray(new String[0]));
        minuteAdjustmentListPreference.setValue("0");
    }

    private void configureDegreeSelectors(ListPreference fajrDegreesListPreference, ListPreference ishaDegreesListPreference)
    {
        ArrayList<String> degreeValuesArrayList = new ArrayList<>();

        for (double i = -12.0; i > -21.0; i -= 0.5)
        {
            degreeValuesArrayList.add("" + i);
        }

        String[] degreeValuesArray = degreeValuesArrayList.toArray(new String[0]);

        fajrDegreesListPreference.setEntries(Arrays.stream(degreeValuesArray).map(x -> x + "°").collect(Collectors.toList()).toArray(new String[0]));
        fajrDegreesListPreference.setEntryValues(degreeValuesArray);
        fajrDegreesListPreference.setValue("-12.0");

        ishaDegreesListPreference.setEntries(Arrays.stream(degreeValuesArray).map(x -> x + "°").collect(Collectors.toList()).toArray(new String[0]));
        ishaDegreesListPreference.setEntryValues(degreeValuesArray);
        ishaDegreesListPreference.setValue("-12.0");
    }

    private void updatePreferenceVisibility()
    {
        boolean isMuwaqqitAPISelected = ESupportedAPIs.valueOf(apiSelectionListPreference.getValue()) == ESupportedAPIs.Muwaqqit;

        boolean showFajrDegreeSelector = PrayerTimeBeginningEndSettingsEntity.FAJR_DEGREE_TYPES.contains(prayerTypeWithMomentType) && isMuwaqqitAPISelected;
        boolean showIshaDegreeSelector = PrayerTimeBeginningEndSettingsEntity.ISHA_DEGREE_TYPES.contains(prayerTypeWithMomentType) && isMuwaqqitAPISelected;

        fajrDegreesListPreference.setVisible(showFajrDegreeSelector);
        ishaDegreesListPreference.setVisible(showIshaDegreeSelector);

        apiSettingsPreferenceCategory.setVisible(fajrDegreesListPreference.isVisible() || ishaDegreesListPreference.isVisible());
    }

    // endregion methods

    @Override
    public void onStop()
    {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(_preferenceChangeListener);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(_preferenceChangeListener);
    }
}
