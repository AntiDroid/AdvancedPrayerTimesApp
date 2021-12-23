package com.example.advancedprayertimes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.DayPrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.databinding.OverviewActivityBinding;
import com.example.advancedprayertimes.databinding.PrayerTimeSettingsActivityBinding;

import java.util.ArrayList;
import java.util.stream.Stream;

public class PrayerTimeSettingsActivity extends AppCompatActivity
{
    EPrayerTimeType prayerTimeType;
    SettingsFragment settingsFragment = new SettingsFragment();

    PrayerTimeSettingsActivityBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prayer_time_settings_activity);

        binding = PrayerTimeSettingsActivityBinding.inflate(getLayoutInflater());

        // Get the Intent that started this activity and extract the string
        Intent intent = this.getIntent();
        prayerTimeType = (EPrayerTimeType) intent.getSerializableExtra(OverviewActivity.INTENT_EXTRA);

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
        AppEnvironment.DayPrayerTimeSettings.remove(prayerTimeType);

        ListPreference apiSelectionListPreference = this.settingsFragment.findPreference("apiSelection");
        ListPreference minuteAdjustmentListPreference = this.settingsFragment.findPreference("minuteAdjustmentSelection");
        ListPreference fajrDegreesListPreference = this.settingsFragment.findPreference("fajrCalculationDegrees");
        ListPreference ishaDegreesListPreference = this.settingsFragment.findPreference("ishaCalculationDegrees");

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

        AppEnvironment.DayPrayerTimeSettings.put(prayerTimeType, new DayPrayerTimeSettingsEntity(api, minuteAdjustment, fajrCalculationDegrees, ishaCalculationDegrees));
        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EPrayerTimeType prayerTimeType = ((PrayerTimeSettingsActivity) this.requireActivity()).prayerTimeType;

            ListPreference apiSelectionListPreference = this.findPreference("apiSelection");
            ListPreference minuteAdjustmentListPreference = this.findPreference("minuteAdjustmentSelection");
            ListPreference fajrDegreesListPreference = this.findPreference("fajrCalculationDegrees");
            ListPreference ishaDegreesListPreference = this.findPreference("ishaCalculationDegrees");

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

                // SET MINUTE ADJUSTMENT VALUES

                ArrayList<String> minuteAdjustmentValuesArrayList = new ArrayList<>();

                for(int i = -15; i < 16; i++)
                {
                    minuteAdjustmentValuesArrayList.add("" + i);
                }

                minuteAdjustmentListPreference.setEntries(minuteAdjustmentValuesArrayList.toArray(new String[0]));
                minuteAdjustmentListPreference.setEntryValues(minuteAdjustmentValuesArrayList.toArray(new String[0]));

                // SET DEGREE VALUES

                ArrayList<String> degreeValuesArrayList = new ArrayList<>();

                for(double i = 12.0; i < 21.0; i = i + 0.5)
                {
                    degreeValuesArrayList.add("" + i);
                }

                fajrDegreesListPreference.setEntries(degreeValuesArrayList.toArray(new String[0]));
                fajrDegreesListPreference.setEntryValues(degreeValuesArrayList.toArray(new String[0]));

                ishaDegreesListPreference.setEntries(degreeValuesArrayList.toArray(new String[0]));
                ishaDegreesListPreference.setEntryValues(degreeValuesArrayList.toArray(new String[0]));

                fajrDegreesListPreference.setVisible(prayerTimeType == EPrayerTimeType.FajrBeginning || prayerTimeType == EPrayerTimeType.IshaEnd);
                ishaDegreesListPreference.setVisible(prayerTimeType == EPrayerTimeType.IshaBeginning || prayerTimeType == EPrayerTimeType.MaghribEnd);
                apiSettingsPreferenceCategory.setVisible(fajrDegreesListPreference.isVisible() || ishaDegreesListPreference.isVisible());

                // SET CURRENT CONFIGURATION FOR PRAYERTIMETYPE

                if(AppEnvironment.DayPrayerTimeSettings.containsKey(prayerTimeType))
                {
                    DayPrayerTimeSettingsEntity settings = AppEnvironment.DayPrayerTimeSettings.get(prayerTimeType);

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
                }
            }
        }
    }
}