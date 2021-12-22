package com.example.advancedprayertimes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.DayPrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;

import java.util.ArrayList;
import java.util.stream.Stream;

public class PrayerTimeSettingsActivity extends AppCompatActivity
{
    EPrayerTimeType prayerTimeType;
    SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prayer_time_settings_activity);

        //PrayerTimeSettingsActivityBinding binding = PrayerTimeSettingsActivityBinding.inflate(getLayoutInflater());

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
        }
    }

    @Override
    protected void onStop()
    {
        AppEnvironment.Instance().DayPrayerTimeSettings.remove(prayerTimeType);

        ListPreference apiSelector = settingsFragment.findPreference("apiSelection");
        ListPreference minuteAdjustmentSelector = settingsFragment.findPreference("minuteAdjustmentSelection");

        ESupportedAPIs api = ESupportedAPIs.Undefined;
        int minuteAdjustment = 0;

        if(apiSelector != null)
        {
            api = ESupportedAPIs.valueOf(apiSelector.getValue());
        }

        if(minuteAdjustmentSelector != null)
        {
            minuteAdjustment = Integer.parseInt(minuteAdjustmentSelector.getValue());
        }

        AppEnvironment.Instance().DayPrayerTimeSettings.put(prayerTimeType, new DayPrayerTimeSettingsEntity(api, minuteAdjustment));
        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            ListPreference apiSelectionListPreference = this.findPreference("apiSelection");
            ListPreference minuteAdjustmentListPreference = this.findPreference("minuteAdjustmentSelection");

            if(apiSelectionListPreference != null && minuteAdjustmentListPreference != null)
            {
                String[] entries = Stream.of(ESupportedAPIs.values()).map(ESupportedAPIs::name).toArray(String[]::new);

                apiSelectionListPreference.setEntries(entries);
                apiSelectionListPreference.setEntryValues(entries);

                // ####

                ArrayList<String> entryList = new ArrayList<>();

                for(int i = -15; i < 16; i++)
                {
                    entryList.add("" + i);
                }

                minuteAdjustmentListPreference.setEntries(entryList.toArray(new String[0]));
                minuteAdjustmentListPreference.setEntryValues(entryList.toArray(new String[0]));

                // ####

                EPrayerTimeType prayerTimeType = ((PrayerTimeSettingsActivity) this.requireActivity()).prayerTimeType;

                if(AppEnvironment.Instance().DayPrayerTimeSettings.containsKey(prayerTimeType))
                {
                    DayPrayerTimeSettingsEntity settings = AppEnvironment.Instance().DayPrayerTimeSettings.get(prayerTimeType);

                    if(settings != null)
                    {
                        apiSelectionListPreference.setValue(settings.get_api().toString());
                        minuteAdjustmentListPreference.setValue(String.valueOf(settings.get_minuteAdjustment()));
                    }
                }
            }
        }
    }
}