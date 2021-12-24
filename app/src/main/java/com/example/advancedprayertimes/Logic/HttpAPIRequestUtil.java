package com.example.advancedprayertimes.Logic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;

import com.example.advancedprayertimes.Logic.Enums.EHttpRequestMethod;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HttpAPIRequestUtil
{
    private static final String MUWAQQIT_JSON_URL = "https://www.muwaqqit.com/api.json";
    private static final String DIYANET_JSON_URL = "https://ezanvakti.herokuapp.com";

    private static final int MUWAQQIT_API_COOLDOWN_SECONDS = 11;

    private static final String BING_MAPS_URL = "https://dev.virtualearth.net/REST/v1/timezone/";
    private static final String BING_MAPS_API_KEY = "AmlyD3G1euqPpGehI1B9s55Pzr2nE-joqnfgBM5ZvHUSn49p-WpQJbrr3NlAE_nw";

    public static Address RetrieveCityByLocation(Context context, Location location) throws Exception
    {
        Geocoder geocoder = new Geocoder(context);

        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

        if (Geocoder.isPresent() && addresses.size() > 0)
        {
            return addresses.get(0);
        }

        return null;
    }

    // Der JSON enthält direkt in der ersten Ebene die Gebetszeiteninformationen für alle Tage des jeweiligen Monats.
    public static DayPrayerTimeEntity RetrieveDiyanetTimes(Context context, Location targetLocation) throws Exception
    {
        Address cityAddress = HttpAPIRequestUtil.RetrieveCityByLocation(context, targetLocation);

        if(cityAddress == null)
        {
            throw new Exception();
        }

        String ulkelerList = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.DIYANET_JSON_URL + "/ulkeler", EHttpRequestMethod.GET);
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

        String sehirlerList = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.DIYANET_JSON_URL + "/sehirler/" + ulkeID, EHttpRequestMethod.GET);
        JSONArray sehirlerJSONArray = new JSONArray(sehirlerList);

        String sehirID = null;

        if(sehirlerJSONArray.length() > 0)
        {
            JSONObject obj = (JSONObject) sehirlerJSONArray.get(0);

            sehirID = obj.getString("SehirID");
        }

        // ######################

        String ilcelerList = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.DIYANET_JSON_URL + "/ilceler/" + sehirID, EHttpRequestMethod.GET);
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

        String vakitlerList = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.DIYANET_JSON_URL + "/vakitler/" + ilceID, EHttpRequestMethod.GET);
        String todayDate = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now());

        Gson gson = new Gson();

        Type listOfDiyanetPrayerTimeEntity = new TypeToken<ArrayList<DiyanetPrayerTimeEntity>>() {}.getType();

        List<DiyanetPrayerTimeEntity> outputList = gson.fromJson(vakitlerList, listOfDiyanetPrayerTimeEntity);
        Optional<DiyanetPrayerTimeEntity> element = outputList.stream().filter(x -> todayDate.equals(x.getDate())).findFirst();

        if(element.isPresent())
        {
            DiyanetPrayerTimeEntity prayerTimeToday = element.get();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            Date fajrTime = sdf.parse(prayerTimeToday.getFajrTime());
            Date sunRiseTime = sdf.parse(prayerTimeToday.getSunrise_time());

            Date dhuhrTime = sdf.parse(prayerTimeToday.getDhuhrTime());
            Date asrTime = sdf.parse(prayerTimeToday.getAsrTime());

            Date maghribTime = sdf.parse(prayerTimeToday.getMaghribTime());
            Date ishaTime = sdf.parse(prayerTimeToday.getIshaTime());

            return new DayPrayerTimeEntity(
                    fajrTime,
                    sunRiseTime,
                    dhuhrTime,
                    asrTime,
                    maghribTime,
                    ishaTime
            );
        }

        return null;
    }

    public static DayPrayerTimeEntity RetrieveMuwaqqitTimes(Location targetLocation, Double fajrDegree, Double ishaDegree) throws Exception
    {
        String timeZone;

        try
        {
            timeZone = RetrieveTimeZoneByLocation(targetLocation);
        }
        catch(Exception e)
        {
            timeZone = "Europe/Berlin";
        }

        HashMap<String, String> queryParameters = new HashMap<>();

        String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

        queryParameters.put("d", todayDate);
        queryParameters.put("ln", Double.toString(targetLocation.getLongitude()));
        queryParameters.put("lt", Double.toString(targetLocation.getLatitude()));
        queryParameters.put("tz", timeZone);

        if(fajrDegree != null)
        {
            queryParameters.put("fa", fajrDegree.toString());
        }

        if(ishaDegree != null)
        {
            queryParameters.put("ea", ishaDegree.toString());
        }

        String response = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.MUWAQQIT_JSON_URL, EHttpRequestMethod.POST, queryParameters);

        // After two consecutive api calls, the next one is only possible after 11 seconds.
        if(response.equals("429 TOO MANY REQUESTS"))
        {
            TimeUnit.SECONDS.sleep(HttpAPIRequestUtil.MUWAQQIT_API_COOLDOWN_SECONDS);
            response = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.MUWAQQIT_JSON_URL, EHttpRequestMethod.POST, queryParameters);
        }

        return HttpAPIRequestUtil.FromMuwaqqitJSONToDayPrayerTime(response);
    }

    public static String RetrieveTimeZoneByLocation(Location targetLocation) throws JSONException
    {
        String urlText =
                HttpAPIRequestUtil.BING_MAPS_URL +
                targetLocation.getLatitude() + "," + targetLocation.getLongitude() +
                        "?key="+HttpAPIRequestUtil.BING_MAPS_API_KEY;

        String response = HttpAPIRequestUtil.RetrieveAPIFeedback(urlText, EHttpRequestMethod.GET);

        JSONObject obj = new JSONObject(response);

        return obj.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getJSONObject("timeZone").getString("ianaTimeZoneId");
    }

    public static String RetrieveAPIFeedback(String urlText, EHttpRequestMethod requestMethod)
    {
        HashMap<String, String> queryParameters = new HashMap<>();
        return HttpAPIRequestUtil.RetrieveAPIFeedback(urlText, requestMethod, queryParameters);
    }

    public static String RetrieveAPIFeedback(String urlText, EHttpRequestMethod requestMethod, Map<String, String> queryParameters)
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
            conn.setRequestMethod(requestMethod.toString());
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);

            Uri.Builder builder = new Uri.Builder();

            for(Map.Entry<String, String> entry : queryParameters.entrySet())
            {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }

            String query = builder.build().getEncodedQuery();

            if(requestMethod.equals(EHttpRequestMethod.POST))
            {
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

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

            // TODO: Fehlerstatus der einzelnen APIs korrekt behandeln
            if (status >= 300)
            {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            else
            {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }

            while ((line = reader.readLine()) != null)
            {
                responseContent.append(line);
            }

            reader.close();

            apiFeedback = responseContent.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(conn != null)
            {
                conn.disconnect();
            }
        }

        return apiFeedback;
    }

    // Der JSON ist in der ersten Ebene eine Liste und diese Liste enthält dann die Gebetszeiteninformationen für alle Tage des jeweiligen Monats.
    public static DayPrayerTimeEntity FromMuwaqqitJSONToDayPrayerTime(String jsonText) throws Exception
    {
        JSONObject jsonObject = new JSONObject(jsonText);
        JSONArray list = (JSONArray) jsonObject.get("list");

        String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

        Gson gson = new Gson();

        Type listOfMyClassObject = new TypeToken<ArrayList<MuwaqqitPrayerTimeEntity>>() {}.getType();

        List<MuwaqqitPrayerTimeEntity> outputList = gson.fromJson(list.toString(), listOfMyClassObject);
        Optional<MuwaqqitPrayerTimeEntity> element = outputList.stream().filter(x -> todayDate.equals(x.getFajrDate())).findFirst();

        if(element.isPresent())
        {
            MuwaqqitPrayerTimeEntity prayerTimeToday = element.get();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

            Date fajrTime = sdf.parse(prayerTimeToday.getFajrTime());
            Date sunRiseTime = sdf.parse(prayerTimeToday.getSunrise_time());

            Date dhuhrTime = sdf.parse(prayerTimeToday.getDhuhrTime());
            Date asrTime = sdf.parse(prayerTimeToday.getAsrMithlTime());

            Date maghribTime = sdf.parse(prayerTimeToday.getMaghribTime());
            Date ishaTime = sdf.parse(prayerTimeToday.getIshaTime());

            return new DayPrayerTimeEntity(
                    fajrTime,
                    sunRiseTime,
                    dhuhrTime,
                    asrTime,
                    maghribTime,
                    ishaTime
            );
        }

        return null;
    }
}
