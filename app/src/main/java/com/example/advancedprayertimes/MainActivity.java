package com.example.advancedprayertimes;

import android.Manifest;
import android.content.Context;
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

public class MainActivity extends AppCompatActivity
{
    private static ActivityMainBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Spinner spinner = (Spinner) binding.spinner;

        String [] arrmile ={"Muwaqqit","Diyanet"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,arrmile);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setEnabled(true);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

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
        Location targetLocation = this.RetrieveLocation();
        String selectedItem = binding.spinner.getSelectedItem().toString();

        switch(selectedItem)
        {
            case "Diyanet":

                currentTimes = this.RetrieveDiyanetTimes(targetLocation);

                break;

            case "Muwaqqit":

                currentTimes = this.RetrieveMuwaqqitTimes(targetLocation);

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
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                    binding.fajrTimeTextLabel.setText(dateFormat.format(currentTimes.get_fajrTime()));
                    binding.dhuhrTimeTextLabel.setText(dateFormat.format(currentTimes.get_dhuhrTime()));
                    binding.asrTimeTextLabel.setText(dateFormat.format(currentTimes.get_asrTime()));
                    binding.maghribTimeTextLabel.setText(dateFormat.format(currentTimes.get_maghribTime()));
                    binding.ishaTimeTextLabel.setText(dateFormat.format(currentTimes.get_ishaTime()));
                }
                catch(Exception e)
                {

                }
            }
        });
    }

    public DayPrayerTimeEntity RetrieveDiyanetTimes(Location targetLocation) throws JSONException, ParseException
    {
        Address cityAddress = this.RetrieveCityByLocation(targetLocation);

        // ######################

        String ulkelerList = this.RetrieveAPIFeedback("https://ezanvakti.herokuapp.com/ulkeler", "GET");
        JSONArray ulkelerJSONArray = new JSONArray(ulkelerList);

        String ulkeID = null;

        for (int i = 0; i < ulkelerJSONArray.length(); i++)
        {
            JSONObject obj = (JSONObject) ulkelerJSONArray.get(i);

            if(obj.getString("UlkeAdiEn").equals(cityAddress.getCountryName().toUpperCase()))
            {
                ulkeID = obj.getString("UlkeID");
            }
        }

        // ######################

        String sehirlerList = this.RetrieveAPIFeedback("https://ezanvakti.herokuapp.com/sehirler/" + ulkeID, "GET");
        JSONArray sehirlerJSONArray = new JSONArray(sehirlerList);

        String sehirID = null;

        if(sehirlerJSONArray.length() > 0)
        {
            JSONObject obj = (JSONObject) sehirlerJSONArray.get(0);

            sehirID = obj.getString("SehirID");
        }

        // ######################

        String ilcelerList = this.RetrieveAPIFeedback("https://ezanvakti.herokuapp.com/ilceler/" + sehirID, "GET");
        JSONArray ilcelerJSONArray = new JSONArray(ilcelerList);

        String ilceID = null;

        for (int i = 0; i < ilcelerJSONArray.length(); i++)
        {
            JSONObject obj = (JSONObject) ilcelerJSONArray.get(i);

            if(obj.getString("IlceAdiEn").equals(cityAddress.getLocality().toUpperCase()))
            {
                ilceID = obj.getString("IlceID");
            }
        }

        // ######################

        String vakitlerList = this.RetrieveAPIFeedback("https://ezanvakti.herokuapp.com/vakitler/" + ilceID, "GET");
        JSONArray vakitlerJSONArray = new JSONArray(vakitlerList);

        String todayDate = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now());

        DayPrayerTimeEntity time = null;

        for (int i = 0; i < vakitlerJSONArray.length(); i++)
        {
            JSONObject obj = (JSONObject) vakitlerJSONArray.get(i);

            if(obj.getString("MiladiTarihKisa").equals(todayDate))
            {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

                Date fajrTime = sdf.parse(obj.getString("Imsak"));
                Date dhuhrTime = sdf.parse(obj.getString("Ogle"));
                Date asrTime = sdf.parse(obj.getString("Ikindi"));
                Date maghribTime = sdf.parse(obj.getString("Aksam"));
                Date ishaTime = sdf.parse(obj.getString("Yatsi"));

                return new DayPrayerTimeEntity(fajrTime, dhuhrTime, asrTime, maghribTime, ishaTime);
            }
        }

        return null;
    }


    public DayPrayerTimeEntity RetrieveMuwaqqitTimes(Location targetLocation) throws JSONException, ParseException
    {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("d", "2021-12-13");
        queryParameters.put("ln", Double.toString(targetLocation.getLongitude()));
        queryParameters.put("lt", Double.toString(targetLocation.getLatitude()));
        queryParameters.put("tz", "Europe/Berlin");

        String response = this.RetrieveAPIFeedback("https://www.muwaqqit.com/api.json", "POST", queryParameters);

        return ReadMuwaqqitTimeJSONAsDayPrayerTime(response);
    }

    public Location RetrieveLocation()
    {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // do permission failed check stuff...
        }

        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    public Address RetrieveCityByLocation(Location location)
    {
        Geocoder geocoder = new Geocoder(this);

        try
        {
            List<Address>addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

            if (geocoder.isPresent())
            {
                StringBuilder stringBuilder = new StringBuilder();

                if (addresses.size() > 0)
                {
                    return addresses.get(0);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String RetrieveAPIFeedback(String urlText, String requestMethod)
    {
        HashMap<String, String> queryParameters = new HashMap<>();
        return RetrieveAPIFeedback(urlText, requestMethod, queryParameters);
    }

    public String RetrieveAPIFeedback(String urlText, String requestMethod, Map<String, String> queryParameters)
    {
        HttpURLConnection conn = null;
        String apiFeedback = null;

        try
        {
            BufferedReader reader;
            String line;
            StringBuilder responseContent = new StringBuilder();

            URL url = new URL(urlText);
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod(requestMethod);
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);

            Uri.Builder builder = new Uri.Builder();

            for(Map.Entry<String, String> entry : queryParameters.entrySet())
            {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }

            String query = builder.build().getEncodedQuery();

            if(requestMethod == "POST")
            {
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                if(queryParameters.size() != 0)
                {
                    writer.write(query);
                }

                writer.flush();
                writer.close();
                os.close();
            }

            // Test if the response from the server is successful
            int status = conn.getResponseCode();

            if (status >= 300)
            {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                while ((line = reader.readLine()) != null)
                {
                    responseContent.append(line);
                }

                reader.close();
            }
            else
            {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((line = reader.readLine()) != null)
                {
                    responseContent.append(line);
                }

                reader.close();
            }

            apiFeedback = responseContent.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            conn.disconnect();
        }

        return apiFeedback;
    }

    public DayPrayerTimeEntity ReadMuwaqqitTimeJSONAsDayPrayerTime(String jsonText) throws JSONException, ParseException
    {
        JSONObject jsonObject = new JSONObject(jsonText);
        JSONArray list = (JSONArray)jsonObject.get("list");

        String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

        for(int i = 0; i < list.length(); i++)
        {
            JSONObject obj = (JSONObject) list.getJSONObject(i);

            if(todayDate.equals(obj.getString("fajr_date")))
            {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                Date fajrTime = sdf.parse(obj.getString("fajr_time"));
                Date dhuhrTime = sdf.parse(obj.getString("zohr_time"));
                Date asrTime = sdf.parse(obj.getString("mithl_time"));
                Date maghribTime = sdf.parse(obj.getString("sunset_time"));
                Date ishaTime = sdf.parse(obj.getString("esha_time"));

                return new DayPrayerTimeEntity(fajrTime, dhuhrTime, asrTime, maghribTime, ishaTime);
            }
        }

        return null;
    }
}