package com.example.advancedprayertimes.Logic;

import android.location.Address;
import android.location.Location;
import android.net.Uri;

import com.example.advancedprayertimes.BuildConfig;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet.DiyanetIlceEntity;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet.DiyanetSehirEntity;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet.DiyanetUlkeEntity;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.DiyanetPrayerTimeDayEntity;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.MuwaqqitPrayerTimeDayEntity;
import com.example.advancedprayertimes.Logic.Entities.DayPrayerTimesPackageEntity;
import com.example.advancedprayertimes.Logic.Enums.EHttpRequestMethod;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;
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

    // Der JSON enthält direkt in der ersten Ebene die Gebetszeiteninformationen für alle Tage des jeweiligen Monats.
    public static DayPrayerTimesPackageEntity RetrieveDiyanetTimes(Address cityAddress) throws Exception
    {
        if(cityAddress == null)
        {
            throw new Exception("Can not retrieve Diyanet prayer time data without a provided address!", null);
        }

        Gson gson = AppEnvironment.BuildGSON("HH:mm");
        String targetUlkeID = null;
        String sehirID = null;
        String ilceID = null; //AppEnvironment.dbHelper.GetDiyanetIlceIDByCountryAndCityName(cityAddress.getCountryName().toUpperCase(), cityAddress.getLocality().toUpperCase());

        if(ilceID == null)
        {
            StringBuilder ulkelerJSONList = new StringBuilder();
            int ulkelerRequestStatus = 0;

            try
            {
                ulkelerRequestStatus = HttpAPIRequestUtil.RetrieveAPIFeedback(ulkelerJSONList, HttpAPIRequestUtil.DIYANET_JSON_URL + "/ulkeler", EHttpRequestMethod.GET, null);

                if(ulkelerRequestStatus == 0 || ulkelerRequestStatus > 299)
                {
                    return null;
                }

                // TODO: CHECK WHETHER IS JSON IS VALID
                Type ulkelerLstType = new TypeToken<ArrayList<DiyanetUlkeEntity>>() {}.getType();
                List<DiyanetUlkeEntity> ulkelerLst = gson.fromJson(ulkelerJSONList.toString(), ulkelerLstType);

                if(!ulkelerLst.stream().allMatch(x -> x.getUlkeID() != null && !x.getUlkeID().equals("") && x.getUlkeAdiEn() != null && !x.getUlkeAdiEn().equals("")))
                {
                    // WEIRD
                }

                for(DiyanetUlkeEntity ulke : ulkelerLst)
                {
                    if(AppEnvironment.dbHelper.GetDiyanetUlkeIDByName(ulke.getUlkeAdiEn()) == null)
                    {
                        AppEnvironment.dbHelper.AddDiyanetUlke(ulke);
                    }

                    if(ulke.getUlkeAdiEn().equals(cityAddress.getCountryName().toUpperCase()))
                    {
                        targetUlkeID = ulke.getUlkeID();
                        break;
                    }
                }

                if(targetUlkeID == null)
                {
                    return null;
                }
            }
            catch(Exception e)
            {
                throw new Exception("Could not process Diyanet ulke information!", e);
            }

            // ######################

            StringBuilder sehirlerList = new StringBuilder();
            int sehirlerRequestStatus = 0;

            try
            {
                sehirlerRequestStatus = HttpAPIRequestUtil.RetrieveAPIFeedback(sehirlerList, HttpAPIRequestUtil.DIYANET_JSON_URL + "/sehirler/" + targetUlkeID, EHttpRequestMethod.GET, null);

                if(sehirlerRequestStatus == 0 || sehirlerRequestStatus > 299)
                {
                    return null;
                }

                Type sehirlerLstType = new TypeToken<ArrayList<DiyanetSehirEntity>>() {}.getType();
                List<DiyanetSehirEntity> sehirlerLst = gson.fromJson(sehirlerList.toString(), sehirlerLstType);

                if(!sehirlerLst.stream().allMatch(x -> x.getSehirID() != null && !x.getSehirID().equals("") && x.getSehirAdiEn() != null && !x.getSehirAdiEn().equals("")))
                {
                    // WEIRD
                }

                Optional<DiyanetSehirEntity> sehirEntity = sehirlerLst.stream().findFirst();

                // TODO: Support mulitple sehirler
                if(sehirEntity.isPresent())
                {
                    if(AppEnvironment.dbHelper.GetDiyanetSehirIDByName(sehirEntity.get().getSehirAdiEn()) == null)
                    {
                        AppEnvironment.dbHelper.AddDiyanetSehir(targetUlkeID, sehirEntity.get());
                    }

                    sehirID = sehirEntity.get().getSehirID();
                }

                if(sehirID == null)
                {
                    return null;
                }
            }
            catch(Exception e)
            {
                throw new Exception("Could not process Diyanet sehir information!", e);
            }

            // ######################

            StringBuilder ilcelerList = new StringBuilder();
            int ilcelerRequestStatus = 0;

            try
            {
                ilcelerRequestStatus = HttpAPIRequestUtil.RetrieveAPIFeedback(ilcelerList, HttpAPIRequestUtil.DIYANET_JSON_URL + "/ilceler/" + sehirID, EHttpRequestMethod.GET, null);

                if(ilcelerRequestStatus == 0 || ilcelerRequestStatus > 299)
                {
                    return null;
                }

                Type ilcelerLstType = new TypeToken<ArrayList<DiyanetIlceEntity>>() {}.getType();
                List<DiyanetIlceEntity> ilcelerLst = gson.fromJson(ilcelerList.toString(), ilcelerLstType);

                if(!ilcelerLst.stream().allMatch(x -> x.getIlceID() != null && !x.getIlceID().equals("") && x.getIlceAdiEn() != null && !x.getIlceAdiEn().equals("")))
                {
                    // WEIRD
                }

                for(DiyanetIlceEntity ilceEntity : ilcelerLst)
                {
                    if(AppEnvironment.dbHelper.GetDiyanetIlceIDByName(ilceEntity.getIlceAdiEn()) == null)
                    {
                        AppEnvironment.dbHelper.AddDiyanetIlce(sehirID, ilceEntity);
                    }

                    if(ilceEntity.getIlceAdiEn().equals(cityAddress.getLocality().toUpperCase()))
                    {
                        ilceID = ilceEntity.getIlceID();
                        break;
                    }
                }

                if(ilceID == null)
                {
                    return null;
                }
            }
            catch(Exception e)
            {
                throw new Exception("Could not process Diyanet ilce information!", e);
            }
        }

        // ######################

        StringBuilder vakitlerList = new StringBuilder();
        int vakitlerRequestStatus = 0;

        DayPrayerTimesPackageEntity timesPackageEntity = null;

        try
        {
            vakitlerRequestStatus = HttpAPIRequestUtil.RetrieveAPIFeedback(vakitlerList, HttpAPIRequestUtil.DIYANET_JSON_URL + "/vakitler/" + ilceID, EHttpRequestMethod.GET, null);

            if(vakitlerRequestStatus == 0 || vakitlerRequestStatus > 299)
            {
                return null;
            }

            String todayDate = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now());

            Type listOfDiyanetPrayerTimeEntity = new TypeToken<ArrayList<DiyanetPrayerTimeDayEntity>>() {}.getType();

            List<DiyanetPrayerTimeDayEntity> outputList = gson.fromJson(vakitlerList.toString(), listOfDiyanetPrayerTimeEntity);
            Optional<DiyanetPrayerTimeDayEntity> element = outputList.stream().filter(x -> todayDate.equals(x.getDate())).findFirst();

            //TODO: API schickt heutige Daten manchmal nicht
            if(!element.isPresent())
            {
                String tomorrowDate = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now().plusDays(1));
                element = outputList.stream().filter(x -> todayDate.equals(x.getDate())).findFirst();
            }

            if(element.isPresent())
            {
                timesPackageEntity = new DayPrayerTimesPackageEntity(element.get());
            }
        }
        catch(Exception e)
        {
            throw new Exception("Could not process Diyanet vakit information!", e);
        }

        if(timesPackageEntity == null)
        {
            throw new Exception("Could not retrieve Diyanet prayer time information for an unknown reason!", null);
        }

        return timesPackageEntity;
    }

    private static long lastMuwaqqitAPIRequest = 0;

    public static DayPrayerTimesPackageEntity RetrieveMuwaqqitTimes(
            Location targetLocation,
            Double fajrDegree,
            Double ishaDegree,
            Double karahaDegree)
            throws Exception
    {
        String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

        // TODO: CHECK TODAY DATE AS WELL
        MuwaqqitPrayerTimeDayEntity storedMuwaqqitTime = null;

        if(false && karahaDegree == null)
        {
            storedMuwaqqitTime = AppEnvironment.dbHelper.GetMuwaqqitPrayerTimesByDateLocationAndDegrees(todayDate, targetLocation.getLongitude(), targetLocation.getLatitude(), fajrDegree, ishaDegree);
        }

        if(storedMuwaqqitTime != null)
        {
            return new DayPrayerTimesPackageEntity(storedMuwaqqitTime);
        }

        String timeZone = RetrieveTimeZoneByLocation(targetLocation);

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

        if(karahaDegree != null)
        {
            queryParameters.put("ia", karahaDegree.toString());
        }

        StringBuilder response = new StringBuilder();
        int muwaqqitRequestStatus = HttpAPIRequestUtil.RetrieveAPIFeedback(response, HttpAPIRequestUtil.MUWAQQIT_JSON_URL, EHttpRequestMethod.POST, queryParameters);

        if(muwaqqitRequestStatus == 0 || muwaqqitRequestStatus > 299)
        {
            return null;
        }

        int secondTryMuwaqqitRequestStatus = -1;

        // Muwaqqit API calls are only possible after 10 second cool downs.
        // An invalid request resets this time
        if(response.toString().equals("429 TOO MANY REQUESTS"))
        {
            long waitTime = HttpAPIRequestUtil.MUWAQQIT_API_COOLDOWN_MILLISECONDS;

            long timeSinceLastRequest = System.currentTimeMillis() - lastMuwaqqitAPIRequest;

            if(timeSinceLastRequest < HttpAPIRequestUtil.MUWAQQIT_API_COOLDOWN_MILLISECONDS)
            {
                waitTime = HttpAPIRequestUtil.MUWAQQIT_API_COOLDOWN_MILLISECONDS - timeSinceLastRequest + 2500;
            }

            try
            {
                TimeUnit.MILLISECONDS.sleep(waitTime);
            }
            catch( Exception e)
            {
                // DO NOTHING
            }

            response = new StringBuilder();
            secondTryMuwaqqitRequestStatus = HttpAPIRequestUtil.RetrieveAPIFeedback(response, HttpAPIRequestUtil.MUWAQQIT_JSON_URL, EHttpRequestMethod.POST, queryParameters);
        }
        lastMuwaqqitAPIRequest = System.currentTimeMillis();

        if(secondTryMuwaqqitRequestStatus == 0 || secondTryMuwaqqitRequestStatus > 299)
        {
            return null;
        }

        return HttpAPIRequestUtil.FromMuwaqqitJSONToDayPrayerTime(response.toString(), targetLocation, fajrDegree, ishaDegree);
    }

    public static String RetrieveTimeZoneByLocation(Location targetLocation) throws Exception
    {
        String urlText =
                HttpAPIRequestUtil.BING_MAPS_URL +
                targetLocation.getLatitude() + "," + targetLocation.getLongitude();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("key", BuildConfig.BING_API_KEY);

        StringBuilder response = new StringBuilder();
        int timezoneRequestStatus = HttpAPIRequestUtil.RetrieveAPIFeedback(response, urlText, EHttpRequestMethod.GET, parameters);

        if(timezoneRequestStatus == 0 || timezoneRequestStatus > 299)
        {
            throw new Exception("Bing API request for timezone was not successful!", null);
        }

        String timezone = "";

        try
        {
            JSONObject obj = new JSONObject(response.toString());
            timezone = obj.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getJSONObject("timeZone").getString("ianaTimeZoneId");
        }
        catch(Exception e)
        {
            throw new Exception("Bing timezone response could not be processed!", e);
        }

        if(timezone == null || timezone.equals(""))
        {
            throw new Exception("Could not retrieve time zone for an unknown reason!", null);
        }

        return timezone;
    }

    public static int RetrieveAPIFeedback(StringBuilder responseContent, String urlText, EHttpRequestMethod requestMethod, Map<String, String> queryParameters)
    {
        HttpURLConnection conn = null;
        int status = 0;

        try
        {
            BufferedReader reader;
            String line;

            if(requestMethod == EHttpRequestMethod.GET && queryParameters != null && queryParameters.size() > 0)
            {
                String parameterPart = "?";

                for(Map.Entry<String, String> entry : queryParameters.entrySet())
                {
                    parameterPart += entry.getKey() + "=" + entry.getValue() + "&";
                }

                // remove & character at the end
                StringBuffer sb= new StringBuffer(parameterPart);
                sb.deleteCharAt(sb.length()-1);

                urlText += sb.toString();
            }

            URL url = new URL(urlText);
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod(requestMethod.toString());
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);

            if(requestMethod == EHttpRequestMethod.POST)
            {
                Uri.Builder builder = new Uri.Builder();

                for(Map.Entry<String, String> entry : queryParameters.entrySet())
                {
                    builder.appendQueryParameter(entry.getKey(), entry.getValue());
                }

                String query = builder.build().getEncodedQuery();

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
            status = conn.getResponseCode();

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
            responseContent.toString();
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

        return status;
    }

    // Der JSON ist in der ersten Ebene eine Liste und diese Liste enthält dann die Gebetszeiteninformationen für alle Tage des jeweiligen Monats.
    public static DayPrayerTimesPackageEntity FromMuwaqqitJSONToDayPrayerTime(String jsonText, Location location, Double fajrDegree, Double ishaDegree) throws Exception
    {
        DayPrayerTimesPackageEntity timesPackageEntity = null;

        try
        {
            Gson gson = AppEnvironment.BuildGSON("HH:mm:ss");
            String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

            JSONObject jsonObject = new JSONObject(jsonText);

            Type listOfMyClassObject = new TypeToken<ArrayList<MuwaqqitPrayerTimeDayEntity>>() {}.getType();

            JSONArray arrayListJson = jsonObject.getJSONArray("list");

            List<MuwaqqitPrayerTimeDayEntity> outputList = gson.fromJson(arrayListJson.toString(), listOfMyClassObject);

            AppEnvironment.dbHelper.DeleteAllMuwaqqitPrayerTimesByDegrees(fajrDegree, ishaDegree);

            for(MuwaqqitPrayerTimeDayEntity time : outputList)
            {
                AppEnvironment.dbHelper.AddMuwaqqitPrayerTime(time, location);
            }

            Optional<MuwaqqitPrayerTimeDayEntity> element = outputList.stream().filter(x -> todayDate.equals(x.getFajrDate())).findFirst();

            if(element.isPresent())
            {
                timesPackageEntity = new DayPrayerTimesPackageEntity(element.get());
            }
        }
        catch(Exception e)
        {
            throw new Exception("Could not process Muwaqqit prayer times for an unknown reason!", e);
        }

        if(timesPackageEntity == null)
        {
            throw new Exception("Could not retrieve Muwaqqit prayer times for an unknown reason!", null);
        }

        return timesPackageEntity;
    }
}
