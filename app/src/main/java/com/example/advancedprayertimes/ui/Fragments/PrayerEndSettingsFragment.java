package com.example.advancedprayertimes.UI.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerPointInTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PrayerEndSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrayerEndSettingsFragment extends Fragment
{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PRAYER_END_POINT_IN_TIME_TYPE = "prayerEndPointInTimeType";

    private EPrayerPointInTimeType prayerEndPointInTimeType;

    public PrayerEndSettingsFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PrayerEndSettingsFragment.
     */
    public static PrayerEndSettingsFragment newInstance(EPrayerPointInTimeType pointInTimeType)
    {
        PrayerEndSettingsFragment fragment = new PrayerEndSettingsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PRAYER_END_POINT_IN_TIME_TYPE, pointInTimeType.toString());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.prayer_end_settings_fragment, container, false);
    }

    SettingsFragment settingsFragment;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null)
        {
            prayerEndPointInTimeType = EPrayerPointInTimeType.valueOf(getArguments().getString(ARG_PRAYER_END_POINT_IN_TIME_TYPE));
        }

        settingsFragment = new SettingsFragment(prayerEndPointInTimeType);

        if (savedInstanceState == null)
        {
            this.getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsEnd, settingsFragment)
                    .commit();
        }
    }

    @Override
    public void onStop()
    {
        ListPreference apiSelectionListPreference = this.settingsFragment.findPreference("apiSelection");
        ListPreference minuteAdjustmentListPreference = this.settingsFragment.findPreference("minuteAdjustmentSelection");
        ListPreference fajrDegreesListPreference = this.settingsFragment.findPreference("fajrCalculationDegree");
        ListPreference ishaDegreesListPreference = this.settingsFragment.findPreference("ishaCalculationDegree");

        ESupportedAPIs api = ESupportedAPIs.Undefined;
        int minuteAdjustment = 0;

        if(apiSelectionListPreference != null)
        {
            api = ESupportedAPIs.valueOf(apiSelectionListPreference.getValue());
        }

        if(minuteAdjustmentListPreference != null)
        {
            minuteAdjustment = Integer.parseInt(minuteAdjustmentListPreference.getValue());
        }

        Double fajrCalculationDegrees = null;
        Double ishaCalculationDegrees = null;

        if(prayerEndPointInTimeType == EPrayerPointInTimeType.IshaEnd)
        {
            fajrCalculationDegrees = Double.parseDouble(fajrDegreesListPreference.getValue());
        }

        if(prayerEndPointInTimeType == EPrayerPointInTimeType.MaghribEnd)
        {
            ishaCalculationDegrees = Double.parseDouble(ishaDegreesListPreference.getValue());
        }

        AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.put(prayerEndPointInTimeType, new PrayerTimeSettingsEntity(api, minuteAdjustment, fajrCalculationDegrees, ishaCalculationDegrees));

        super.onStop();
    }
}