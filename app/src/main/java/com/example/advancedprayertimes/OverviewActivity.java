package com.example.advancedprayertimes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.DayPrayerTimeEntity;
import com.example.advancedprayertimes.Logic.DayPrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.Logic.HttpAPIRequestUtil;
import com.example.advancedprayertimes.databinding.OverviewActivityBinding;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

public class OverviewActivity extends AppCompatActivity
{
    private OverviewActivityBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = OverviewActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configureTimeClickEvents();

        binding.showStuffButton.setOnClickListener(view ->
        {
            Thread asyncRetrievePrayerTimesThread = new Thread(this::retrievePrayerTimes);

            binding.statusTextLabel.setText("Loading!");
            binding.showStuffButton.setEnabled(false);
            asyncRetrievePrayerTimesThread.start();
        });
    }

    @Override
    protected void onPause()
    {
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(EPrayerTimeType.FajrBeginning.toString() + "value", binding.fajrTimeBeginningTextLabel.getText().toString());
        editor.putString(EPrayerTimeType.FajrEnd.toString() + "value", binding.fajrTimeEndTextLabel.getText().toString());

        editor.putString(EPrayerTimeType.DhuhrBeginning.toString() + "value", binding.dhuhrTimeBeginningTextLabel.getText().toString());
        editor.putString(EPrayerTimeType.DhuhrEnd.toString() + "value", binding.dhuhrTimeEndTextLabel.getText().toString());

        editor.putString(EPrayerTimeType.AsrBeginning.toString() + "value", binding.asrTimeBeginningTextLabel.getText().toString());
        editor.putString(EPrayerTimeType.AsrEnd.toString() + "value", binding.asrTimeEndTextLabel.getText().toString());

        editor.putString(EPrayerTimeType.MaghribBeginning.toString() + "value", binding.maghribTimeBeginningTextLabel.getText().toString());
        editor.putString(EPrayerTimeType.MaghribEnd.toString() + "value", binding.maghribTimeEndTextLabel.getText().toString());

        editor.putString(EPrayerTimeType.IshaBeginning.toString() + "value", binding.ishaTimeBeginningTextLabel.getText().toString());
        editor.putString(EPrayerTimeType.IshaEnd.toString() + "value", binding.ishaTimeEndTextLabel.getText().toString());

        Gson gson = new Gson();

        for(Map.Entry<EPrayerTimeType, DayPrayerTimeSettingsEntity> entry : AppEnvironment.Instance().DayPrayerTimeSettings.entrySet())
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

        binding.fajrTimeBeginningTextLabel.setText(sharedPref.getString(EPrayerTimeType.FajrBeginning.toString() + "value", "xx:xx"));
        binding.fajrTimeEndTextLabel.setText(sharedPref.getString(EPrayerTimeType.FajrEnd.toString() + "value", "xx:xx"));

        binding.dhuhrTimeBeginningTextLabel.setText(sharedPref.getString(EPrayerTimeType.DhuhrBeginning.toString() + "value", "xx:xx"));
        binding.dhuhrTimeEndTextLabel.setText(sharedPref.getString(EPrayerTimeType.DhuhrEnd.toString() + "value", "xx:xx"));

        binding.asrTimeBeginningTextLabel.setText(sharedPref.getString(EPrayerTimeType.AsrBeginning.toString() + "value", "xx:xx"));
        binding.asrTimeEndTextLabel.setText(sharedPref.getString(EPrayerTimeType.AsrEnd.toString() + "value", "xx:xx"));

        binding.maghribTimeBeginningTextLabel.setText(sharedPref.getString(EPrayerTimeType.MaghribBeginning.toString() + "value", "xx:xx"));
        binding.maghribTimeEndTextLabel.setText(sharedPref.getString(EPrayerTimeType.MaghribEnd.toString() + "value", "xx:xx"));

        binding.ishaTimeBeginningTextLabel.setText(sharedPref.getString(EPrayerTimeType.IshaBeginning.toString() + "value", "xx:xx"));
        binding.ishaTimeEndTextLabel.setText(sharedPref.getString(EPrayerTimeType.IshaEnd.toString() + "value", "xx:xx"));

        String[] enumStrings = Stream.of(EPrayerTimeType.values()).map(EPrayerTimeType::name).toArray(String[]::new);

        Gson gson = new Gson();

        for(String enumName : enumStrings)
        {
            String value = sharedPref.getString(enumName + "settings", null);

            if(value != null)
            {
                try
                {
                    EPrayerTimeType prayerTimeType = EPrayerTimeType.valueOf(enumName);
                    DayPrayerTimeSettingsEntity settings = gson.fromJson(value, DayPrayerTimeSettingsEntity.class);

                    AppEnvironment.Instance().DayPrayerTimeSettings.put(prayerTimeType, settings);
                }
                catch(Exception e)
                {
                    continue;
                }
            }
        }

        super.onResume();
    }

    private void configureTimeClickEvents()
    {
        binding.fajrTimeBeginningTextLabel      .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.FajrBeginning));
        binding.fajrTimeEndTextLabel            .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.FajrEnd));
        binding.dhuhrTimeBeginningTextLabel     .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.DhuhrBeginning));
        binding.dhuhrTimeEndTextLabel           .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.DhuhrEnd));
        binding.asrTimeBeginningTextLabel       .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.AsrBeginning));
        binding.asrTimeEndTextLabel             .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.AsrEnd));
        binding.maghribTimeBeginningTextLabel   .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.MaghribBeginning));
        binding.maghribTimeEndTextLabel         .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.MaghribEnd));
        binding.ishaTimeBeginningTextLabel      .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.IshaBeginning));
        binding.ishaTimeEndTextLabel            .setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(EPrayerTimeType.IshaEnd));
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

    DayPrayerTimeEntity muwaqqitTimes = null;
    DayPrayerTimeEntity diyanetTimes = null;

    public void retrievePrayerTimes()
    {
        try
        {
            Location targetLocation = HttpAPIRequestUtil.RetrieveLocation(this);

            muwaqqitTimes = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation);
            diyanetTimes = HttpAPIRequestUtil.RetrieveDiyanetTimes(this, targetLocation);

            if(muwaqqitTimes == null && diyanetTimes == null)
            {
                new AlertDialog.Builder(this)
                        .setTitle("FEHLER")
                        .setMessage("Daten konnten nicht geladen werden!")
                        .show();
            }

            new Handler(Looper.getMainLooper()).post(() ->
            {
                applyTimeSettingsToOverview();
                binding.statusTextLabel.setText("Success!");
                binding.showStuffButton.setEnabled(true);
            });
        }
        catch (Exception e)
        {
            binding.statusTextLabel.setText("Error!");
            e.printStackTrace();
        }
    }

    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private String getCorrectText(EPrayerTimeType prayerTimeType)
    {
        if(AppEnvironment.Instance().DayPrayerTimeSettings.containsKey(prayerTimeType))
        {
            DayPrayerTimeSettingsEntity settings = AppEnvironment.Instance().DayPrayerTimeSettings.get(prayerTimeType);

            Date targetDate = null;

            if(settings != null)
            {
                if (settings.get_api() == ESupportedAPIs.Muwaqqit)
                {
                    targetDate = muwaqqitTimes.GetTimeByType(prayerTimeType);
                }
                else if (settings.get_api() == ESupportedAPIs.Diyanet)
                {
                    targetDate = diyanetTimes.GetTimeByType(prayerTimeType);
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
            binding.fajrTimeBeginningTextLabel.setText(getCorrectText(EPrayerTimeType.FajrBeginning));
            binding.fajrTimeEndTextLabel.setText(getCorrectText(EPrayerTimeType.FajrEnd));

            binding.dhuhrTimeBeginningTextLabel.setText(getCorrectText(EPrayerTimeType.DhuhrBeginning));
            binding.dhuhrTimeEndTextLabel.setText(getCorrectText(EPrayerTimeType.DhuhrEnd));

            binding.asrTimeBeginningTextLabel.setText(getCorrectText(EPrayerTimeType.AsrBeginning));
            binding.asrTimeEndTextLabel.setText(getCorrectText(EPrayerTimeType.AsrEnd));

            binding.maghribTimeBeginningTextLabel.setText(getCorrectText(EPrayerTimeType.MaghribBeginning));
            binding.maghribTimeEndTextLabel.setText(getCorrectText(EPrayerTimeType.MaghribEnd));

            binding.ishaTimeBeginningTextLabel.setText(getCorrectText(EPrayerTimeType.IshaBeginning));
            binding.ishaTimeEndTextLabel.setText(getCorrectText(EPrayerTimeType.IshaEnd));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}