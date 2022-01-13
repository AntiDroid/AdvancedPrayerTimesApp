package com.example.advancedprayertimes.UI.Fragments.SettingPreferenceFragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.DataManagementUtil;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerSettingsEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.SubTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.R;
import com.google.gson.Gson;

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
        SubTimeSettingsEntity settings = AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).getSubPrayer1Settings();

        if(settings == null)
        {
            settings = new SubTimeSettingsEntity(false, false, 4.5);
            AppEnvironment.prayerSettingsByPrayerType.get(_prayerType).setSubPrayer1Settings(settings);
        }

        if(_prayerType == EPrayerTimeType.Asr)
        {
            setPreferencesFromResource(R.xml.asr_prayer_night_settings_preferences, rootKey);
            createAsrPreferences(settings);
        }
        else if(_prayerType == EPrayerTimeType.Isha)
        {
            setPreferencesFromResource(R.xml.isha_prayer_night_settings_preferences, rootKey);
            createIshaPreferences(settings);
        }
        else
        {
            return;
        }

        // change listener for all preference value changes
        _preferenceChangeListener = this::onPreferenceChange;
    }

    private SwitchPreference isTwoShadowLengthsEnabledSwitchPreference = null;
    private SwitchPreference isKarahaTimeEnabledSwitchPreference = null;
    private ListPreference karahaCalculationDegreeListPreference = null;

    private SwitchPreference isThirdsOfNightEnabledSwitchPreference = null;
    private SwitchPreference isHalfsOfNightEnabledSwitchPreference = null;

    private void onPreferenceChange(SharedPreferences sharedPreferences, String key)
    {
        PrayerSettingsEntity prayerSettings = AppEnvironment.prayerSettingsByPrayerType.get(_prayerType);

        if(prayerSettings == null)
        {
            return;
        }

        if(_prayerType == EPrayerTimeType.Asr)
        {
            if(key.equals(isTwoShadowLengthsEnabledSwitchPreference.getKey()) && sharedPreferences.contains(isTwoShadowLengthsEnabledSwitchPreference.getKey()))
            {
                boolean isTwoShadowLengthsEnabled = sharedPreferences.getBoolean(isTwoShadowLengthsEnabledSwitchPreference.getKey(), false);
                prayerSettings.getSubPrayer1Settings().setEnabled1(isTwoShadowLengthsEnabled);
            }
            else if(key.equals(isKarahaTimeEnabledSwitchPreference.getKey()) && sharedPreferences.contains(isKarahaTimeEnabledSwitchPreference.getKey()))
            {
                boolean isKarahaTimeEnabled = sharedPreferences.getBoolean(isKarahaTimeEnabledSwitchPreference.getKey(), false);
                prayerSettings.getSubPrayer1Settings().setEnabled2(isKarahaTimeEnabled);
            }
            else if(key.equals(karahaCalculationDegreeListPreference.getKey()) && sharedPreferences.contains(karahaCalculationDegreeListPreference.getKey()))
            {
                Double fajrCalculationDegrees = Double.parseDouble(sharedPreferences.getString(karahaCalculationDegreeListPreference.getKey(), "0.0"));
                prayerSettings.getSubPrayer1Settings().setAsrKarahaDegree(fajrCalculationDegrees);
            }
        }
        else if(_prayerType == EPrayerTimeType.Isha)
        {
            if(key.equals(isThirdsOfNightEnabledSwitchPreference.getKey()) && sharedPreferences.contains(isThirdsOfNightEnabledSwitchPreference.getKey()))
            {
                boolean isThirdsOfNightEnabled = sharedPreferences.getBoolean(isThirdsOfNightEnabledSwitchPreference.getKey(), false);
                prayerSettings.getSubPrayer1Settings().setEnabled1(isThirdsOfNightEnabled);
            }
            else if(key.equals(isHalfsOfNightEnabledSwitchPreference.getKey()) && sharedPreferences.contains(isHalfsOfNightEnabledSwitchPreference.getKey()))
            {
                boolean isHalfsOfNightEnabled = sharedPreferences.getBoolean(isHalfsOfNightEnabledSwitchPreference.getKey(), false);
                prayerSettings.getSubPrayer1Settings().setEnabled2(isHalfsOfNightEnabled);
            }
        }

        String jsonString = new Gson().toJson(prayerSettings);

        SharedPreferences globalSharedPreference = this.getActivity().getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        globalSharedPreference.edit().putString(DataManagementUtil.GetTimeSettingsEntityKeyForSharedPreference(_prayerType), jsonString).commit();
    }

    private void createAsrPreferences(SubTimeSettingsEntity settings)
    {
        isTwoShadowLengthsEnabledSwitchPreference = this.findPreference("isTwoShadowLengthsEnabled");
        isKarahaTimeEnabledSwitchPreference = this.findPreference("isKarahaTimeEnabled");
        karahaCalculationDegreeListPreference = this.findPreference("karahaCalculationDegree");

        if (isTwoShadowLengthsEnabledSwitchPreference == null ||
                isKarahaTimeEnabledSwitchPreference == null || karahaCalculationDegreeListPreference == null)
        {
            return;
        }

        isTwoShadowLengthsEnabledSwitchPreference.setChecked(settings.isEnabled1());
        isKarahaTimeEnabledSwitchPreference.setChecked(settings.isEnabled2());
        karahaCalculationDegreeListPreference.setValue(settings.getAsrKarahaDegree() + "");
    }

    private void createIshaPreferences(SubTimeSettingsEntity settings)
    {
        isThirdsOfNightEnabledSwitchPreference = this.findPreference("isThirdsOfNightEnabled");
        isHalfsOfNightEnabledSwitchPreference = this.findPreference("isHalfsOfNightEnabled");

        if (isThirdsOfNightEnabledSwitchPreference == null || isHalfsOfNightEnabledSwitchPreference == null)
        {
            return;
        }

        isThirdsOfNightEnabledSwitchPreference.setChecked(settings.isEnabled1());
        isHalfsOfNightEnabledSwitchPreference.setChecked(settings.isEnabled2());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(_preferenceChangeListener);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(_preferenceChangeListener);
    }
}
