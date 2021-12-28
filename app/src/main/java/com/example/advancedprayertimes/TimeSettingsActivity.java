package com.example.advancedprayertimes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.databinding.TimeSettingsActivityBinding;

import java.util.ArrayList;
import java.util.stream.Stream;

public class TimeSettingsActivity extends AppCompatActivity
{
    EPrayerTimeType prayerTimeType;
    SettingsFragment settingsFragment = new SettingsFragment();

    TimeSettingsActivityBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_settings_activity);

        binding = TimeSettingsActivityBinding.inflate(getLayoutInflater());

        // Get the Intent that started this activity and extract the string
        Intent intent = this.getIntent();
        prayerTimeType = (EPrayerTimeType) intent.getSerializableExtra(TimeOverviewActivity.INTENT_EXTRA);

        if (savedInstanceState == null)
        {
            this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, settingsFragment)
                    .commit();
        }

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(prayerTimeType.toString());
        }
    }

    @Override
    protected void onStop()
    {
        //TODO: Consider that setting values may be "Not set"

        ListPreference apiSelectionListPreference = this.settingsFragment.findPreference("apiSelection");
        ListPreference minuteAdjustmentListPreference = this.settingsFragment.findPreference("minuteAdjustmentSelection");
        ListPreference fajrDegreesListPreference = this.settingsFragment.findPreference("fajrCalculationDegree");
        ListPreference ishaDegreesListPreference = this.settingsFragment.findPreference("ishaCalculationDegree");

        ESupportedAPIs api = ESupportedAPIs.Undefined;
        int minuteAdjustment = 0;

        if(apiSelectionListPreference != null)
        {
            api = ESupportedAPIs.valueOf(apiSelectionListPreference.getValue());
        }

        if(minuteAdjustmentListPreference != null)
        {
            minuteAdjustment = Integer.parseInt(minuteAdjustmentListPreference.getValue());
        }

        Double fajrCalculationDegrees = null;
        Double ishaCalculationDegrees = null;

        if(prayerTimeType == EPrayerTimeType.FajrBeginning || prayerTimeType == EPrayerTimeType.IshaEnd)
        {
            fajrCalculationDegrees = Double.parseDouble(fajrDegreesListPreference.getValue());
        }

        if(prayerTimeType == EPrayerTimeType.IshaBeginning || prayerTimeType == EPrayerTimeType.MaghribEnd)
        {
            ishaCalculationDegrees = Double.parseDouble(ishaDegreesListPreference.getValue());
        }

        AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.put(prayerTimeType, new PrayerTimeSettingsEntity(api, minuteAdjustment, fajrCalculationDegrees, ishaCalculationDegrees));
        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EPrayerTimeType prayerTimeType = ((TimeSettingsActivity) this.requireActivity()).prayerTimeType;

            ListPreference apiSelectionListPreference = this.findPreference("apiSelection");
            ListPreference minuteAdjustmentListPreference = this.findPreference("minuteAdjustmentSelection");
            ListPreference fajrDegreesListPreference = this.findPreference("fajrCalculationDegree");
            ListPreference ishaDegreesListPreference = this.findPreference("ishaCalculationDegree");

            PreferenceCategory apiSettingsPreferenceCategory = this.findPreference("apiSettingsPreferenceCategory");

            if(     apiSelectionListPreference != null
                    &&
                    minuteAdjustmentListPreference != null
                    &&
                    fajrDegreesListPreference != null
                    &&
                    ishaDegreesListPreference != null
                    &&
                    apiSettingsPreferenceCategory != null)
            {
                // SET API SELECTION VALUES

                String[] apiNamesArray = Stream.of(ESupportedAPIs.values()).map(ESupportedAPIs::name).toArray(String[]::new);

                apiSelectionListPreference.setEntries(apiNamesArray);
                apiSelectionListPreference.setEntryValues(apiNamesArray);
                apiSelectionListPreference.setValue(ESupportedAPIs.Undefined.toString());

                // SET MINUTE ADJUSTMENT VALUES

                ArrayList<String> minuteAdjustmentValuesArrayList = new ArrayList<>();

                for(int i = -15; i < 16; i++)
                {
                    minuteAdjustmentValuesArrayList.add("" + i);
                }

                minuteAdjustmentListPreference.setEntries(minuteAdjustmentValuesArrayList.toArray(new String[0]));
                minuteAdjustmentListPreference.setEntryValues(minuteAdjustmentValuesArrayList.toArray(new String[0]));
                minuteAdjustmentListPreference.setValue("0");

                // SET DEGREE VALUES

                ArrayList<String> degreeValuesArrayList = new ArrayList<>();

                for(double i = -12.0; i > -21.0; i -= 0.5)
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

                fajrDegreesListPreference.setVisible(PrayerTimeSettingsEntity.FAJR_DEGREE_TYPES.contains(prayerTimeType));
                ishaDegreesListPreference.setVisible(PrayerTimeSettingsEntity.ISHA_DEGREE_TYPES.contains(prayerTimeType));
                apiSettingsPreferenceCategory.setVisible(fajrDegreesListPreference.isVisible() || ishaDegreesListPreference.isVisible());

                // SET CURRENT CONFIGURATION FOR PRAYERTIMETYPE

                if(AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.containsKey(prayerTimeType))
                {
                    PrayerTimeSettingsEntity settings = AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.get(prayerTimeType);

                    if(settings != null)
                    {
                        apiSelectionListPreference.setValue(settings.get_api().toString());
                        minuteAdjustmentListPreference.setValue(String.valueOf(settings.get_minuteAdjustment()));

                        if(fajrDegreesListPreference.isVisible())
                        {
                            fajrDegreesListPreference.setValue(settings.getFajrCalculationDegree().toString());
                        }

                        if(ishaDegreesListPreference.isVisible())
                        {
                            ishaDegreesListPreference.setValue(settings.getIshaCalculationDegree().toString());
                        }
                    }
                    // TODO: Set default configuration values for new configuration
                }
            }
        }
    }
}