package com.example.advancedprayertimes.ui.activities

import androidx.appcompat.app.AppCompatActivity
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import android.os.Bundle
import com.example.advancedprayertimes.R
import com.example.advancedprayertimes.ui.prayer_setting_ui_components.PrayerSettingsViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_prayer_settings.*
import java.util.ArrayList

class PrayerSettingsActivity : AppCompatActivity() {

    private var _prayerType: EPrayerTimeType? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer_settings)

        // Get the Intent that started this activity and extract the string
        _prayerType =  this.intent.getSerializableExtra(TimeOverviewActivity.INTENT_EXTRA) as EPrayerTimeType?

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.title = _prayerType.toString() + " settings"

        configureTabs()
    }

    private val tabNames: ArrayList<String> = object : ArrayList<String>() {
        init {
            add("Beginning")
            add("End")
            add("Misc")
        }
    }

    private fun configureTabs() {
        viewPagerSetting.adapter = PrayerSettingsViewPagerAdapter(this, _prayerType!!)

        TabLayoutMediator(tabLayoutSetting, viewPagerSetting) { tab: TabLayout.Tab, position: Int ->
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