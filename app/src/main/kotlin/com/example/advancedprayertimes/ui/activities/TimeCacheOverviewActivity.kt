package com.example.advancedprayertimes.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.advancedprayertimes.R
import com.example.advancedprayertimes.ui.time_cache_ui_components.TimeCacheViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_cache_dev_view.*

class TimeCacheOverviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache_dev_view)

        this.supportActionBar?.hide()

        configureTabs()
    }

    private val tabNames: ArrayList<String> = object : ArrayList<String>() {
        init {
            add("Muwaqqit")
            add("Diyanet")
            add("AlAdhan")
        }
    }

    private fun configureTabs() {
        viewPagerCache.adapter = TimeCacheViewPagerAdapter(this)

        TabLayoutMediator(tabLayoutCache, viewPagerCache) { tab: TabLayout.Tab, position: Int ->
            val tabTitle =
                if (position < tabNames.size) {
                    tabNames[position]
                } else {
                    "Unknown: $position"
                }

            tab.text = tabTitle
        }.attach()
    }
}