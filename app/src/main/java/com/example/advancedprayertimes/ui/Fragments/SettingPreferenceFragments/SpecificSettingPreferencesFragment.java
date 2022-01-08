package com.example.advancedprayertimes.UI.Fragments.SettingPreferenceFragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.SubTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SpecificSettingPreferencesFragment extends PreferenceFragmentCompat
{
    // region static fields

    // endregion static fields

    // region fields

    private SharedPreferences.OnSharedPreferenceChangeListener _preferenceChangeListener;

    private EPrayerTimeType _prayerType;

    // endregion fields

    public SpecificSettingPreferencesFragment(EPrayerTimeType prayerType)
    {
        super();

        _prayerType = prayerType;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        if(_prayerType == EPrayerTimeType.Asr)
        {
            setPreferencesFromResource(R.xml.asr_prayer_night_settings_preferences, rootKey);
            createAsrPreferences();
        }
        else if(_prayerType == EPrayerTimeType.Isha)
        {
            setPreferencesFromResource(R.xml.isha_prayer_night_settings_preferences, rootKey);
            createIshaPreferences();
        }
        else
        {
            return;
        }

//        SwitchPreference isSubtimeOneEnabledSwitchPreference = this.findPreference("isSubtimeOneEnabled");
//        ListPreference subtimeOneTimeListPreference = this.findPreference("subtimeOneTimeSelection");
//
//        SwitchPreference isSubtimeTwoEnabledSwitchPreference = this.findPreference("isSubtimeTwoEnabled");
//        ListPreference subtimeTwoTimeListPreference = this.findPreference("subtimeTwoTimeSelection");
//
//        SwitchPreference isSubtimeThreeEnabledSwitchPreference = this.findPreference("isSubtimeThreeEnabled");
//        ListPreference subtimeThreeTimeListPreference = this.findPreference("subtimeThreeTimeSelection");
//
//        if (isSubtimeOneEnabledSwitchPreference == null || subtimeOneTimeListPreference == null || isSubtimeTwoEnabledSwitchPreference == null
//                || subtimeTwoTimeListPreference == null || isSubtimeThreeEnabledSwitchPreference == null ||
//                subtimeThreeTimeListPreference == null)
//        {
//            return;
//        }
//
//        // change listener for all preference value changes
//        _preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener()
//        {
//            @Override
//            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
//            {
////                if(key.equals(PREFERENCE_NAME_API_SELECTION))
////                {
////                    updatePreferenceVisibility();
////                }
//            }
//        };
//
//        configureStuffCorrectly(subtimeOneTimeListPreference, subtimeTwoTimeListPreference, subtimeThreeTimeListPreference);
//
//        SubTimeSettingsEntity subPrayer1 = AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).getSubPrayer1Settings();
//        SubTimeSettingsEntity subPrayer2 = AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).getSubPrayer2Settings();
//        SubTimeSettingsEntity subPrayer3 = AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).getSubPrayer3Settings();
//
//        if(subPrayer1 != null)
//        {
//            isSubtimeOneEnabledSwitchPreference.setChecked(subPrayer1.isEnabled1());
//            subtimeOneTimeListPreference.setValue(subPrayer1.getTimeConfig() + "");
//        }
//        else
//        {
//            isSubtimeOneEnabledSwitchPreference.setChecked(false);
//            subtimeOneTimeListPreference.setValue("10");
//        }
//
//        if(subPrayer2 != null)
//        {
//            isSubtimeTwoEnabledSwitchPreference.setChecked(subPrayer2.isEnabled1());
//            subtimeTwoTimeListPreference.setValue(subPrayer2.getTimeConfig() + "");
//        }
//        else
//        {
//            isSubtimeTwoEnabledSwitchPreference.setChecked(false);
//            subtimeTwoTimeListPreference.setValue("10");
//        }
//
//        if(subPrayer3 != null)
//        {
//            isSubtimeThreeEnabledSwitchPreference.setChecked(subPrayer3.isEnabled1());
//            subtimeThreeTimeListPreference.setValue(subPrayer3.getTimeConfig() + "");
//        }
//        else
//        {
//            isSubtimeThreeEnabledSwitchPreference.setChecked(false);
//            subtimeThreeTimeListPreference.setValue("10");
//        }
    }

    private void configureStuffCorrectly(ListPreference subtimeOneTimeListPreference, ListPreference subtimeTwoTimeListPreference, ListPreference subtimeThreeTimeListPreference)
    {
        ArrayList<String> percentageArrayList = new ArrayList<>();

        for (int i = 10; i < 100; i += 10)
        {
            percentageArrayList.add("" + i);
        }

        subtimeOneTimeListPreference.setEntries(percentageArrayList.stream().map(x -> x + "%").collect(Collectors.toList()).toArray(new String[0]));
        subtimeOneTimeListPreference.setEntryValues(percentageArrayList.toArray(new String[0]));

        subtimeTwoTimeListPreference.setEntries(percentageArrayList.stream().map(x -> x + "%").collect(Collectors.toList()).toArray(new String[0]));
        subtimeTwoTimeListPreference.setEntryValues(percentageArrayList.toArray(new String[0]));

        subtimeThreeTimeListPreference.setEntries(percentageArrayList.stream().map(x -> x + "%").collect(Collectors.toList()).toArray(new String[0]));
        subtimeThreeTimeListPreference.setEntryValues(percentageArrayList.toArray(new String[0]));
    }

    private void createAsrPreferences()
    {
        SwitchPreference isTwoShadowLengthsEnabledSwitchPreference = this.findPreference("isTwoShadowLengthsEnabled");
        SwitchPreference isKarahaTimeEnabledSwitchPreference = this.findPreference("isKarahaTimeEnabled");
        ListPreference karahaCalculationDegreeListPreference = this.findPreference("karahaCalculationDegree");

        if (isTwoShadowLengthsEnabledSwitchPreference == null ||
                isKarahaTimeEnabledSwitchPreference == null || karahaCalculationDegreeListPreference == null)
        {
            return;
        }

        ArrayList<String> degreeValuesArrayList = new ArrayList<>();

        for (double i = 1.0; i < 10.5; i += 0.5)
        {
            degreeValuesArrayList.add("" + i);
        }

        String[] degreeValuesArray = degreeValuesArrayList.toArray(new String[0]);

        karahaCalculationDegreeListPreference.setEntries(Arrays.stream(degreeValuesArray).map(x -> x + "°").collect(Collectors.toList()).toArray(new String[0]));
        karahaCalculationDegreeListPreference.setEntryValues(degreeValuesArray);
        karahaCalculationDegreeListPreference.setValue("4.5");

        SubTimeSettingsEntity subPrayerSettings = AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).getSubPrayer1Settings();

        if(subPrayerSettings != null)
        {
            isTwoShadowLengthsEnabledSwitchPreference.setChecked(subPrayerSettings.isEnabled1());
            isKarahaTimeEnabledSwitchPreference.setChecked(subPrayerSettings.isEnabled2());
            karahaCalculationDegreeListPreference.setValue(subPrayerSettings.getAsrKarahaDegree() + "");
        }
        else
        {
            isTwoShadowLengthsEnabledSwitchPreference.setChecked(false);
            isKarahaTimeEnabledSwitchPreference.setChecked(false);
        }
    }

    private void createIshaPreferences()
    {
        SwitchPreference isThirdsOfNightEnabledSwitchPreference = this.findPreference("isThirdsOfNightEnabled");
        SwitchPreference isHalfsOfNightEnabledSwitchPreference = this.findPreference("isHalfsOfNightEnabled");

        if (isThirdsOfNightEnabledSwitchPreference == null || isHalfsOfNightEnabledSwitchPreference == null)
        {
            return;
        }

        SubTimeSettingsEntity subPrayerSettings = AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).getSubPrayer1Settings();

        if(subPrayerSettings != null)
        {
            isThirdsOfNightEnabledSwitchPreference.setChecked(subPrayerSettings.isEnabled1());
            isHalfsOfNightEnabledSwitchPreference.setChecked(subPrayerSettings.isEnabled2());
        }
        else
        {
            isThirdsOfNightEnabledSwitchPreference.setChecked(false);
            isHalfsOfNightEnabledSwitchPreference.setChecked(false);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }
}
