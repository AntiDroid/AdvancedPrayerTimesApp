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
import com.example.advancedprayertimes.databinding.PrayerTimeSettingsActivityBinding;

import java.util.ArrayList;
import java.util.stream.Stream;

public class PrayerTimeSettingsActivity extends AppCompatActivity
{
    private static PrayerTimeSettingsActivityBinding binding = null;
    EPrayerTimeType prayerTimeType;
    SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prayer_time_settings_activity);

        binding = PrayerTimeSettingsActivityBinding.inflate(getLayoutInflater());

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        prayerTimeType = (EPrayerTimeType) intent.getSerializableExtra(OverviewActivity.INTENT_EXTRA);

        if (savedInstanceState == null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, settingsFragment)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStop()
    {
        if(AppEnvironment.Instance().DayPrayerTimeSettings.containsKey(prayerTimeType))
        {
            AppEnvironment.Instance().DayPrayerTimeSettings.remove(prayerTimeType);
        }

        ESupportedAPIs api = ESupportedAPIs.valueOf(((ListPreference)settingsFragment.findPreference("apiSelection")).getValue());
        int minuteAdjustment = Integer.valueOf(((ListPreference)settingsFragment.findPreference("minuteAdjustmentSelection")).getValue());

        DayPrayerTimeSettingsEntity settingsEntity = new DayPrayerTimeSettingsEntity(api, minuteAdjustment);

        AppEnvironment.Instance().DayPrayerTimeSettings.put(prayerTimeType, settingsEntity);

        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            ListPreference apiSelectionListPreference = this.findPreference("apiSelection");
            String[] entries = Stream.of(ESupportedAPIs.values()).map(ESupportedAPIs::name).toArray(String[]::new);

            apiSelectionListPreference.setEntries(entries);
            apiSelectionListPreference.setEntryValues(entries);

            ListPreference minuteAdjustmentListPreference = this.findPreference("minuteAdjustmentSelection");

            ArrayList<String> entryList = new ArrayList<String>();

            for(int i = -15; i < 16; i++)
            {
                entryList.add("" + i);
            }

            minuteAdjustmentListPreference.setEntries(entryList.toArray(new String[0]));
            minuteAdjustmentListPreference.setEntryValues(entryList.toArray(new String[0]));
        }
    }
}