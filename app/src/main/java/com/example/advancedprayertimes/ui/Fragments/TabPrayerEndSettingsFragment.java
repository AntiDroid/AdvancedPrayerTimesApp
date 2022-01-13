package com.example.advancedprayertimes.UI.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.R;
import com.example.advancedprayertimes.UI.Fragments.SettingPreferenceFragments.PrayerBeginningEndSettingPreferencesFragment;

public class TabPrayerEndSettingsFragment extends Fragment
{
    private static final String ARG_PRAYER_TYPE = "prayerType";

    private EPrayerTimeType _prayerType;

    public static TabPrayerEndSettingsFragment newInstance(EPrayerTimeType prayerType)
    {
        TabPrayerEndSettingsFragment fragment = new TabPrayerEndSettingsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PRAYER_TYPE, prayerType.toString());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_prayer_end_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null)
        {
            _prayerType = EPrayerTimeType.valueOf(getArguments().getString(ARG_PRAYER_TYPE));
        }

        PrayerBeginningEndSettingPreferencesFragment prayerBeginningEndSettingPreferencesFragment = new PrayerBeginningEndSettingPreferencesFragment(_prayerType, false);

        if (savedInstanceState == null)
        {
            this.getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsEnd, prayerBeginningEndSettingPreferencesFragment)
                    .commit();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }
}