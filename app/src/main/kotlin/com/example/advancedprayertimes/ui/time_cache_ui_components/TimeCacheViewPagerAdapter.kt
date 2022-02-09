package com.example.advancedprayertimes.ui.time_cache_ui_components

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.advancedprayertimes.ui.time_cache_ui_components.tab_fragments.AlAdhanTimeCacheTabFragment
import com.example.advancedprayertimes.ui.time_cache_ui_components.tab_fragments.DiyanetTimeCacheTabFragment
import com.example.advancedprayertimes.ui.time_cache_ui_components.tab_fragments.MuwaqqitTimeCacheTabFragment

class TimeCacheViewPagerAdapter(fragmentActivity: FragmentActivity?) : FragmentStateAdapter(fragmentActivity!!) {

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> MuwaqqitTimeCacheTabFragment()
            1 -> DiyanetTimeCacheTabFragment()
            else -> AlAdhanTimeCacheTabFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}