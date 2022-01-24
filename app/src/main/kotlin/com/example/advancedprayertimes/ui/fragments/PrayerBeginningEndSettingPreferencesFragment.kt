package com.example.advancedprayertimes.ui.fragments

import android.content.Context
import com.example.advancedprayertimes.logic.DataManagementUtil.GetTimeSettingsEntityKeyForSharedPreference
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import androidx.preference.PreferenceFragmentCompat
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import android.os.Bundle
import com.example.advancedprayertimes.R
import com.example.advancedprayertimes.logic.enums.ESupportedAPIs
import com.example.advancedprayertimes.logic.setting_entities.PrayerTimeBeginningEndSettingsEntity
import com.example.advancedprayertimes.logic.AppEnvironment
import android.content.SharedPreferences
import android.view.MenuItem
import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory
import com.google.gson.Gson
import java.util.AbstractMap
import java.util.stream.Stream
import kotlin.streams.asSequence

class PrayerBeginningEndSettingPreferencesFragment(
    prayerType: EPrayerTimeType,
    private val _isBeginning: Boolean
) : PreferenceFragmentCompat() {

    private var _preferenceChangeListener: OnSharedPreferenceChangeListener? = null
    private var apiSettingsPreferenceCategory: PreferenceCategory? = null
    private var apiSelectionListPreference: ListPreference? = null
    private var minuteAdjustmentListPreference: ListPreference? = null
    private var fajrDegreesListPreference: ListPreference? = null
    private var ishaDegreesListPreference: ListPreference? = null
    var prayerTypeWithMomentType: AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>

    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.prayer_beginning_end_settings_preferences, rootKey)
        apiSettingsPreferenceCategory = findPreference(PREFERENCE_CATEGORY_API_SETTINGS)
        apiSelectionListPreference = findPreference(PREFERENCE_NAME_API_SELECTION)
        minuteAdjustmentListPreference = findPreference(PREFERENCE_NAME_MINUTE_ADJUSTMENT_SELECTION)
        fajrDegreesListPreference = findPreference(PREFERENCE_NAME_FAJR_DEGREE_SELECTION)
        ishaDegreesListPreference = findPreference(PREFERENCE_NAME_ISHA_DEGREE_SELECTION)
        if (apiSelectionListPreference == null || minuteAdjustmentListPreference == null || fajrDegreesListPreference == null || ishaDegreesListPreference == null || apiSettingsPreferenceCategory == null) {
            return
        }
        configureAPISelector(apiSelectionListPreference!!)
        var selectedAPI = ESupportedAPIs.Undefined
        var minuteAdjustmentValue = 0
        var fajrDegreeValue = -12.0
        var ishaDegreeValue = -12.0
        var settings = AppEnvironment.prayerSettingsByPrayerType[prayerTypeWithMomentType.key]!!
            .GetBeginningEndSettingByMomentType(_isBeginning)
        if (settings == null) {
            settings = PrayerTimeBeginningEndSettingsEntity(
                selectedAPI,
                minuteAdjustmentValue,
                fajrDegreeValue,
                ishaDegreeValue
            )
            AppEnvironment.prayerSettingsByPrayerType[prayerTypeWithMomentType.key]!!
                .SetBeginningEndSettingByMomentType(_isBeginning, settings)
        } else {
            selectedAPI = settings.api
            minuteAdjustmentValue = settings.minuteAdjustment
            if (settings.fajrCalculationDegree != null) {
                fajrDegreeValue = settings.fajrCalculationDegree!!
            }
            if (settings.ishaCalculationDegree != null) {
                ishaDegreeValue = settings.ishaCalculationDegree!!
            }
        }
        apiSelectionListPreference!!.value = selectedAPI.toString()
        minuteAdjustmentListPreference!!.value = minuteAdjustmentValue.toString()
        fajrDegreesListPreference!!.value = fajrDegreeValue.toString() + ""
        ishaDegreesListPreference!!.value = ishaDegreeValue.toString() + ""
        updatePreferenceVisibility()

        // change listener for all preference value changes
        _preferenceChangeListener =
            OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, key: String ->
                onPreferenceChange(
                    sharedPreferences,
                    key
                )
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun onPreferenceChange(sharedPreferences: SharedPreferences, key: String) {
        val prayerSettings = AppEnvironment.prayerSettingsByPrayerType[prayerTypeWithMomentType.key]
            ?: return
        if (key == apiSelectionListPreference!!.key && sharedPreferences.contains(
                apiSelectionListPreference!!.key
            )
        ) {
            val api = ESupportedAPIs.valueOf(apiSelectionListPreference!!.value)
            prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning)!!.api = api
            updatePreferenceVisibility()
        } else if (key == minuteAdjustmentListPreference!!.key && sharedPreferences.contains(
                minuteAdjustmentListPreference!!.key
            )
        ) {
            val minuteAdjustment = minuteAdjustmentListPreference!!.value.toInt()
            prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning)!!.minuteAdjustment =
                minuteAdjustment
        } else if (key == fajrDegreesListPreference!!.key && sharedPreferences.contains(
                fajrDegreesListPreference!!.key
            )
        ) {
            val fajrCalculationDegrees = fajrDegreesListPreference!!.value.toDouble()
            prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning)!!.fajrCalculationDegree =
                fajrCalculationDegrees
        } else if (key == ishaDegreesListPreference!!.key && sharedPreferences.contains(
                ishaDegreesListPreference!!.key
            )
        ) {
            val ishaCalculationDegrees = ishaDegreesListPreference!!.value.toDouble()
            prayerSettings.GetBeginningEndSettingByMomentType(_isBeginning)!!.ishaCalculationDegree =
                ishaCalculationDegrees
        }
        val jsonString = Gson().toJson(prayerSettings)
        val globalSharedPreference = this.requireActivity()
            .getSharedPreferences(
                AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE
            )
        globalSharedPreference.edit().putString(
            GetTimeSettingsEntityKeyForSharedPreference(prayerTypeWithMomentType.key),
            jsonString
        ).commit()
    }

    // region methods
    private fun configureAPISelector(apiSelectionListPreference: ListPreference) {
        var apiNamesArray: Array<String>? = null
        apiNamesArray = if (prayerTypeWithMomentType.key === EPrayerTimeType.Duha) {
            Stream.of(*ESupportedAPIs.values()).asSequence()
                .filter { x: ESupportedAPIs -> x === ESupportedAPIs.Undefined || x === ESupportedAPIs.Muwaqqit }
                .map { x -> x.toString() }
                .toList().toTypedArray()
        } else {
            Stream.of(*ESupportedAPIs.values()).asSequence()
                .map { obj: ESupportedAPIs -> obj.name }
                .toList().toTypedArray()
        }
        apiSelectionListPreference.entries = apiNamesArray
        apiSelectionListPreference.entryValues = apiNamesArray
    }

    private fun updatePreferenceVisibility() {
        val isDegreeAPISelected = (ESupportedAPIs.valueOf(
            apiSelectionListPreference!!.value
        ) === ESupportedAPIs.Muwaqqit
                ||
                ESupportedAPIs.valueOf(apiSelectionListPreference!!.value) === ESupportedAPIs.AlAdhan)
        val showFajrDegreeSelector =
            isDegreeAPISelected && PrayerTimeBeginningEndSettingsEntity.FAJR_DEGREE_TYPES.contains(
                prayerTypeWithMomentType
            )
        val showIshaDegreeSelector =
            isDegreeAPISelected && PrayerTimeBeginningEndSettingsEntity.ISHA_DEGREE_TYPES.contains(
                prayerTypeWithMomentType
            )
        fajrDegreesListPreference!!.isVisible = showFajrDegreeSelector
        ishaDegreesListPreference!!.isVisible = showIshaDegreeSelector
        apiSettingsPreferenceCategory!!.isVisible =
            fajrDegreesListPreference!!.isVisible || ishaDegreesListPreference!!.isVisible
    }

    // endregion methods
    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(
            _preferenceChangeListener
        )
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(
            _preferenceChangeListener
        )
    }

    companion object {
        // region static fields
        private const val PREFERENCE_CATEGORY_API_SETTINGS = "apiSettingsPreferenceCategory"
        private const val PREFERENCE_NAME_API_SELECTION = "apiSelection"
        private const val PREFERENCE_NAME_MINUTE_ADJUSTMENT_SELECTION = "minuteAdjustmentSelection"
        private const val PREFERENCE_NAME_FAJR_DEGREE_SELECTION = "fajrCalculationDegree"
        private const val PREFERENCE_NAME_ISHA_DEGREE_SELECTION = "ishaCalculationDegree"
    }

    // endregion fields
    init {
        prayerTypeWithMomentType = AbstractMap.SimpleEntry(
            prayerType,
            if (_isBeginning) EPrayerTimeMomentType.Beginning else EPrayerTimeMomentType.End
        )
    }
}