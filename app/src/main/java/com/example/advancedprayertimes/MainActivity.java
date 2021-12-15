package com.example.advancedprayertimes;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.advancedprayertimes.databinding.ActivityMainBinding;
import com.example.advancedprayertimes.ui.main.SectionsPagerAdapter;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    public String doRequestStuff()
    {
        HttpURLConnection conn = null;

        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();
        try
        {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // do permission failed check stuff...
            }

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            URL url = new URL("https://www.muwaqqit.com/api.json");
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("d", "2021-12-13")
                    .appendQueryParameter("ln", Double.toString(lastLocation.getLongitude()))
                    .appendQueryParameter("lt", Double.toString(lastLocation.getLatitude()))
                    .appendQueryParameter("tz", "Europe/Berlin");
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            // Test if the response from the server is successful
            int status = conn.getResponseCode();

            if (status >= 300)
            {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }
            else {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }

            return responseContent.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            conn.disconnect();
        }

        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        binding.showStuffButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run()
                    {

                        try
                        {
                            String text = doRequestStuff();

                            new Handler(Looper.getMainLooper()).post(new Runnable ()
                            {
                                @Override
                                public void run ()
                                {
                                    try
                                    {
                                        JSONObject jsonObject = new JSONObject(text);
                                        JSONArray list = (JSONArray)jsonObject.get("list");

                                        String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

                                        for(int i = 0; i < list.length(); i++)
                                        {
                                            JSONObject obj = (JSONObject) list.getJSONObject(i);

                                            if(todayDate.equals(obj.getString("fajr_date")))
                                            {
                                                binding.fajrTimeTextLabel.setText(obj.getString("fajr_time"));
                                                binding.dhuhrTimeTextLabel.setText(obj.getString("zohr_time"));
                                                binding.asrTimeTextLabel.setText(obj.getString("mithl_time"));
                                                binding.maghribTimeTextLabel.setText(obj.getString("sunset_time"));
                                                binding.ishaTimeTextLabel.setText(obj.getString("esha_time"));
                                                break;
                                            }
                                        }
                                    }
                                    catch(Exception e)
                                    {
                                        String lol = "Hallo";
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
            }

        });
    }
}