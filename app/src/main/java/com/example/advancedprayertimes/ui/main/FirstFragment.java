package com.example.advancedprayertimes.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.R;
import com.example.advancedprayertimes.TimeSettingsActivity;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstFragment extends Fragment
{
    SettingsFragment settingsFragment = new SettingsFragment();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FirstFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment firstFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FirstFragment newInstance(String param1, String param2)
    {
        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
        {
            this.getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, settingsFragment)
                    .commit();
        }

        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EPrayerTimeType prayerTimeType = EPrayerTimeType.AsrEnd;

            ListPreference apiSelectionListPreference = this.findPreference("apiSelection");
            ListPreference minuteAdjustmentListPreference = this.findPreference("minuteAdjustmentSelection");
            ListPreference fajrDegreesListPreference = this.findPreference("fajrCalculationDegree");
            ListPreference ishaDegreesListPreference = this.findPreference("ishaCalculationDegree");

            PreferenceCategory apiSettingsPreferenceCategory = this.findPreference("apiSettingsPreferenceCategory");

            if(     false && apiSelectionListPreference != null
                    &&
                    minuteAdjustmentListPreference != null
                    &&
                    fajrDegreesListPreference != null
                    &&
                    ishaDegreesListPreference != null
                    &&
                    apiSettingsPreferenceCategory != null)
            {
                // SET API SELECTION VALUES

                String[] apiNamesArray = Stream.of(ESupportedAPIs.values()).map(ESupportedAPIs::name).toArray(String[]::new);

                apiSelectionListPreference.setEntries(apiNamesArray);
                apiSelectionListPreference.setEntryValues(apiNamesArray);
                apiSelectionListPreference.setValue(ESupportedAPIs.Undefined.toString());

                // SET MINUTE ADJUSTMENT VALUES

                ArrayList<String> minuteAdjustmentValuesArrayList = new ArrayList<>();

                for(int i = -15; i < 16; i++)
                {
                    minuteAdjustmentValuesArrayList.add("" + i);
                }

                minuteAdjustmentListPreference.setEntries(minuteAdjustmentValuesArrayList.toArray(new String[0]));
                minuteAdjustmentListPreference.setEntryValues(minuteAdjustmentValuesArrayList.toArray(new String[0]));
                minuteAdjustmentListPreference.setValue("0");

                // SET DEGREE VALUES

                ArrayList<String> degreeValuesArrayList = new ArrayList<>();

                for(double i = -12.0; i > -21.0; i -= 0.5)
                {
                    degreeValuesArrayList.add("" + i);
                }

                String[] degreeValuesArray = degreeValuesArrayList.toArray(new String[0]);

                fajrDegreesListPreference.setEntries(degreeValuesArray);
                fajrDegreesListPreference.setEntryValues(degreeValuesArray);
                fajrDegreesListPreference.setValue("-12.0");

                ishaDegreesListPreference.setEntries(degreeValuesArray);
                ishaDegreesListPreference.setEntryValues(degreeValuesArray);
                ishaDegreesListPreference.setValue("-12.0");

                fajrDegreesListPreference.setVisible(PrayerTimeSettingsEntity.FAJR_DEGREE_TYPES.contains(prayerTimeType));
                ishaDegreesListPreference.setVisible(PrayerTimeSettingsEntity.ISHA_DEGREE_TYPES.contains(prayerTimeType));
                apiSettingsPreferenceCategory.setVisible(fajrDegreesListPreference.isVisible() || ishaDegreesListPreference.isVisible());

                // SET CURRENT CONFIGURATION FOR PRAYERTIMETYPE

                if(AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.containsKey(prayerTimeType))
                {
                    PrayerTimeSettingsEntity settings = AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.get(prayerTimeType);

                    if(settings != null)
                    {
                        apiSelectionListPreference.setValue(settings.get_api().toString());
                        minuteAdjustmentListPreference.setValue(String.valueOf(settings.get_minuteAdjustment()));

                        if(fajrDegreesListPreference.isVisible())
                        {
                            fajrDegreesListPreference.setValue(settings.getFajrCalculationDegree().toString());
                        }

                        if(ishaDegreesListPreference.isVisible())
                        {
                            ishaDegreesListPreference.setValue(settings.getIshaCalculationDegree().toString());
                        }
                    }
                    // TODO: Set default configuration values for new configuration
                }
            }
        }
    }
}