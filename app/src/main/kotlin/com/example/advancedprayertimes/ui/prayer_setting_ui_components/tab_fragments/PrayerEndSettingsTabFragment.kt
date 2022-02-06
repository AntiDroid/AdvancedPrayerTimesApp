package com.example.advancedprayertimes.ui.prayer_setting_ui_components.tab_fragments

import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.advancedprayertimes.R
import com.example.advancedprayertimes.ui.prayer_setting_ui_components.PrayerBeginningEndSettingPreferencesFragment

class PrayerEndSettingsTabFragment : Fragment() {

    private lateinit var _prayerType: EPrayerTimeType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_prayer_end_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            _prayerType = EPrayerTimeType.valueOf(this.requireArguments().getString(ARG_PRAYER_TYPE)!!)
        }

        val prayerBeginningEndSettingPreferencesFragment =
            PrayerBeginningEndSettingPreferencesFragment(
                _prayerType!!, false
            )

        if (savedInstanceState == null) {
            this.parentFragmentManager
                .beginTransaction()
                .replace(R.id.settingsEnd, prayerBeginningEndSettingPreferencesFragment)
                .commit()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    companion object {

        private const val ARG_PRAYER_TYPE = "prayerType"

        @JvmStatic
        fun newInstance(prayerType: EPrayerTimeType): PrayerEndSettingsTabFragment {

            val fragment = PrayerEndSettingsTabFragment()
            val args = Bundle()
            args.putString(ARG_PRAYER_TYPE, prayerType.toString())
            fragment.arguments = args

            return fragment
        }
    }
}