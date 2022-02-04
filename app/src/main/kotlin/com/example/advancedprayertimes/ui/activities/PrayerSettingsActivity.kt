package com.example.advancedprayertimes.ui.activities

import androidx.appcompat.app.AppCompatActivity
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import android.os.Bundle
import com.example.advancedprayertimes.R
import com.google.android.material.tabs.TabLayout
import com.example.advancedprayertimes.ui.PrayerSettingsViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_prayer_settings.*
import java.util.ArrayList

class PrayerSettingsActivity : AppCompatActivity()
{
    var _prayerType: EPrayerTimeType? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer_settings)

        // Get the Intent that started this activity and extract the string
        _prayerType =  this.intent.getSerializableExtra(TimeOverviewActivity.INTENT_EXTRA) as EPrayerTimeType?

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.title = _prayerType.toString() + " settings"

        configureTabs()
    }

    var tabNames: ArrayList<String> = object : ArrayList<String>()
    {
        init
        {
            add("Beginning")
            add("End")
            add("Misc")
        }
    }

    private fun configureTabs()
    {
        val adapter = PrayerSettingsViewPagerAdapter(this, _prayerType!!)
        viewPager.adapter = adapter

        TabLayoutMediator(
            tabLayout, viewPager
        )
        { tab: TabLayout.Tab, position: Int ->
            var tabTitle = "Unknown: $position"

            if (position < tabNames.size)
            {
                tabTitle = tabNames[position]
            }
            tab.text = tabTitle
        }.attach()
    }

    override fun onStop()
    {
        super.onStop()
    }
}