package com.example.advancedprayertimes.ui.time_cache_ui_components.tab_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.advancedprayertimes.R
import com.example.advancedprayertimes.logic.db.DBMuwaqqitHelper
import com.example.advancedprayertimes.logic.extensions.toStringByFormat
import com.example.advancedprayertimes.logic.util.createTableLayout
import kotlinx.android.synthetic.main.fragment_muwaqqit_time_cache.*

class MuwaqqitTimeCacheTabFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_muwaqqit_time_cache, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayData = arrayListOf(
            arrayOf("Date", "Fajr", "Shuruq", "Dhuhr", "Asr", "Maghrib", "Isha")
        )

        for(entry in DBMuwaqqitHelper.getAllMuwaqqitPrayerTimes().sortedBy { x -> x.date }) {

            with(entry) {
                displayData.add(
                        arrayOf(
                        date!!.toStringByFormat("dd.MM.yy"),
                        //"$fajrAngle°/$asrKarahaAngle°/$ishaAngle",
                        fajrTime!!.toStringByFormat("HH:mm"),
                        sunriseTime!!.toStringByFormat("HH:mm"),
                        dhuhrTime!!.toStringByFormat("HH:mm"),
                        asrTime!!.toStringByFormat("HH:mm"),
                        maghribTime!!.toStringByFormat("HH:mm"),
                        ishaTime!!.toStringByFormat("HH:mm")
                    )
                )
            }
        }
        if(displayData.any()) {
            muwaqqitTimeCache.addView(createTableLayout(this.requireContext(), displayData.toTypedArray()))
        }
    }
}