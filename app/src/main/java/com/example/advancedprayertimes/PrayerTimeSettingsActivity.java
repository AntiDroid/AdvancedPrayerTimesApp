package com.example.advancedprayertimes;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;

import java.util.ArrayList;
import java.util.stream.Stream;

public class PrayerTimeSettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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