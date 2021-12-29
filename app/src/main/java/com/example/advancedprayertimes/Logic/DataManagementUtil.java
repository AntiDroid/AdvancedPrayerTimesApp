package com.example.advancedprayertimes.Logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;

import com.example.advancedprayertimes.Logic.Entities.CustomPlaceEntity;
import com.example.advancedprayertimes.Logic.Entities.DayPrayerTimesEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.databinding.TimeOverviewActivityBinding;
import com.google.android.libraries.places.api.model.Place;
import com.google.gson.Gson;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataManagementUtil
{
    public static void SaveLocalData(SharedPreferences sharedPref, TimeOverviewActivityBinding binding)
    {
        SharedPreferences.Editor editor = sharedPref.edit();
        sharedPref.edit().clear().commit();

        // SAVE PRAYER TIME DATA
        for(PrayerEntity prayerEntity : PrayerEntity.prayers)
        {
            if(prayerEntity.getBeginningTime() != null)
            {
                editor.putLong(prayerEntity.getTitle() + " beginning value", prayerEntity.getBeginningTime().getTime());
            }

            if(prayerEntity.getEndTime() != null)
            {
                editor.putLong(prayerEntity.getTitle() + " end value", prayerEntity.getEndTime().getTime());
            }
        }

        // SAVE ASSOCIATED DATE STRING
        editor.putString("displayedTime", binding.displayedDateTextLabel.getText().toString());

        // SAVE PRAYER TIME SETTINGS
        Gson gson = AppEnvironment.BuildGSON("HH:mm");

        for(Map.Entry<EPrayerTimeType, PrayerTimeSettingsEntity> entry : AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.entrySet())
        {
            String jsonString = gson.toJson(entry.getValue());
            editor.putString(entry.getKey().toString() + "settings", jsonString);
        }

        if(AppEnvironment.place != null)
        {
            String jsonPlace = gson.toJson(AppEnvironment.place);
            editor.putString("place", jsonPlace);
        }

        editor.apply();
    }

    public static void RetrieveLocalData(SharedPreferences sharedPref, TimeOverviewActivityBinding binding, Set<EPrayerTimeType> prayerTimeTypes)
    {
        // RETRIEVE PRAYER TIME DATA
        for(PrayerEntity prayerEntity : PrayerEntity.prayers)
        {
            Time beginningTime = null;
            Time endTime = null;

            if(sharedPref.contains(prayerEntity.getTitle() + " beginning value"))
            {
                beginningTime = new Time(sharedPref.getLong(prayerEntity.getTitle() + " beginning value", 0));
            }

            if(sharedPref.contains(prayerEntity.getTitle() + " end value"))
            {
                endTime = new Time(sharedPref.getLong(prayerEntity.getTitle() + " end value", 0));
            }

            prayerEntity.setBeginningTime(beginningTime);
            prayerEntity.setEndTime(endTime);
        }

        // RETRIEVE ASSOCIATED DATE STRING
        binding.displayedDateTextLabel.setText(sharedPref.getString("displayedTime", "xx.xx.xxxx"));

        // RETRIEVE ASSOCIATED DATE STRING
        Gson gson = new Gson();

        for(EPrayerTimeType prayerTimeType : prayerTimeTypes)
        {
            String enumName = prayerTimeType.toString();
            String value = sharedPref.getString(enumName + "settings", null);

            if(value != null)
            {
                try
                {
                    PrayerTimeSettingsEntity settings = gson.fromJson(value, PrayerTimeSettingsEntity.class);

                    AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.put(prayerTimeType, settings);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        String placesValue = sharedPref.getString("place", null);

        if(sharedPref.contains("place") && placesValue != null && AppEnvironment.place == null)
        {
            try
            {
                AppEnvironment.place = gson.fromJson(placesValue, CustomPlaceEntity.class);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static Map<EPrayerTimeType, DayPrayerTimesEntity> RetrieveDiyanetTimes(Context context, Map<EPrayerTimeType, PrayerTimeSettingsEntity> toBeCalculatedPrayerTimes, Address cityAddress) throws Exception
    {
        Map<EPrayerTimeType, PrayerTimeSettingsEntity> diyanetPrayerTimeTypesHashMap =
                toBeCalculatedPrayerTimes.entrySet().stream()
                        .filter(x -> x.getValue().get_api() == ESupportedAPIs.Diyanet)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // ADD ALL DIYANET TIME CALCULATIONS
        if(diyanetPrayerTimeTypesHashMap.size() > 0)
        {
            DayPrayerTimesEntity diyanetTime = HttpAPIRequestUtil.RetrieveDiyanetTimes(cityAddress);

            if(diyanetTime != null)
            {
                return diyanetPrayerTimeTypesHashMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                x -> x.getKey(),
                                y -> diyanetTime
                        ));
            }
        }

        return new HashMap<>();
    }

    public static Map<EPrayerTimeType, DayPrayerTimesEntity> RetrieveMuwaqqitTimes(Map<EPrayerTimeType, PrayerTimeSettingsEntity> toBeCalculatedPrayerTimes, Location targetLocation) throws Exception
    {
        HashMap<EPrayerTimeType, DayPrayerTimesEntity> muwaqqitTimesHashMap = new HashMap<>();

        Map<EPrayerTimeType, PrayerTimeSettingsEntity> muwaqqitPrayerTimeTypesHashMap =
                toBeCalculatedPrayerTimes.entrySet().stream()
                        .filter(x -> x.getValue().get_api() == ESupportedAPIs.Muwaqqit)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<EPrayerTimeType, PrayerTimeSettingsEntity> degreeSettingsMuwaqqitPrayerTimeTypesHashMap =
                muwaqqitPrayerTimeTypesHashMap.entrySet().stream()
                        .filter(x -> PrayerTimeSettingsEntity.DEGREE_TYPES.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<EPrayerTimeType, PrayerTimeSettingsEntity> nonDegreeSettingsMuwaqqitPrayerTimeTypesHashMap =
                muwaqqitPrayerTimeTypesHashMap.entrySet().stream()
                        .filter(x -> !PrayerTimeSettingsEntity.DEGREE_TYPES.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if(degreeSettingsMuwaqqitPrayerTimeTypesHashMap.size() > 0)
        {
            Map<EPrayerTimeType, PrayerTimeSettingsEntity> fajrDegreeMuwaqqitTimesHashMap =
                    degreeSettingsMuwaqqitPrayerTimeTypesHashMap.entrySet().stream()
                            .filter(x -> PrayerTimeSettingsEntity.FAJR_DEGREE_TYPES.contains(x.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<EPrayerTimeType, PrayerTimeSettingsEntity> ishaDegreeMuwaqqitTimesHashMap =
                    degreeSettingsMuwaqqitPrayerTimeTypesHashMap.entrySet().stream()
                            .filter(x -> PrayerTimeSettingsEntity.ISHA_DEGREE_TYPES.contains(x.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // MERGE CALCULATIONS FOR MERGABLE TIMES
            while(fajrDegreeMuwaqqitTimesHashMap.size() > 0 && ishaDegreeMuwaqqitTimesHashMap.size() > 0)
            {
                Map.Entry<EPrayerTimeType, PrayerTimeSettingsEntity> fajrDegreeEntry = fajrDegreeMuwaqqitTimesHashMap.entrySet().stream().findFirst().get();
                Map.Entry<EPrayerTimeType, PrayerTimeSettingsEntity> ishaDegreeEntry = ishaDegreeMuwaqqitTimesHashMap.entrySet().stream().findFirst().get();

                PrayerTimeSettingsEntity fajrDegreeSettingsEntity = fajrDegreeEntry.getValue();
                PrayerTimeSettingsEntity ishaDegreeSettingsEntity = ishaDegreeEntry.getValue();

                Double fajrDegree = fajrDegreeSettingsEntity.getFajrCalculationDegree();
                Double ishaDegree = ishaDegreeSettingsEntity.getIshaCalculationDegree();

                DayPrayerTimesEntity degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, fajrDegree, ishaDegree);

                if(degreeMuwaqqitTimeEntity == null)
                {
                    throw new Exception();
                }

                muwaqqitTimesHashMap.put(fajrDegreeEntry.getKey(), degreeMuwaqqitTimeEntity);
                muwaqqitTimesHashMap.put(ishaDegreeEntry.getKey(), degreeMuwaqqitTimeEntity);

                // remove handled entries from the lists
                fajrDegreeMuwaqqitTimesHashMap.remove(fajrDegreeEntry.getKey());
                ishaDegreeMuwaqqitTimesHashMap.remove(ishaDegreeEntry.getKey());

                degreeSettingsMuwaqqitPrayerTimeTypesHashMap.remove(fajrDegreeEntry.getKey());
                degreeSettingsMuwaqqitPrayerTimeTypesHashMap.remove(ishaDegreeEntry.getKey());
            }

            // ADD REMAINING CALCULATIONS FOR NON MERGABLE DEGREE TIMES
            for(Map.Entry<EPrayerTimeType, PrayerTimeSettingsEntity> entry : degreeSettingsMuwaqqitPrayerTimeTypesHashMap.entrySet())
            {
                EPrayerTimeType prayerTimeType = entry.getKey();
                PrayerTimeSettingsEntity settingsEntity = entry.getValue();

                Double fajrDegree = settingsEntity.getFajrCalculationDegree();
                Double ishaDegree = settingsEntity.getIshaCalculationDegree();

                DayPrayerTimesEntity degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, fajrDegree, ishaDegree);

                if(degreeMuwaqqitTimeEntity == null)
                {
                    throw new Exception();
                }

                muwaqqitTimesHashMap.put(prayerTimeType, degreeMuwaqqitTimeEntity);
            }
        }

        // ADD CALCULATIONS FOR NON DEGREE TIMES
        if(nonDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.size() > 0)
        {
            DayPrayerTimesEntity nonDegreeMuwaqqitTimeEntity;

            // any other muwaqqit request will suffice
            if(muwaqqitTimesHashMap.values().stream().findFirst().isPresent())
            {
                nonDegreeMuwaqqitTimeEntity = muwaqqitTimesHashMap.values().stream().findFirst().get();
            }
            else
            {
                nonDegreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, null, null);
            }

            for(EPrayerTimeType prayerTimeType : nonDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.keySet())
            {
                muwaqqitTimesHashMap.put(prayerTimeType, nonDegreeMuwaqqitTimeEntity);
            }
        }

        return muwaqqitTimesHashMap;
    }
}
