package com.example.advancedprayertimes.UI.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.SubTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.R;
import com.example.advancedprayertimes.UI.Fragments.SettingPreferenceFragments.SpecificSettingPreferencesFragment;

public class TabPrayerSpecificSettingsFragment extends Fragment
{
    private static final String ARG_PRAYER_TYPE = "prayerType";

    private EPrayerTimeType _prayerType;

    public TabPrayerSpecificSettingsFragment()
    {
        // Required empty public constructor
    }

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

    SpecificSettingPreferencesFragment prayerSubtimeSettingsFragment;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null)
        {
            _prayerType = EPrayerTimeType.valueOf(getArguments().getString(ARG_PRAYER_TYPE));
        }

        prayerSubtimeSettingsFragment = new SpecificSettingPreferencesFragment(_prayerType);

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
//        if(_prayerType == EPrayerTimeType.Asr)
//        {
//            saveAsrSettingConfiguration();
//        }
//        else if(_prayerType == EPrayerTimeType.Isha)
//        {
//            saveIshaSettingConfiguration();
//        }

        super.onStop();
    }

    private void saveAsrSettingConfiguration()
    {
        SwitchPreference isTwoShadowLengthsEnabledSwitchPreference = this.prayerSubtimeSettingsFragment.findPreference("isTwoShadowLengthsEnabled");
        SwitchPreference isKarahaTimeEnabledSwitchPreference = this.prayerSubtimeSettingsFragment.findPreference("isKarahaTimeEnabled");
        ListPreference karahaCalculationDegreeListPreference = this.prayerSubtimeSettingsFragment.findPreference("karahaCalculationDegree");

        if (isTwoShadowLengthsEnabledSwitchPreference == null ||
                isKarahaTimeEnabledSwitchPreference == null || karahaCalculationDegreeListPreference == null)
        {
            return;
        }

        boolean isTwoShadowLengthsEnabled = isTwoShadowLengthsEnabledSwitchPreference.isChecked();
        boolean isKarahaTimeEnabled = isKarahaTimeEnabledSwitchPreference.isChecked();
        Double karahaDegree = Double.parseDouble(karahaCalculationDegreeListPreference.getValue());

        SubTimeSettingsEntity settingsEntity = new SubTimeSettingsEntity(isTwoShadowLengthsEnabled, isKarahaTimeEnabled, karahaDegree);
        AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).setSubPrayer1Settings(settingsEntity);
    }

    private void saveIshaSettingConfiguration()
    {
        SwitchPreference isThirdsOfNightEnabledSwitchPreference = this.prayerSubtimeSettingsFragment.findPreference("isThirdsOfNightEnabled");
        SwitchPreference isHalfsOfNightEnabledSwitchPreference = this.prayerSubtimeSettingsFragment.findPreference("isHalfsOfNightEnabled");

        if (isThirdsOfNightEnabledSwitchPreference == null || isHalfsOfNightEnabledSwitchPreference == null)
        {
            return;
        }

        boolean isThirdsOfNightEnabled = isThirdsOfNightEnabledSwitchPreference.isChecked();
        boolean isHalfesOfNightEnabled = isHalfsOfNightEnabledSwitchPreference.isChecked();

        SubTimeSettingsEntity settingsEntity = new SubTimeSettingsEntity(isThirdsOfNightEnabled, isHalfesOfNightEnabled, null);
        AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).setSubPrayer1Settings(settingsEntity);
    }
}