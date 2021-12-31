package com.example.advancedprayertimes.UI.Fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerPointInTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SettingsFragment extends PreferenceFragmentCompat
{
    private EPrayerPointInTimeType _prayerPointInTimeType;

    public SettingsFragment(EPrayerPointInTimeType prayerPointInTimeType)
    {
        super();
        this._prayerPointInTimeType = prayerPointInTimeType;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.prayer_settings_preferences, rootKey);

        ListPreference apiSelectionListPreference = this.findPreference("apiSelection");
        ListPreference minuteAdjustmentListPreference = this.findPreference("minuteAdjustmentSelection");
        ListPreference fajrDegreesListPreference = this.findPreference("fajrCalculationDegree");
        ListPreference ishaDegreesListPreference = this.findPreference("ishaCalculationDegree");

        PreferenceCategory apiSettingsPreferenceCategory = this.findPreference("apiSettingsPreferenceCategory");

        if (apiSelectionListPreference != null
                &&
                minuteAdjustmentListPreference != null
                &&
                fajrDegreesListPreference != null
                &&
                ishaDegreesListPreference != null
                &&
                apiSettingsPreferenceCategory != null)
        {
            configureAPISelector(apiSelectionListPreference);
            configureMinuteAdjustmentSelector(minuteAdjustmentListPreference);
            configureDegreeSelectors(fajrDegreesListPreference, ishaDegreesListPreference);

            // LOAD CURRENT SETTINGS, IF THERE ARE ANY

            ESupportedAPIs selectedAPI = ESupportedAPIs.Undefined;
            int minuteAdjustmentValue = 0;

            boolean showFajrDegreeSelector = PrayerTimeSettingsEntity.FAJR_DEGREE_TYPES.contains(_prayerPointInTimeType);
            double fajrDegreeValue = 12.0;

            boolean showIshaDegreeSelector = PrayerTimeSettingsEntity.ISHA_DEGREE_TYPES.contains(_prayerPointInTimeType);
            double ishaDegreeValue = 12.0;

            if (AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.get(_prayerPointInTimeType) != null)
            {
                PrayerTimeSettingsEntity settings = AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.get(_prayerPointInTimeType);

                selectedAPI = settings.get_api();
                minuteAdjustmentValue = settings.get_minuteAdjustment();

                showFajrDegreeSelector = showFajrDegreeSelector && settings.get_api() == ESupportedAPIs.Muwaqqit;
                if(settings.getFajrCalculationDegree() != null)
                {
                    fajrDegreeValue = settings.getFajrCalculationDegree();
                }

                showIshaDegreeSelector = showIshaDegreeSelector && settings.get_api() == ESupportedAPIs.Muwaqqit;
                if(settings.getIshaCalculationDegree() != null)
                {
                    ishaDegreeValue = settings.getIshaCalculationDegree();
                }
            }

            apiSelectionListPreference.setValue(selectedAPI.toString());
            minuteAdjustmentListPreference.setValue(String.valueOf(minuteAdjustmentValue));

            fajrDegreesListPreference.setVisible(showFajrDegreeSelector);
            fajrDegreesListPreference.setValue(fajrDegreeValue + "");

            ishaDegreesListPreference.setVisible(showIshaDegreeSelector);
            ishaDegreesListPreference.setValue(ishaDegreeValue + "");

            apiSettingsPreferenceCategory.setVisible(fajrDegreesListPreference.isVisible() || ishaDegreesListPreference.isVisible());
        }
    }

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

        fajrDegreesListPreference.setEntries(degreeValuesArray);
        fajrDegreesListPreference.setEntryValues(degreeValuesArray);
        fajrDegreesListPreference.setValue("-12.0");

        ishaDegreesListPreference.setEntries(degreeValuesArray);
        ishaDegreesListPreference.setEntryValues(degreeValuesArray);
        ishaDegreesListPreference.setValue("-12.0");
    }
}
