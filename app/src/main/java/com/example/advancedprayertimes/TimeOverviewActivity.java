package com.example.advancedprayertimes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.Entities.DayPrayerTimesEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.Logic.HttpAPIRequestUtil;
import com.example.advancedprayertimes.databinding.TimeOverviewActivityBinding;
import com.google.gson.Gson;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimeOverviewActivity extends AppCompatActivity
{
    private final int backgroundColor = Color.argb(255,45,54,71);
    private TimeOverviewActivityBinding binding = null;



    HashMap<EPrayerTimeType, TextView> prayerTimeTypeWithAssociatedTextView = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = TimeOverviewActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //binding.getRoot().setBackgroundColor(backgroundColor);

        configurePrayerTimeTextViews();



        binding.showStuffButton.setOnClickListener(view ->
        {
            binding.statusTextLabel.setText("");

            Thread asyncRetrievePrayerTimesThread = new Thread(this::retrievePrayerTimes);

            binding.showStuffButton.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            asyncRetrievePrayerTimesThread.start();
        });

        binding.timeInfoTitleTextLabel.setText("API: \nMinute adjustment: \nFajr degree: \nIsha degree: ");

        binding.drawGraphicsButton.setOnClickListener(view ->
        {
            binding.testCustomView.invalidate();
        });
    }

    @Override
    protected void onPause()
    {
        saveLocalData();

        super.onPause();
    }

    private void saveLocalData()
    {
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

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

        for(Map.Entry<EPrayerTimeType, PrayerTimeSettingsEntity> entry : AppEnvironment.DayPrayerTimeSettings.entrySet())
        {
            String jsonString = gson.toJson(entry.getValue());
            editor.putString(entry.getKey().toString() + "settings", jsonString);
        }

        editor.apply();
    }

    @Override
    protected void onResume()
    {
        retrieveLocalData();

        super.onResume();
    }

    private void retrieveLocalData()
    {
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);

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

        for(EPrayerTimeType prayerTimeType : this.prayerTimeTypeWithAssociatedTextView.keySet())
        {
            String enumName = prayerTimeType.toString();
            String value = sharedPref.getString(enumName + "settings", null);

            if(value != null)
            {
                try
                {
                    PrayerTimeSettingsEntity settings = gson.fromJson(value, PrayerTimeSettingsEntity.class);

                    AppEnvironment.DayPrayerTimeSettings.put(prayerTimeType, settings);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        this.applyTimeSettingsToOverview();

        // TODO: Commit und dergleichen hinterfragen
        sharedPref.edit().clear();
        sharedPref.edit().commit();
        sharedPref.edit().apply();
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
            prayerTimeTextLabel.setOnTouchListener((View view, MotionEvent event) -> doTouchStuff(view, event));
        }
    }

    Map<View, Long> touchPointStuff = new HashMap<>();

    private boolean doTouchStuff(View v, MotionEvent event)
    {
        boolean useUpEvent = false;

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                touchPointStuff.put(v, System.currentTimeMillis());
                break;

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:

                if(touchPointStuff.containsKey(v) && touchPointStuff.get(v) != null)
                {
                    long milliSecondDifference = System.currentTimeMillis() - touchPointStuff.get(v);

                    if(milliSecondDifference > 500)
                    {
                        if(event.getAction() == MotionEvent.ACTION_MOVE)
                        {
                            String infoValuesText = "";

                            EPrayerTimeType prayerTimeType = this.prayerTimeTypeWithAssociatedTextView.entrySet().stream().filter(x -> x.getValue() == v).findFirst().get().getKey();

                            if(AppEnvironment.DayPrayerTimeSettings.containsKey(prayerTimeType))
                            {
                                PrayerTimeSettingsEntity settings = AppEnvironment.DayPrayerTimeSettings.get(prayerTimeType);

                                infoValuesText = settings.get_api().toString()
                                        + "\n" + settings.get_minuteAdjustment()
                                        + "\n" + (settings.getFajrCalculationDegree() != null ? settings.getFajrCalculationDegree() : "")
                                        + "\n" + (settings.getIshaCalculationDegree() != null ? settings.getIshaCalculationDegree() : "");
                            }

                            binding.timeInfoValuesTextLabel.setText(infoValuesText);
                        }
                        else if(event.getAction() == MotionEvent.ACTION_UP)
                        {
                            useUpEvent = true;
                            binding.timeInfoValuesTextLabel.setText("");
                        }
                    }
                }

                break;

            default:
                break;
        }

        return useUpEvent;
    }

    public static String INTENT_EXTRA = "prayerTime";

    private void openSettingsForSpecificPrayerTimeType(EPrayerTimeType prayerTimeType)
    {
        try
        {
            Intent myIntent = new Intent(TimeOverviewActivity.this, TimeSettingsActivity.class);
            myIntent.putExtra(INTENT_EXTRA, prayerTimeType); //Optional parameters
            TimeOverviewActivity.this.startActivity(myIntent);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    Map<EPrayerTimeType, DayPrayerTimesEntity> muwaqqitTimesHashMap = new HashMap<>();
    Map<EPrayerTimeType, DayPrayerTimesEntity> diyanetTimesHashMap = new HashMap<>();

    public void retrievePrayerTimes()
    {
        try
        {
            Location targetLocation = AppEnvironment.RetrieveLocation(this);

            if(targetLocation == null)
            {
                new Handler(Looper.getMainLooper())
                        .post(() ->
                        new AlertDialog.Builder(this)
                                .setTitle("LOCATION NOT AVAILABLE")
                                .setMessage("Location could not be retrieved!")
                                .show()
                        );

                binding.showStuffButton.setEnabled(true);
                binding.progressBar.setVisibility(View.INVISIBLE);
                return;
            }

            Map<EPrayerTimeType, PrayerTimeSettingsEntity> toBeCalculatedPrayerTimes = AppEnvironment.DayPrayerTimeSettings;

            retrieveDiyanetTimes(toBeCalculatedPrayerTimes, targetLocation);
            retrieveMuwaqqitTimes(toBeCalculatedPrayerTimes, targetLocation);
            applyTimesToPrayerEntities();

            new Handler(Looper.getMainLooper()).post(() ->
            {
                applyTimeSettingsToOverview();
                binding.showStuffButton.setEnabled(true);
                binding.progressBar.setVisibility(View.INVISIBLE);
            });
        }
        catch (Exception e)
        {
            new Handler(Looper.getMainLooper()).post(() ->
            {
                binding.statusTextLabel.setText("Error!");
                binding.showStuffButton.setEnabled(true);
                binding.progressBar.setVisibility(View.INVISIBLE);
            });
            e.printStackTrace();
        }
    }

    private void retrieveDiyanetTimes(Map<EPrayerTimeType, PrayerTimeSettingsEntity> toBeCalculatedPrayerTimes, Location targetLocation) throws Exception
    {
        // reset values
        diyanetTimesHashMap.clear();

        Map<EPrayerTimeType, PrayerTimeSettingsEntity> diyanetTimes =
                toBeCalculatedPrayerTimes.entrySet().stream()
                        .filter(x -> x.getValue().get_api() == ESupportedAPIs.Diyanet)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // ADD ALL DIYANET TIME CALCULATIONS
        if(diyanetTimes.size() > 0)
        {
            DayPrayerTimesEntity diyanetTime = HttpAPIRequestUtil.RetrieveDiyanetTimes(this, targetLocation);

            if(diyanetTime != null)
            {
                diyanetTimesHashMap = diyanetTimes.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, y -> diyanetTime));
            }
        }
    }

    private void retrieveMuwaqqitTimes(Map<EPrayerTimeType, PrayerTimeSettingsEntity> toBeCalculatedPrayerTimes, Location targetLocation) throws Exception
    {
        // reset values
        muwaqqitTimesHashMap.clear();

        Map<EPrayerTimeType, PrayerTimeSettingsEntity> muwaqqitTimesHashMap =
                toBeCalculatedPrayerTimes.entrySet().stream()
                        .filter(x -> x.getValue().get_api() == ESupportedAPIs.Muwaqqit)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<EPrayerTimeType, PrayerTimeSettingsEntity> degreeMuwaqqitTimesHashMap =
                muwaqqitTimesHashMap.entrySet().stream()
                        .filter(x -> PrayerTimeSettingsEntity.DEGREE_TYPES.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<EPrayerTimeType, PrayerTimeSettingsEntity> nonDegreeMuwaqqitTimesHashMap =
                muwaqqitTimesHashMap.entrySet().stream()
                        .filter(x -> !PrayerTimeSettingsEntity.DEGREE_TYPES.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if(degreeMuwaqqitTimesHashMap.size() > 0)
        {
            Map<EPrayerTimeType, PrayerTimeSettingsEntity> fajrDegreeMuwaqqitTimesHashMap =
                    degreeMuwaqqitTimesHashMap.entrySet().stream()
                            .filter(x -> PrayerTimeSettingsEntity.FAJR_DEGREE_TYPES.contains(x.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<EPrayerTimeType, PrayerTimeSettingsEntity> ishaDegreeMuwaqqitTimesHashMap =
                    degreeMuwaqqitTimesHashMap.entrySet().stream()
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

                this.muwaqqitTimesHashMap.put(fajrDegreeEntry.getKey(), degreeMuwaqqitTimeEntity);
                this.muwaqqitTimesHashMap.put(ishaDegreeEntry.getKey(), degreeMuwaqqitTimeEntity);

                // remove handled entries from the lists
                fajrDegreeMuwaqqitTimesHashMap.remove(fajrDegreeEntry.getKey());
                ishaDegreeMuwaqqitTimesHashMap.remove(ishaDegreeEntry.getKey());

                degreeMuwaqqitTimesHashMap.remove(fajrDegreeEntry.getKey());
                degreeMuwaqqitTimesHashMap.remove(ishaDegreeEntry.getKey());
            }

            // ADD REMAINING CALCULATIONS FOR NON MERGABLE DEGREE TIMES
            for(Map.Entry<EPrayerTimeType, PrayerTimeSettingsEntity> entry : degreeMuwaqqitTimesHashMap.entrySet())
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

                this.muwaqqitTimesHashMap.put(prayerTimeType, degreeMuwaqqitTimeEntity);
            }
        }

        // ADD CALCULATIONS FOR NON DEGREE TIMES
        if(nonDegreeMuwaqqitTimesHashMap.size() > 0)
        {
            DayPrayerTimesEntity nonDegreeMuwaqqitTimeEntity;

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

    private void applyTimesToPrayerEntities()
    {
        for(PrayerEntity prayerEntity : PrayerEntity.prayers)
        {
            Time beginningTime = getCorrectTime(prayerEntity.getBeginningTimeType());
            Time endTime = getCorrectTime(prayerEntity.getEndTimeType());

            prayerEntity.setBeginningTime(beginningTime);
            prayerEntity.setEndTime(endTime);
        }
    }

    private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private Time getCorrectTime(EPrayerTimeType prayerTimeType)
    {
        if(AppEnvironment.DayPrayerTimeSettings.containsKey(prayerTimeType))
        {
            PrayerTimeSettingsEntity settings = AppEnvironment.DayPrayerTimeSettings.get(prayerTimeType);

            Time correctTime = null;

            if(settings != null)
            {
                // TODO: Isha-Ende muss Fajr des  *Folgetages* sein!

                if (settings.get_api() == ESupportedAPIs.Muwaqqit
                        && muwaqqitTimesHashMap.containsKey(prayerTimeType)
                        && muwaqqitTimesHashMap.get(prayerTimeType) != null)
                {
                    correctTime = muwaqqitTimesHashMap.get(prayerTimeType).GetTimeByType(prayerTimeType);
                }
                else if (settings.get_api() == ESupportedAPIs.Diyanet
                        && diyanetTimesHashMap.containsKey(prayerTimeType)
                        && diyanetTimesHashMap.get(prayerTimeType) != null)
                {
                    correctTime = diyanetTimesHashMap.get(prayerTimeType).GetTimeByType(prayerTimeType);
                }
            }

            if(correctTime != null)
            {
                long minuteAdjustment = (long) settings.get_minuteAdjustment();

                // minute adjustment
                correctTime = new Time(correctTime.getTime() + (minuteAdjustment * 60 * 1000));

                return correctTime;
            }
        }

        return null;
    }

    private void applyTimeSettingsToOverview()
    {
        try
        {
            binding.displayedDateTextLabel.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now()));

            for(PrayerEntity prayerEntity : PrayerEntity.prayers)
            {
                String beginningText = this.getResources().getString(R.string.no_time_display_text);
                String endText = this.getResources().getString(R.string.no_time_display_text);

                if(prayerEntity.getBeginningTime() != null)
                {
                    beginningText = timeFormat.format(prayerEntity.getBeginningTime().getTime());
                }

                if(prayerEntity.getEndTime() != null)
                {
                    endText = timeFormat.format(prayerEntity.getEndTime().getTime());
                }

                TextView beginningTimeTextView = this.prayerTimeTypeWithAssociatedTextView.get(prayerEntity.getBeginningTimeType());
                TextView endTimeTextView = this.prayerTimeTypeWithAssociatedTextView.get(prayerEntity.getEndTimeType());

                beginningTimeTextView.setText(beginningText);
                endTimeTextView.setText(endText);
            }

            LocalDateTime currentDate = LocalDateTime.now();
            Time currentTime = new Time(currentDate.getHour(), currentDate.getMinute(), currentDate.getSecond());

            binding.testCustomView.setDisplayPrayerEntity(PrayerEntity.GetPrayerByTime(currentTime));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}