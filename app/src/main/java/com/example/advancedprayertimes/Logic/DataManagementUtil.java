package com.example.advancedprayertimes.Logic;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;

import com.example.advancedprayertimes.Logic.Entities.API_Entities.CustomPlaceEntity;
import com.example.advancedprayertimes.Logic.Entities.DayPrayerTimesPackageEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerSettingsEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerTimeBeginningEndSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeMomentType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.databinding.ActivityTimeOverviewBinding;
import com.google.gson.Gson;

import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataManagementUtil
{
    private static Gson gson = AppEnvironment.BuildGSON("HH:mm");

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    public static String GetPrayerTimeEntityKeyForSharedPreference(EPrayerTimeType prayerTimeType)
    {
        return prayerTimeType.toString() + " data";
    }

    public static String GetTimeSettingsEntityKeyForSharedPreference(EPrayerTimeType prayerTimeType)
    {
        return prayerTimeType.toString() + " settings";
    }

    public static String GetSelectedPlaceKeyForSharedPreference()
    {
        return "place information";
    }

    public static void SaveLocalData(SharedPreferences sharedPref, ActivityTimeOverviewBinding binding)
    {
        SharedPreferences.Editor editor = sharedPref.edit();

        // SAVE LOCATION
        if(AppEnvironment.PlaceEntity != null)
        {
            editor.putString(GetSelectedPlaceKeyForSharedPreference(), gson.toJson(AppEnvironment.PlaceEntity));
        }

        // SAVE PRAYER TIME DATA
        for(PrayerTimeEntity prayerEntity : PrayerTimeEntity.Prayers)
        {
            editor.putString(GetPrayerTimeEntityKeyForSharedPreference(prayerEntity.getPrayerTimeType()), gson.toJson(prayerEntity));
        }

        // SAVE ASSOCIATED DATE STRING
        editor.putString("displayedTime", binding.displayedDateTextLabel.getText().toString());

        // SAVE PRAYER TIME SETTINGS
        for(Map.Entry<EPrayerTimeType, PrayerSettingsEntity> entry : AppEnvironment.prayerSettingsByPrayerType.entrySet())
        {
            String jsonString = gson.toJson(entry.getValue());
            editor.putString(GetTimeSettingsEntityKeyForSharedPreference(entry.getKey()), jsonString);
        }

        editor.apply();
    }

    public static void RetrieveLocalData(SharedPreferences sharedPref, ActivityTimeOverviewBinding binding)
    {
        if(sharedPref.contains(GetSelectedPlaceKeyForSharedPreference()))
        {
            AppEnvironment.PlaceEntity = gson.fromJson(sharedPref.getString(GetSelectedPlaceKeyForSharedPreference(), null), CustomPlaceEntity.class);
        }

        // RETRIEVE PRAYER TIME DATA
        for(int i = 0; i < PrayerTimeEntity.Prayers.size(); i++)
        {
            String key = GetPrayerTimeEntityKeyForSharedPreference(PrayerTimeEntity.Prayers.get(i).getPrayerTimeType());
            String storedValue = sharedPref.getString(key, null);

            if(storedValue != null)
            {
                PrayerTimeEntity retrievedPrayer = gson.fromJson(storedValue, PrayerTimeEntity.class);

                if(retrievedPrayer != null)
                {
                    PrayerTimeEntity.Prayers.set(i, retrievedPrayer);
                }
            }
        }

        // RETRIEVE ASSOCIATED DATE STRING
        binding.displayedDateTextLabel.setText(sharedPref.getString("displayedTime", "xx.xx.xxxx"));

        // RETRIEVE ASSOCIATED DATE STRING
        Gson gson = new Gson();

        for(EPrayerTimeType prayerTimeType : AppEnvironment.prayerSettingsByPrayerType.keySet())
        {
            String value = sharedPref.getString(GetTimeSettingsEntityKeyForSharedPreference(prayerTimeType), null);

            if(value != null)
            {
                PrayerSettingsEntity settings = gson.fromJson(value, PrayerSettingsEntity.class);
                AppEnvironment.prayerSettingsByPrayerType.put(prayerTimeType, settings);
            }
        }
    }

    public static Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, DayPrayerTimesPackageEntity> RetrieveDiyanetTimeData(
            Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> toBeCalculatedPrayerTimes,
            Address cityAddress)
            throws Exception
    {
        Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> diyanetPrayerTimeTypesHashMap =
                toBeCalculatedPrayerTimes.entrySet().stream()
                        .filter(x -> x.getValue().get_api() == ESupportedAPIs.Diyanet)
                        .collect(Collectors.toMap(x -> x.getKey(), y -> y.getValue()));

        // ADD ALL DIYANET TIME CALCULATIONS
        if(diyanetPrayerTimeTypesHashMap.size() > 0)
        {
            try
            {
                DayPrayerTimesPackageEntity diyanetTime = HttpAPIRequestUtil.RetrieveDiyanetTimes(cityAddress);

                if(diyanetTime != null)
                {
                    return diyanetPrayerTimeTypesHashMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, y -> diyanetTime));
                }
                else
                {
                    throw new Exception("Could not retrieve Diyanet prayer time data for an unknown reason!", null);
                }
            }
            catch(Exception e)
            {
                throw new Exception("An error has occured while trying to retrieve Diyanet prayer time data!", e);
            }
        }

        return new HashMap<>();
    }

    public static Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, DayPrayerTimesPackageEntity> RetrieveMuwaqqitTimeData(
            Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> toBeCalculatedPrayerTimes,
            Address cityAddress)
            throws Exception
    {
        Location targetLocation = new Location("");
        targetLocation.setLongitude(cityAddress.getLongitude());
        targetLocation.setLatitude(cityAddress.getLatitude());

        Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, DayPrayerTimesPackageEntity> muwaqqitTimesHashMap = new HashMap<>();

        Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> muwaqqitPrayerTimeTypesHashMap =
                toBeCalculatedPrayerTimes.entrySet().stream()
                        .filter(x -> x.getValue().get_api() == ESupportedAPIs.Muwaqqit)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap =
                muwaqqitPrayerTimeTypesHashMap.entrySet().stream()
                        .filter(x -> PrayerTimeBeginningEndSettingsEntity.DEGREE_TYPES.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> nonFajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap =
                muwaqqitPrayerTimeTypesHashMap.entrySet().stream()
                        .filter(x -> !PrayerTimeBeginningEndSettingsEntity.DEGREE_TYPES.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Double asrKarahaDegree = null;

        if(AppEnvironment.prayerSettingsByPrayerType.get(EPrayerTimeType.Asr).getSubPrayer1Settings() != null && AppEnvironment.prayerSettingsByPrayerType.get(EPrayerTimeType.Asr).getSubPrayer1Settings().isEnabled2())
        {
            asrKarahaDegree = AppEnvironment.prayerSettingsByPrayerType.get(EPrayerTimeType.Asr).getSubPrayer1Settings().getAsrKarahaDegree();
        }

        if(fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.size() > 0)
        {
            Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> fajrDegreeMuwaqqitTimesHashMap =
                    fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.entrySet().stream()
                            .filter(x -> PrayerTimeBeginningEndSettingsEntity.FAJR_DEGREE_TYPES.contains(x.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> ishaDegreeMuwaqqitTimesHashMap =
                    fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.entrySet().stream()
                            .filter(x -> PrayerTimeBeginningEndSettingsEntity.ISHA_DEGREE_TYPES.contains(x.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // MERGE CALCULATIONS FOR MERGABLE TIMES
            while(fajrDegreeMuwaqqitTimesHashMap.size() > 0 && ishaDegreeMuwaqqitTimesHashMap.size() > 0)
            {
                Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> fajrDegreeEntry = fajrDegreeMuwaqqitTimesHashMap.entrySet().stream().findFirst().get();
                Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> ishaDegreeEntry = ishaDegreeMuwaqqitTimesHashMap.entrySet().stream().findFirst().get();

                PrayerTimeBeginningEndSettingsEntity fajrDegreeSettingsEntity = fajrDegreeEntry.getValue();
                PrayerTimeBeginningEndSettingsEntity ishaDegreeSettingsEntity = ishaDegreeEntry.getValue();

                Double fajrDegree = fajrDegreeSettingsEntity.getFajrCalculationDegree();
                Double ishaDegree = ishaDegreeSettingsEntity.getIshaCalculationDegree();

                DayPrayerTimesPackageEntity degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, fajrDegree, ishaDegree, asrKarahaDegree);

                if(degreeMuwaqqitTimeEntity == null)
                {
                    throw new Exception("Could not retrieve Fajr/Isha Muwaqqit prayer time data for an unknown reason!", null);
                }

                muwaqqitTimesHashMap.put(fajrDegreeEntry.getKey(), degreeMuwaqqitTimeEntity);
                muwaqqitTimesHashMap.put(ishaDegreeEntry.getKey(), degreeMuwaqqitTimeEntity);

                // remove handled entries from the lists
                fajrDegreeMuwaqqitTimesHashMap.remove(fajrDegreeEntry.getKey());
                ishaDegreeMuwaqqitTimesHashMap.remove(ishaDegreeEntry.getKey());

                fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.remove(fajrDegreeEntry.getKey());
                fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.remove(ishaDegreeEntry.getKey());
            }

            // ADD REMAINING CALCULATIONS FOR NON MERGABLE DEGREE TIMES
            for(Map.Entry<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> entry : fajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.entrySet())
            {
                PrayerTimeBeginningEndSettingsEntity settingsEntity = entry.getValue();

                Double fajrDegree = settingsEntity.getFajrCalculationDegree();
                Double ishaDegree = settingsEntity.getIshaCalculationDegree();

                DayPrayerTimesPackageEntity degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, fajrDegree, ishaDegree, asrKarahaDegree);

                if(degreeMuwaqqitTimeEntity == null)
                {
                    throw new Exception("Could not retrieve Non-Fajr/Isha Muwaqqit prayer time data for an unknown reason!", null);
                }

                muwaqqitTimesHashMap.put(entry.getKey(), degreeMuwaqqitTimeEntity);
            }
        }

        // ADD CALCULATIONS FOR NON DEGREE TIMES
        if(nonFajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.size() > 0)
        {
            DayPrayerTimesPackageEntity nonDegreeMuwaqqitTimeEntity;

            // any other muwaqqit request will suffice
            if(muwaqqitTimesHashMap.values().stream().findFirst().isPresent())
            {
                nonDegreeMuwaqqitTimeEntity = muwaqqitTimesHashMap.values().stream().findFirst().get();
            }
            else
            {
                nonDegreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, null, null, asrKarahaDegree);
            }

            if(nonDegreeMuwaqqitTimeEntity != null)
            {
                for(Map.Entry<EPrayerTimeType, EPrayerTimeMomentType> prayerTimeType : nonFajrIshaDegreeSettingsMuwaqqitPrayerTimeTypesHashMap.keySet())
                {
                    muwaqqitTimesHashMap.put(prayerTimeType, nonDegreeMuwaqqitTimeEntity);
                }
            }
        }

        if(asrKarahaDegree != null)
        {
            DayPrayerTimesPackageEntity asrKarahaTimePackage = null;

            // any other muwaqqit request will suffice
            if(muwaqqitTimesHashMap.values().stream().findFirst().isPresent())
            {
                asrKarahaTimePackage = muwaqqitTimesHashMap.values().stream().findFirst().get();
            }
            else
            {
                asrKarahaTimePackage = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, null, null, asrKarahaDegree);
            }

            if(asrKarahaTimePackage != null)
            {
                muwaqqitTimesHashMap.put(new AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.SubTimeOne), asrKarahaTimePackage);
                muwaqqitTimesHashMap.put(new AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.SubTimeTwo), asrKarahaTimePackage);
            }
        }

        return muwaqqitTimesHashMap;
    }
}
