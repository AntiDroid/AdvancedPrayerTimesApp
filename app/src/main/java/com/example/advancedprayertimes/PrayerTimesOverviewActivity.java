package com.example.advancedprayertimes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.advancedprayertimes.Logic.DayPrayerTimeEntity;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.Logic.HttpAPIRequestUtil;
import com.example.advancedprayertimes.databinding.ActivityMainBinding;
import com.example.advancedprayertimes.ui.main.SectionsPagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrayerTimesOverviewActivity extends AppCompatActivity
{
    private static ActivityMainBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Spinner apiSelectionSpinner = (Spinner) binding.apiSelectionSpinner;

        String [] arrmile ={"Muwaqqit","Diyanet"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,arrmile);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        apiSelectionSpinner.setAdapter(adapter);

        apiSelectionSpinner.setEnabled(true);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        binding.ishaTextLabel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                try
                {
                    Intent myIntent = new Intent(PrayerTimesOverviewActivity.this, PrayerTimeSettingsActivity.class);
                    myIntent.putExtra("key", "value"); //Optional parameters
                    PrayerTimesOverviewActivity.this.startActivity(myIntent);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        binding.showStuffButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Thread asyncRetrievePrayerTimesThread = new Thread(new Runnable() {

                    @Override
                    public void run()
                    {
                        try
                        {
                            retrievePrayerTimes();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                asyncRetrievePrayerTimesThread.start();
            }
        });
    }

    DayPrayerTimeEntity currentTimes = null;

    public void retrievePrayerTimes() throws Exception
    {
        Location targetLocation = HttpAPIRequestUtil.RetrieveLocation(this);
        String selectedItem = binding.apiSelectionSpinner.getSelectedItem().toString();

        switch(selectedItem)
        {
            case "Diyanet":
                currentTimes = HttpAPIRequestUtil.RetrieveDiyanetTimes(this, targetLocation);
                break;

            case "Muwaqqit":
                currentTimes = HttpAPIRequestUtil.RetrieveMuwaqqitTimes(targetLocation);
                break;

            default:
                break;
        }

        if(currentTimes == null)
        {
            new AlertDialog.Builder(this)
                    .setTitle("FEHLER")
                    .setMessage("Daten konnten nicht geladen werden!")
                    .show();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable ()
        {
            @Override
            public void run ()
            {
                try
                {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm");

                    binding.fajrTimeBeginningTextLabel.setText(dateFormat.format(currentTimes.get_fajrTimeBeginning()));
                    binding.fajrTimeEndTextLabel.setText(dateFormat.format(currentTimes.get_fajrTimeEnd()));

                    binding.dhuhrTimeBeginningTextLabel.setText(dateFormat.format(currentTimes.get_dhuhrTimeBeginning()));
                    binding.dhuhrTimeEndTextLabel.setText(dateFormat.format(currentTimes.get_dhuhrTimeEnd()));

                    binding.asrTimeBeginningTextLabel.setText(dateFormat.format(currentTimes.get_asrTimeBeginning()));
                    binding.asrTimeEndTextLabel.setText(dateFormat.format(currentTimes.get_asrTimeEnd()));

                    binding.maghribTimeBeginningTextLabel.setText(dateFormat.format(currentTimes.get_maghribTimeBeginning()));
                    binding.maghribTimeEndTextLabel.setText(dateFormat.format(currentTimes.get_maghribTimeEnd()));

                    binding.ishaTimeBeginningTextLabel.setText(dateFormat.format(currentTimes.get_ishaTimeBeginning()));
                    binding.ishaTimeEndTextLabel.setText(dateFormat.format(currentTimes.get_ishaTimeEnd()));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}