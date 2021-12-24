package com.example.advancedprayertimes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.DayPrayerTimeEntity;
import com.example.advancedprayertimes.Logic.DayPrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.Logic.HttpAPIRequestUtil;
import com.example.advancedprayertimes.databinding.OverviewActivityBinding;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OverviewActivity extends AppCompatActivity
{
    private OverviewActivityBinding binding = null;

    HashMap<EPrayerTimeType, TextView> prayerTimeTypeWithAssociatedTextView = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = OverviewActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurePrayerTimeTextViews();

        binding.progressBar.setVisibility(View.GONE);

        binding.showStuffButton.setOnClickListener(view ->
        {
            binding.statusTextLabel.setText("");

            Thread asyncRetrievePrayerTimesThread = new Thread(this::retrievePrayerTimes);

            binding.showStuffButton.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            asyncRetrievePrayerTimesThread.start();
        });
    }

    @Override
    protected void onPause()
    {
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        for(Map.Entry<EPrayerTimeType, TextView> entry : this.prayerTimeTypeWithAssociatedTextView.entrySet())
        {
            EPrayerTimeType prayerTimeType = entry.getKey();
            TextView prayerTimeTextLabel = entry.getValue();

            editor.putString(prayerTimeType.toString() + "value", prayerTimeTextLabel.getText().toString());
        }

        editor.putString("displayedTime", binding.displayedDateTextLabel.getText().toString());

        Gson gson = new Gson();

        for(Map.Entry<EPrayerTimeType, DayPrayerTimeSettingsEntity> entry : AppEnvironment.DayPrayerTimeSettings.entrySet())
        {
            String jsonString = gson.toJson(entry.getValue());
            editor.putString(entry.getKey().toString() + "settings", jsonString);
        }

        editor.apply();

        super.onPause();
    }

    @Override
    protected void onResume()
    {
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);

        for(Map.Entry<EPrayerTimeType, TextView> entry : this.prayerTimeTypeWithAssociatedTextView.entrySet())
        {
            EPrayerTimeType prayerTimeType = entry.getKey();
            TextView prayerTimeTextLabel = entry.getValue();

            prayerTimeTextLabel.setText(sharedPref.getString(prayerTimeType.toString() + "value", "xx:xx"));
        }

        binding.displayedDateTextLabel.setText(sharedPref.getString("displayedTime", "xx.xx.xxxx"));

        // locally save settings as JSON string objects
        Gson gson = new Gson();
        String[] enumStrings = Stream.of(EPrayerTimeType.values()).map(EPrayerTimeType::name).toArray(String[]::new);

        for(String enumName : enumStrings)
        {
            String value = sharedPref.getString(enumName + "settings", null);

            if(value != null)
            {
                try
                {
                    EPrayerTimeType prayerTimeType = EPrayerTimeType.valueOf(enumName);
                    DayPrayerTimeSettingsEntity settings = gson.fromJson(value, DayPrayerTimeSettingsEntity.class);

                    AppEnvironment.DayPrayerTimeSettings.put(prayerTimeType, settings);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        super.onResume();
    }

    private void configurePrayerTimeTextViews()
    {
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.FajrBeginning, binding.fajrTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.FajrEnd, binding.fajrTimeEndTextLabel);

        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.DhuhrBeginning, binding.dhuhrTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.DhuhrEnd, binding.dhuhrTimeEndTextLabel);

        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.AsrBeginning, binding.asrTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.AsrEnd, binding.asrTimeEndTextLabel);

        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.MaghribBeginning, binding.maghribTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.MaghribEnd, binding.maghribTimeEndTextLabel);

        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.IshaBeginning, binding.ishaTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.IshaEnd, binding.ishaTimeEndTextLabel);

        for(Map.Entry<EPrayerTimeType, TextView> entry : this.prayerTimeTypeWithAssociatedTextView.entrySet())
        {
            EPrayerTimeType prayerTimeType = entry.getKey();
            TextView prayerTimeTextLabel = entry.getValue();

            prayerTimeTextLabel.setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(prayerTimeType));
        }
    }

    public static String INTENT_EXTRA = "prayerTime";

    private void openSettingsForSpecificPrayerTimeType(EPrayerTimeType prayerTimeType)
    {
        try
        {
            Intent myIntent = new Intent(OverviewActivity.this, PrayerTimeSettingsActivity.class);
            myIntent.putExtra(INTENT_EXTRA, prayerTimeType); //Optional parameters
            OverviewActivity.this.startActivity(myIntent);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    Map<EPrayerTimeType, DayPrayerTimeEntity> muwaqqitTimesHashMap = new HashMap<>();
    Map<EPrayerTimeType, DayPrayerTimeEntity> diyanetTimesHashMap = new HashMap<>();

    public void retrievePrayerTimes()
    {
        try
        {
            Location targetLocation = HttpAPIRequestUtil.RetrieveLocation(this);

            if(targetLocation == null)
            {
                new Handler(Looper.getMainLooper()).post(() ->
                        new AlertDialog.Builder(this)
                                .setTitle("LOCATION NOT AVAILABLE")
                                .setMessage("Location could not be retrieved!")
                                .show());

                binding.showStuffButton.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> toBeCalculatedPrayerTimes = AppEnvironment.DayPrayerTimeSettings;

            retrieveDiyanetTimes(toBeCalculatedPrayerTimes, targetLocation);
            retrieveMuwaqqitTimes(toBeCalculatedPrayerTimes, targetLocation);

            new Handler(Looper.getMainLooper()).post(() ->
            {
                applyTimeSettingsToOverview();
                binding.showStuffButton.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            });
        }
        catch (Exception e)
        {
            new Handler(Looper.getMainLooper()).post(() ->
            {
                binding.statusTextLabel.setText("Error!");
                binding.showStuffButton.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            });
            e.printStackTrace();
        }
    }

    private void retrieveDiyanetTimes(Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> toBeCalculatedPrayerTimes, Location targetLocation) throws Exception
    {
        // reset values
        diyanetTimesHashMap = new HashMap<>();

        Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> diyanetTimes =
                toBeCalculatedPrayerTimes.entrySet().stream()
                        .filter(x -> x.getValue().get_api() == ESupportedAPIs.Diyanet)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // ADD ALL DIYANET TIME CALCULATIONS
        if(diyanetTimes.size() > 0)
        {
            DayPrayerTimeEntity diyanetTime = HttpAPIRequestUtil.RetrieveDiyanetTimes(this, targetLocation);

            if(diyanetTime != null)
            {
                diyanetTimesHashMap = diyanetTimes.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, y -> diyanetTime));
            }
        }
    }

    private void retrieveMuwaqqitTimes(Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> toBeCalculatedPrayerTimes, Location targetLocation) throws Exception
    {
        // reset values
        muwaqqitTimesHashMap = new HashMap<>();

        Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> muwaqqitTimesHashMap =
                toBeCalculatedPrayerTimes.entrySet().stream()
                        .filter(x -> x.getValue().get_api() == ESupportedAPIs.Muwaqqit)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> degreeMuwaqqitTimesHashMap =
                muwaqqitTimesHashMap.entrySet().stream()
                        .filter(x -> DayPrayerTimeSettingsEntity.DEGREE_TYPES.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> nonDegreeMuwaqqitTimesHashMap =
                muwaqqitTimesHashMap.entrySet().stream()
                        .filter(x -> !DayPrayerTimeSettingsEntity.DEGREE_TYPES.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if(degreeMuwaqqitTimesHashMap.size() > 0)
        {
            Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> fajrDegreeMuwaqqitTimesHashMap =
                    degreeMuwaqqitTimesHashMap.entrySet().stream()
                            .filter(x -> DayPrayerTimeSettingsEntity.FAJR_DEGREE_TYPES.contains(x.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<EPrayerTimeType, DayPrayerTimeSettingsEntity> ishaDegreeMuwaqqitTimesHashMap =
                    degreeMuwaqqitTimesHashMap.entrySet().stream()
                            .filter(x -> DayPrayerTimeSettingsEntity.ISHA_DEGREE_TYPES.contains(x.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // MERGE CALCULATIONS FOR MERGABLE TIMES
            while(fajrDegreeMuwaqqitTimesHashMap.size() > 0 && ishaDegreeMuwaqqitTimesHashMap.size() > 0)
            {
                Map.Entry<EPrayerTimeType, DayPrayerTimeSettingsEntity> fajrDegreeEntry = fajrDegreeMuwaqqitTimesHashMap.entrySet().stream().findFirst().get();
                Map.Entry<EPrayerTimeType, DayPrayerTimeSettingsEntity> ishaDegreeEntry = ishaDegreeMuwaqqitTimesHashMap.entrySet().stream().findFirst().get();

                DayPrayerTimeSettingsEntity fajrDegreeSettingsEntity = fajrDegreeEntry.getValue();
                DayPrayerTimeSettingsEntity ishaDegreeSettingsEntity = ishaDegreeEntry.getValue();

                Double fajrDegree = fajrDegreeSettingsEntity.getFajrCalculationDegree();
                Double ishaDegree = ishaDegreeSettingsEntity.getIshaCalculationDegree();

                DayPrayerTimeEntity degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, fajrDegree, ishaDegree);

                if(degreeMuwaqqitTimeEntity == null)
                {
                    throw new Exception();
                }

                this.muwaqqitTimesHashMap.put(fajrDegreeEntry.getKey(), degreeMuwaqqitTimeEntity);
                this.muwaqqitTimesHashMap.put(ishaDegreeEntry.getKey(), degreeMuwaqqitTimeEntity);

                // remove handled entries from the lists
                fajrDegreeMuwaqqitTimesHashMap.remove(fajrDegreeEntry.getKey());
                ishaDegreeMuwaqqitTimesHashMap.remove(ishaDegreeEntry.getKey());

                degreeMuwaqqitTimesHashMap.remove(fajrDegreeEntry.getKey());
                degreeMuwaqqitTimesHashMap.remove(ishaDegreeEntry.getKey());
            }

            // ADD REMAINING CALCULATIONS FOR NON MERGABLE DEGREE TIMES
            for(Map.Entry<EPrayerTimeType, DayPrayerTimeSettingsEntity> entry : degreeMuwaqqitTimesHashMap.entrySet())
            {
                EPrayerTimeType prayerTimeType = entry.getKey();
                DayPrayerTimeSettingsEntity settingsEntity = entry.getValue();

                Double fajrDegree = settingsEntity.getFajrCalculationDegree();
                Double ishaDegree = settingsEntity.getIshaCalculationDegree();

                DayPrayerTimeEntity degreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, fajrDegree, ishaDegree);

                if(degreeMuwaqqitTimeEntity == null)
                {
                    throw new Exception();
                }

                this.muwaqqitTimesHashMap.put(prayerTimeType, degreeMuwaqqitTimeEntity);
            }
        }

        // ADD CALCULATIONS FOR NON DEGREE TIMES
        if(nonDegreeMuwaqqitTimesHashMap.size() > 0)
        {
            DayPrayerTimeEntity nonDegreeMuwaqqitTimeEntity = null;

            // any other muwaqqit request will suffice
            if(this.muwaqqitTimesHashMap.values().stream().findFirst().isPresent())
            {
                nonDegreeMuwaqqitTimeEntity = this.muwaqqitTimesHashMap.values().stream().findFirst().get();
            }
            else
            {
                nonDegreeMuwaqqitTimeEntity = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation, null, null);
            }

            for(EPrayerTimeType prayerTimeType : nonDegreeMuwaqqitTimesHashMap.keySet())
            {
                this.muwaqqitTimesHashMap.put(prayerTimeType, nonDegreeMuwaqqitTimeEntity);
            }
        }
    }

    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private String getCorrectText(EPrayerTimeType prayerTimeType)
    {
        if(AppEnvironment.DayPrayerTimeSettings.containsKey(prayerTimeType))
        {
            DayPrayerTimeSettingsEntity settings = AppEnvironment.DayPrayerTimeSettings.get(prayerTimeType);

            Date targetDate = null;

            if(settings != null)
            {
                if (settings.get_api() == ESupportedAPIs.Muwaqqit
                        && muwaqqitTimesHashMap.containsKey(prayerTimeType)
                        && muwaqqitTimesHashMap.get(prayerTimeType) != null)
                {
                    targetDate = muwaqqitTimesHashMap.get(prayerTimeType).GetTimeByType(prayerTimeType);
                }
                else if (settings.get_api() == ESupportedAPIs.Diyanet
                        && diyanetTimesHashMap.containsKey(prayerTimeType)
                        && diyanetTimesHashMap.get(prayerTimeType) != null)
                {
                    targetDate = diyanetTimesHashMap.get(prayerTimeType).GetTimeByType(prayerTimeType);
                }
            }

            if(targetDate != null)
            {
                // minute adjustment
                targetDate = new Date(targetDate.getTime() + ((long)settings.get_minuteAdjustment() * 60 * 1000));

                return dateFormat.format(targetDate);
            }
        }

        return "xx:xx";
    }

    private void applyTimeSettingsToOverview()
    {
        try
        {
            binding.displayedDateTextLabel.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now()));

            for(Map.Entry<EPrayerTimeType, TextView> entry : this.prayerTimeTypeWithAssociatedTextView.entrySet())
            {
                EPrayerTimeType prayerTimeType = entry.getKey();
                TextView prayerTimeTextLabel = entry.getValue();

                prayerTimeTextLabel.setText(getCorrectText(prayerTimeType));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}