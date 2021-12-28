package com.example.advancedprayertimes.Logic;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;

import com.example.advancedprayertimes.Logic.DB.DBHelper;
import com.example.advancedprayertimes.Logic.Entities.DiyanetPrayerTimeDayEntity;
import com.example.advancedprayertimes.Logic.Entities.MuwaqqitPrayerTimeDayEntity;
import com.example.advancedprayertimes.Logic.Entities.DayPrayerTimesEntity;
import com.example.advancedprayertimes.Logic.Enums.EHttpRequestMethod;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private static final long MUWAQQIT_API_COOLDOWN_MILLISECONDS = 11000;

    private static final String BING_MAPS_URL = "https://dev.virtualearth.net/REST/v1/timezone/";
    private static final String BING_MAPS_API_KEY = "AmlyD3G1euqPpGehI1B9s55Pzr2nE-joqnfgBM5ZvHUSn49p-WpQJbrr3NlAE_nw";

    // Der JSON enthält direkt in der ersten Ebene die Gebetszeiteninformationen für alle Tage des jeweiligen Monats.
    public static DayPrayerTimesEntity RetrieveDiyanetTimes(Address cityAddress) throws Exception
    {
        if(cityAddress == null)
        {
            throw new Exception();
        }

        DBHelper helper = new DBHelper(AppEnvironment.context);

        String ulkeID = null;
        String sehirID = null;
        String ilceID = helper.GetDiyanetIlceIDByCountryAndCityName(cityAddress.getCountryName().toUpperCase(), cityAddress.getLocality().toUpperCase());

        if(ilceID == null)
        {
            String ulkelerList = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.DIYANET_JSON_URL + "/ulkeler", EHttpRequestMethod.GET);
            JSONArray ulkelerJSONArray = new JSONArray(ulkelerList);

            for (int i = 0; i < ulkelerJSONArray.length(); i++)
            {
                JSONObject obj = (JSONObject) ulkelerJSONArray.get(i);

                if(helper.GetDiyanetUlkeIDByName(obj.getString("UlkeAdiEn")) == null)
                {
                    helper.AddDiyanetUlke(obj.getString("UlkeID"), obj.getString("UlkeAdiEn"));
                }

                if(obj.getString("UlkeAdiEn").equals(cityAddress.getCountryName().toUpperCase()))
                {
                    ulkeID = obj.getString("UlkeID");
                    break;
                }
            }

            if(ulkeID == null)
            {
                return null;
            }

            // ######################

            String sehirlerList = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.DIYANET_JSON_URL + "/sehirler/" + ulkeID, EHttpRequestMethod.GET);
            JSONArray sehirlerJSONArray = new JSONArray(sehirlerList);

            if(sehirlerJSONArray.length() > 0)
            {
                JSONObject obj = (JSONObject) sehirlerJSONArray.get(0);

                if(helper.GetDiyanetSehirIDByName(obj.getString("SehirAdiEn")) == null)
                {
                    helper.AddDiyanetSehir(ulkeID, obj.getString("SehirID"), obj.getString("SehirAdiEn"));
                }

                sehirID = obj.getString("SehirID");
            }

            if(sehirID == null)
            {
                return null;
            }

            // ######################

            String ilcelerList = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.DIYANET_JSON_URL + "/ilceler/" + sehirID, EHttpRequestMethod.GET);
            JSONArray ilcelerJSONArray = new JSONArray(ilcelerList);

            for (int i = 0; i < ilcelerJSONArray.length(); i++)
            {
                JSONObject obj = (JSONObject) ilcelerJSONArray.get(i);

                if(helper.GetDiyanetIlceIDByName(obj.getString("IlceAdiEn")) == null)
                {
                    helper.AddDiyanetIlce(sehirID, obj.getString("IlceID"), obj.getString("IlceAdiEn"));
                }

                if(obj.getString("IlceAdiEn").equals(cityAddress.getLocality().toUpperCase()))
                {
                    ilceID = obj.getString("IlceID");
                    break;
                }
            }

            if(ilceID == null)
            {
                return null;
            }
        }

        // ######################

        String vakitlerList = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.DIYANET_JSON_URL + "/vakitler/" + ilceID, EHttpRequestMethod.GET);
        String todayDate = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("HH:mm");

        Gson gson = AppEnvironment.BuildGSON("HH:mm");

        Type listOfDiyanetPrayerTimeEntity = new TypeToken<ArrayList<DiyanetPrayerTimeDayEntity>>() {}.getType();

        List<DiyanetPrayerTimeDayEntity> outputList = gson.fromJson(vakitlerList, listOfDiyanetPrayerTimeEntity);
        Optional<DiyanetPrayerTimeDayEntity> element = outputList.stream().filter(x -> todayDate.equals(x.getDate())).findFirst();

        //TODO: API schickt heutige Daten manchmal nicht
        if(!element.isPresent())
        {
            String tomorrowDate = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now().plusDays(1));
            element = outputList.stream().filter(x -> todayDate.equals(x.getDate())).findFirst();
        }

        if(element.isPresent())
        {
            return new DayPrayerTimesEntity(element.get());
        }

        return null;
    }

    private static long lastMuwaqqitAPIRequest = 0;

    public static DayPrayerTimesEntity RetrieveMuwaqqitTimes(Location targetLocation, Double fajrDegree, Double ishaDegree) throws Exception
    {
        String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

        DBHelper helper = new DBHelper(AppEnvironment.context);

        // TODO: CHECK TODAY DATE AS WELL
        MuwaqqitPrayerTimeDayEntity storedMuwaqqitTime = helper.GetMuwaqqitPrayerTimesByDateLocationAndDegrees(todayDate, targetLocation.getLongitude(), targetLocation.getLatitude(), fajrDegree, ishaDegree);

        if(storedMuwaqqitTime != null)
        {
            return new DayPrayerTimesEntity(storedMuwaqqitTime);
        }

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

        // Muwaqqit API calls are only possible after 10 second cool downs.
        // An invalid request resets this time
        if(response.equals("429 TOO MANY REQUESTS"))
        {
            long waitTime = HttpAPIRequestUtil.MUWAQQIT_API_COOLDOWN_MILLISECONDS;

            long timeSinceLastRequest = System.currentTimeMillis() - lastMuwaqqitAPIRequest;

            if(timeSinceLastRequest < HttpAPIRequestUtil.MUWAQQIT_API_COOLDOWN_MILLISECONDS)
            {
                waitTime = HttpAPIRequestUtil.MUWAQQIT_API_COOLDOWN_MILLISECONDS - timeSinceLastRequest + 1500;
            }

            TimeUnit.MILLISECONDS.sleep(waitTime);
            response = HttpAPIRequestUtil.RetrieveAPIFeedback(HttpAPIRequestUtil.MUWAQQIT_JSON_URL, EHttpRequestMethod.POST, queryParameters);

            TimeUnit.MILLISECONDS.sleep(1);
        }
        lastMuwaqqitAPIRequest = System.currentTimeMillis();

        return HttpAPIRequestUtil.FromMuwaqqitJSONToDayPrayerTime(response, targetLocation, fajrDegree, ishaDegree);
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
    public static DayPrayerTimesEntity FromMuwaqqitJSONToDayPrayerTime(String jsonText, Location location, Double fajrDegree, Double ishaDegree) throws Exception
    {
        JSONObject jsonObject = new JSONObject(jsonText);
        JSONArray list = (JSONArray) jsonObject.get("list");

        String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("HH:mm:ss");

        Gson gson = AppEnvironment.BuildGSON("HH:mm:ss");

        Type listOfMyClassObject = new TypeToken<ArrayList<MuwaqqitPrayerTimeDayEntity>>() {}.getType();

        List<MuwaqqitPrayerTimeDayEntity> outputList = gson.fromJson(list.toString(), listOfMyClassObject);

        DBHelper helper = new DBHelper(AppEnvironment.context);

        helper.DeleteAllMuwaqqitPrayerTimesByDegrees(fajrDegree, ishaDegree);

        for(MuwaqqitPrayerTimeDayEntity time : outputList)
        {
            helper.AddMuwaqqitPrayerTime(time, location);
        }

        Optional<MuwaqqitPrayerTimeDayEntity> element = outputList.stream().filter(x -> todayDate.equals(x.getFajrDate())).findFirst();

        if(element.isPresent())
        {
            return new DayPrayerTimesEntity(element.get());
        }

        return null;
    }
}
