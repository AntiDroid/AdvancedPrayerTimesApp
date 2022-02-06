package com.example.advancedprayertimes.ui.prayer_setting_ui_components

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.advancedprayertimes.ui.prayer_setting_ui_components.tab_fragments.PrayerBeginningSettingsTabFragment
import com.example.advancedprayertimes.ui.prayer_setting_ui_components.tab_fragments.PrayerEndSettingsTabFragment
import com.example.advancedprayertimes.ui.prayer_setting_ui_components.tab_fragments.PrayerSpecificSettingsTabFragment

class PrayerSettingsViewPagerAdapter(fragmentActivity: FragmentActivity?, private val _prayerType: EPrayerTimeType
) : FragmentStateAdapter(
    fragmentActivity!!
) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PrayerBeginningSettingsTabFragment.newInstance(_prayerType)
            1 -> PrayerEndSettingsTabFragment.newInstance(_prayerType)
            else -> PrayerSpecificSettingsTabFragment.newInstance(_prayerType)
        }
    }

    override fun getItemCount(): Int {
        return when(_prayerType) {
            EPrayerTimeType.Asr, EPrayerTimeType.Maghrib, EPrayerTimeType.Isha -> 3
            else -> 2
        }
    }
}