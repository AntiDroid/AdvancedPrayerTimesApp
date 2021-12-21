package com.example.advancedprayertimes.Logic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;

import com.example.advancedprayertimes.PrayerTimesOverviewActivity;
import com.google.firebase.crashlytics.buildtools.api.net.Constants;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpAPIRequestUtil
{
    public static Location RetrieveLocation(Context context)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // do permission failed check stuff...
        }

        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public static Address RetrieveCityByLocation(Context context, Location location)
    {
        Geocoder geocoder = new Geocoder(context);

        try
        {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

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

    public static DayPrayerTimeEntity RetrieveDiyanetTimes(Context context, Location targetLocation) throws JSONException, ParseException
    {
        Address cityAddress = HttpAPIRequestUtil.RetrieveCityByLocation(context, targetLocation);

        // ######################

        String ulkelerList = HttpAPIRequestUtil.RetrieveAPIFeedback("https://ezanvakti.herokuapp.com/ulkeler", "GET");
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

        String sehirlerList = HttpAPIRequestUtil.RetrieveAPIFeedback("https://ezanvakti.herokuapp.com/sehirler/" + ulkeID, "GET");
        JSONArray sehirlerJSONArray = new JSONArray(sehirlerList);

        String sehirID = null;

        if(sehirlerJSONArray.length() > 0)
        {
            JSONObject obj = (JSONObject) sehirlerJSONArray.get(0);

            sehirID = obj.getString("SehirID");
        }

        // ######################

        String ilcelerList = HttpAPIRequestUtil.RetrieveAPIFeedback("https://ezanvakti.herokuapp.com/ilceler/" + sehirID, "GET");
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

        String vakitlerList = HttpAPIRequestUtil.RetrieveAPIFeedback("https://ezanvakti.herokuapp.com/vakitler/" + ilceID, "GET");
        JSONArray vakitlerJSONArray = new JSONArray(vakitlerList);

        String todayDate = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now());

        DayPrayerTimeEntity time = null;

        for (int i = 0; i < vakitlerJSONArray.length(); i++)
        {
            JSONObject obj = (JSONObject) vakitlerJSONArray.get(i);

            if(obj.getString("MiladiTarihKisa").equals(todayDate))
            {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

                Date fajrTimeBeginning = sdf.parse(obj.getString("Imsak"));
                Date fajrTimeEnd = sdf.parse(obj.getString("Gunes"));

                Date dhuhrTimeBeginning = sdf.parse(obj.getString("Ogle"));
                Date dhuhrTimeEnd = sdf.parse(obj.getString("Ikindi"));

                Date asrTimeBeginning = sdf.parse(obj.getString("Ikindi"));
                Date asrTimeEnd = sdf.parse(obj.getString("Aksam"));

                Date maghribTimeBeginning = sdf.parse(obj.getString("Aksam"));
                Date maghribTimeEnd = sdf.parse(obj.getString("Yatsi"));

                Date ishaTimeBeginning = sdf.parse(obj.getString("Yatsi"));
                Date ishaTimeEnd = sdf.parse(obj.getString("Imsak"));

                return new DayPrayerTimeEntity(
                        fajrTimeBeginning,
                        fajrTimeEnd,
                        dhuhrTimeBeginning,
                        dhuhrTimeEnd,
                        asrTimeBeginning,
                        asrTimeEnd,
                        maghribTimeBeginning,
                        maghribTimeEnd,
                        ishaTimeBeginning,
                        ishaTimeEnd
                );
            }
        }

        return null;
    }

    public static DayPrayerTimeEntity RetrieveMuwaqqitTimes(Location targetLocation) throws JSONException, ParseException
    {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("d", "2021-12-13");
        queryParameters.put("ln", Double.toString(targetLocation.getLongitude()));
        queryParameters.put("lt", Double.toString(targetLocation.getLatitude()));
        queryParameters.put("tz", "Europe/Berlin");

        String response = HttpAPIRequestUtil.RetrieveAPIFeedback("https://www.muwaqqit.com/api.json", "POST", queryParameters);

        return ReadMuwaqqitTimeJSONAsDayPrayerTime(response);
    }

    public static String RetrieveAPIFeedback(String urlText, String requestMethod)
    {
        HashMap<String, String> queryParameters = new HashMap<>();
        return RetrieveAPIFeedback(urlText, requestMethod, queryParameters);
    }

    public static String RetrieveAPIFeedback(String urlText, String requestMethod, Map<String, String> queryParameters)
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

    public static DayPrayerTimeEntity ReadMuwaqqitTimeJSONAsDayPrayerTime(String jsonText) throws JSONException, ParseException
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

                Date fajrTimeBeginning = sdf.parse(obj.getString("fajr_time"));
                Date fajrTimeEnd = sdf.parse(obj.getString("sunrise_time"));

                Date dhuhrTimeBeginning = sdf.parse(obj.getString("zohr_time"));
                Date dhuhrTimeEnd = sdf.parse(obj.getString("mithl_time"));

                Date asrTimeBeginning = sdf.parse(obj.getString("mithl_time"));
                Date asrTimeEnd = sdf.parse(obj.getString("sunset_time"));

                Date maghribTimeBeginning = sdf.parse(obj.getString("sunset_time"));
                Date maghribTimeEnd = sdf.parse(obj.getString("esha_time"));

                Date ishaTimeBeginning = sdf.parse(obj.getString("esha_time"));
                Date ishaTimeEnd = sdf.parse(obj.getString("fajr_time"));

                return new DayPrayerTimeEntity(
                        fajrTimeBeginning,
                        fajrTimeEnd,
                        dhuhrTimeBeginning,
                        dhuhrTimeEnd,
                        asrTimeBeginning,
                        asrTimeEnd,
                        maghribTimeBeginning,
                        maghribTimeEnd,
                        ishaTimeBeginning,
                        ishaTimeEnd
                );
            }
        }

        return null;
    }

}
