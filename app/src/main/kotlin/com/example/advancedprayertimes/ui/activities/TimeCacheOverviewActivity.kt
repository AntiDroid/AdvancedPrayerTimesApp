package com.example.advancedprayertimes.ui.activities

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.advancedprayertimes.R
import com.example.advancedprayertimes.logic.AppEnvironment
import com.example.advancedprayertimes.logic.api_entities.MuwaqqitPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.extensions.toStringByFormat
import com.example.advancedprayertimes.logic.util.*

class TimeCacheOverviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache_dev_view)

        this.supportActionBar?.hide()

//        val gridView = findViewById<View>(R.id.gridView) as GridView
//
//        val arrayofName = listOf<String>("Talip", "Talip2")
//
//        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
//            this,
//            android.R.layout.simple_list_item_checked, arrayofName
//        )
//
//        gridView.adapter = adapter
//
//        gridView.onItemClickListener = OnItemClickListener { _, v, _, _ ->
//            Toast.makeText(applicationContext, (v as TextView).text, Toast.LENGTH_SHORT)
//                .show()
//        }
    }
}