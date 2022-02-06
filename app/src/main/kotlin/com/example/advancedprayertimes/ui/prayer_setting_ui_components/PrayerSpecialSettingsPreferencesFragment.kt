package com.example.advancedprayertimes.ui.prayer_setting_ui_components

import android.content.Context
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import androidx.preference.PreferenceFragmentCompat
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import com.example.advancedprayertimes.logic.setting_entities.SubTimeSettingsEntity
import com.example.advancedprayertimes.logic.AppEnvironment
import com.example.advancedprayertimes.R
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.SwitchPreference
import com.example.advancedprayertimes.logic.util.DataManagementUtil
import com.google.gson.Gson

class PrayerSpecialSettingsPreferencesFragment (private val _prayerType: EPrayerTimeType) : PreferenceFragmentCompat() {

    private var _preferenceChangeListener: OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        var settings = AppEnvironment.prayerSettingsByPrayerType[_prayerType]!!.subPrayer1Settings

        if (settings == null) {
            settings = SubTimeSettingsEntity(false, false, 4.5, -10.0)
            AppEnvironment.prayerSettingsByPrayerType[_prayerType]!!.subPrayer1Settings = settings
        }

        when(_prayerType){
            EPrayerTimeType.Asr -> {
                setPreferencesFromResource(R.xml.asr_prayer_settings_preferences, rootKey)
                createAsrPreferences(settings)
            }
            EPrayerTimeType.Maghrib -> {
                setPreferencesFromResource(R.xml.maghrib_prayer_settings_preferences, rootKey)
                createMaghribPreferences(settings)
            }
            EPrayerTimeType.Isha -> {
                setPreferencesFromResource(R.xml.isha_prayer_settings_preferences, rootKey)
                createIshaPreferences(settings)
            }
            else -> return
        }

        // change listener for all preference value changes
        _preferenceChangeListener =
            OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, key: String ->
                onPreferenceChange(
                    sharedPreferences,
                    key
                )
            }
    }

    private var isTwoShadowLengthsEnabledSwitchPreference: SwitchPreference? = null
    private var isKarahaTimeEnabledSwitchPreference: SwitchPreference? = null
    private var karahaCalculationDegreeListPreference: ListPreference? = null

    private var isThirdsOfNightEnabledSwitchPreference: SwitchPreference? = null
    private var isHalfsOfNightEnabledSwitchPreference: SwitchPreference? = null

    private var isIshtibaqTimeEnabled: SwitchPreference? = null
    private var ishtibaqCalculationDegree: ListPreference? = null

    private fun onPreferenceChange(sharedPreferences: SharedPreferences, key: String) {

        val prayerSettings = AppEnvironment.prayerSettingsByPrayerType[_prayerType] ?: return

        if(sharedPreferences.contains(key)) {

            val specialPrayerSettings: SubTimeSettingsEntity? = prayerSettings.subPrayer1Settings

            if (_prayerType === EPrayerTimeType.Asr) {

                when(key) {
                    isTwoShadowLengthsEnabledSwitchPreference!!.key -> {
                        specialPrayerSettings!!.isEnabled1 = sharedPreferences.getBoolean(key, false)
                    }
                    isKarahaTimeEnabledSwitchPreference!!.key -> {
                        specialPrayerSettings!!.isEnabled2 = sharedPreferences.getBoolean(key, false)
                    }
                    karahaCalculationDegreeListPreference!!.key -> {
                        specialPrayerSettings!!.asrKarahaDegree = sharedPreferences.getString(key, "0.0")!!.toDouble()
                    }
                }

            }
            else if (_prayerType === EPrayerTimeType.Maghrib) {

                when(key) {
                    isIshtibaqTimeEnabled!!.key -> {
                        specialPrayerSettings!!.isEnabled1 = sharedPreferences.getBoolean(key, false)
                    }
                    ishtibaqCalculationDegree!!.key -> {
                        specialPrayerSettings!!.ishtibaqDegree = sharedPreferences.getString(key, "0.0")!!.toDouble()
                    }
                }

            }
            else if (_prayerType === EPrayerTimeType.Isha) {

                when(key) {
                    isThirdsOfNightEnabledSwitchPreference!!.key -> {
                        specialPrayerSettings!!.isEnabled1 = sharedPreferences.getBoolean(key, false)
                    }
                    isHalfsOfNightEnabledSwitchPreference!!.key -> {
                        specialPrayerSettings!!.isEnabled2 = sharedPreferences.getBoolean(key, false)
                    }
                }

            }
        }

        val jsonString = Gson().toJson(prayerSettings)
        val globalSharedPreference = this.requireActivity()
            .getSharedPreferences(
                AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE
            )

        globalSharedPreference.edit()
            .putString(DataManagementUtil.getTimeSettingsEntityKeyForSharedPreference(_prayerType), jsonString)
            .apply()
    }

    private fun createAsrPreferences(settings: SubTimeSettingsEntity) {

        isTwoShadowLengthsEnabledSwitchPreference = findPreference("isTwoShadowLengthsEnabled")
        isKarahaTimeEnabledSwitchPreference = findPreference("isKarahaTimeEnabled")
        karahaCalculationDegreeListPreference = findPreference("karahaCalculationDegree")

        if (isTwoShadowLengthsEnabledSwitchPreference == null || isKarahaTimeEnabledSwitchPreference == null || karahaCalculationDegreeListPreference == null) {
            return
        }

        isTwoShadowLengthsEnabledSwitchPreference!!.isChecked = settings.isEnabled1
        isKarahaTimeEnabledSwitchPreference!!.isChecked = settings.isEnabled2
        karahaCalculationDegreeListPreference!!.value = settings.asrKarahaDegree.toString() + ""
    }

    private fun createMaghribPreferences(settings: SubTimeSettingsEntity) {

        isIshtibaqTimeEnabled = findPreference("isIshtibaqTimeEnabled")
        ishtibaqCalculationDegree = findPreference("ishtibaqCalculationDegree")

        if (isIshtibaqTimeEnabled == null || ishtibaqCalculationDegree == null) {
            return
        }

        isIshtibaqTimeEnabled!!.isChecked = settings.isEnabled1
        ishtibaqCalculationDegree!!.value = settings.ishtibaqDegree.toString() + ""
    }

    private fun createIshaPreferences(settings: SubTimeSettingsEntity) {

        isThirdsOfNightEnabledSwitchPreference = findPreference("isThirdsOfNightEnabled")
        isHalfsOfNightEnabledSwitchPreference = findPreference("isHalfsOfNightEnabled")

        if (isThirdsOfNightEnabledSwitchPreference == null || isHalfsOfNightEnabledSwitchPreference == null) {
            return
        }

        isThirdsOfNightEnabledSwitchPreference!!.isChecked = settings.isEnabled1
        isHalfsOfNightEnabledSwitchPreference!!.isChecked = settings.isEnabled2
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(_preferenceChangeListener)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(_preferenceChangeListener)
    }
}