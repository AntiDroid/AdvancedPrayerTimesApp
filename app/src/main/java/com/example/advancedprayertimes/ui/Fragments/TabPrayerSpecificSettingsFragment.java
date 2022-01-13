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
import com.example.advancedprayertimes.UI.Fragments.SettingPreferenceFragments.SpecificSettingPreferencesFragment;

public class TabPrayerSpecificSettingsFragment extends Fragment
{
    private static final String ARG_PRAYER_TYPE = "prayerType";

    private EPrayerTimeType _prayerType;

    public static TabPrayerSpecificSettingsFragment newInstance(EPrayerTimeType prayerType)
    {
        TabPrayerSpecificSettingsFragment fragment = new TabPrayerSpecificSettingsFragment();

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
        return inflater.inflate(R.layout.fragment_prayer_specific_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null)
        {
            _prayerType = EPrayerTimeType.valueOf(getArguments().getString(ARG_PRAYER_TYPE));
        }

        SpecificSettingPreferencesFragment prayerSubtimeSettingsFragment = new SpecificSettingPreferencesFragment(_prayerType);

        if (savedInstanceState == null)
        {
            this.getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsSubtimes, prayerSubtimeSettingsFragment)
                    .commit();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }
}