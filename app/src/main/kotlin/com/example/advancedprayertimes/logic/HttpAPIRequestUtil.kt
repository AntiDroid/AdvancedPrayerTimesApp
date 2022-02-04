package com.example.advancedprayertimes.logic

import android.location.Address
import android.net.Uri
import com.example.advancedprayertimes.BuildConfig
import com.example.advancedprayertimes.logic.AppEnvironment.BuildGSON
import kotlin.Throws
import com.example.advancedprayertimes.logic.api_entities.DiyanetPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.enums.EHttpResponseStatusType
import com.example.advancedprayertimes.logic.enums.EHttpRequestMethod
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetUlkeEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetSehirEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetIlceEntity
import com.example.advancedprayertimes.logic.api_entities.AlAdhanPrayerTimeDayEntity
import org.json.JSONObject
import com.example.advancedprayertimes.logic.api_entities.MuwaqqitPrayerTimeDayEntity
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object HttpAPIRequestUtil {
    private const val MUWAQQIT_JSON_URL = "https://www.muwaqqit.com/api.json"
    private const val DIYANET_JSON_URL = "https://ezanvakti.herokuapp.com"
    private const val ALADHAN_JSON_URL = "https://api.aladhan.com/v1/calendar"
    private const val MUWAQQIT_API_COOLDOWN_MILLISECONDS: Long = 12000
    private const val BING_MAPS_URL = "https://dev.virtualearth.net/REST/v1/timezone/"

    // Der JSON enthält direkt in der ersten Ebene die Gebetszeiteninformationen für alle Tage des jeweiligen Monats.
    @Throws(Exception::class)
    fun RetrieveDiyanetTimes(cityAddress: Address?): DiyanetPrayerTimeDayEntity? {

        if (cityAddress == null) {
            throw Exception(
                "Can not retrieve Diyanet prayer time data without a provided address!",
                null
            )
        }

        val gson = BuildGSON("HH:mm")
        var targetUlkeID: String? = null
        var sehirID: String? = null

        //AppEnvironment.dbHelper.GetDiyanetIlceIDByCountryAndCityName(cityAddress.getCountryName().toUpperCase(), cityAddress.getLocality().toUpperCase());
        var ilceID: String? = null

        if (ilceID == null) {

            val ulkelerJSONList = StringBuilder()

            try {
                val ulkelerResponseStatusType = retrieveAPIFeedback(
                    ulkelerJSONList,
                    "$DIYANET_JSON_URL/ulkeler",
                    EHttpRequestMethod.GET,
                    null
                )
                if (ulkelerResponseStatusType !== EHttpResponseStatusType.Success) {
                    return null
                }

                // TODO: CHECK WHETHER IS JSON IS VALID
                val ulkelerLstType = object : TypeToken<ArrayList<DiyanetUlkeEntity?>?>() {}.type
                val ulkelerLst = gson.fromJson<List<DiyanetUlkeEntity>>(
                    ulkelerJSONList.toString(),
                    ulkelerLstType
                )
                if (!ulkelerLst.stream()
                        .allMatch { x: DiyanetUlkeEntity -> x.ulkeID != null && x.ulkeID != "" && x.ulkeAdiEn != null && x.ulkeAdiEn != "" }
                ) {
                    // WEIRD
                }
                for (ulke in ulkelerLst) {
                    if (AppEnvironment.dbHelper!!.GetDiyanetUlkeIDByName(ulke.ulkeAdiEn!!) == null) {
                        AppEnvironment.dbHelper!!.AddDiyanetUlke(ulke)
                    }
                    if (ulke.ulkeAdiEn == cityAddress.countryName.toUpperCase()) {
                        targetUlkeID = ulke.ulkeID
                        break
                    }
                }
                if (targetUlkeID == null) {
                    return null
                }
            } catch (e: Exception) {
                throw Exception("Could not process Diyanet ulke information!", e)
            }

            // ######################
            val sehirlerList = StringBuilder()

            try {
                val sehirlerResponseStatusType = retrieveAPIFeedback(
                    sehirlerList,
                    "$DIYANET_JSON_URL/sehirler/$targetUlkeID",
                    EHttpRequestMethod.GET,
                    null
                )
                if (sehirlerResponseStatusType !== EHttpResponseStatusType.Success) {
                    return null
                }
                val sehirlerLstType = object : TypeToken<ArrayList<DiyanetSehirEntity?>?>() {}.type
                val sehirlerLst = gson.fromJson<List<DiyanetSehirEntity>>(
                    sehirlerList.toString(),
                    sehirlerLstType
                )
                if (!sehirlerLst.stream()
                        .allMatch { x: DiyanetSehirEntity -> x.sehirID != null && x.sehirID != "" && x.sehirAdiEn != null && x.sehirAdiEn != "" }
                ) {
                    // WEIRD
                }
                val sehirEntity = sehirlerLst.stream().findFirst()

                // TODO: Support mulitple sehirler
                if (sehirEntity.isPresent) {
                    if (AppEnvironment.dbHelper!!.GetDiyanetSehirIDByName(sehirEntity.get().sehirAdiEn!!) == null) {
                        AppEnvironment.dbHelper!!.AddDiyanetSehir(targetUlkeID, sehirEntity.get())
                    }
                    sehirID = sehirEntity.get().sehirID
                }
                if (sehirID == null) {
                    return null
                }
            } catch (e: Exception) {
                throw Exception("Could not process Diyanet sehir information!", e)
            }

            // ######################
            val ilcelerList = StringBuilder()

            try {
                val ilcelerResponseStatusType = retrieveAPIFeedback(
                    ilcelerList,
                    "$DIYANET_JSON_URL/ilceler/$sehirID",
                    EHttpRequestMethod.GET,
                    null
                )
                if (ilcelerResponseStatusType !== EHttpResponseStatusType.Success) {
                    return null
                }
                val ilcelerLstType = object : TypeToken<ArrayList<DiyanetIlceEntity?>?>() {}.type
                val ilcelerLst =
                    gson.fromJson<List<DiyanetIlceEntity>>(ilcelerList.toString(), ilcelerLstType)
                if (!ilcelerLst.stream()
                        .allMatch { x: DiyanetIlceEntity -> x.ilceID != null && x.ilceID != "" && x.ilceAdiEn != null && x.ilceAdiEn != "" }
                ) {
                    // WEIRD
                }
                for (ilceEntity in ilcelerLst) {
                    if (AppEnvironment.dbHelper!!.GetDiyanetIlceIDByName(ilceEntity.ilceAdiEn!!) == null) {
                        AppEnvironment.dbHelper!!.AddDiyanetIlce(sehirID, ilceEntity)
                    }
                    if (ilceEntity.ilceAdiEn == cityAddress.locality.toUpperCase()) {
                        ilceID = ilceEntity.ilceID
                        break
                    }
                }
                if (ilceID == null) {
                    return null
                }
            } catch (e: Exception) {
                throw Exception("Could not process Diyanet ilce information!", e)
            }
        }

        // ######################
        val vakitlerList = StringBuilder()

        var timesPackageEntity: DiyanetPrayerTimeDayEntity? = null

        try {
            val vakitlerResponseStatusType = retrieveAPIFeedback(
                vakitlerList,
                "$DIYANET_JSON_URL/vakitler/$ilceID",
                EHttpRequestMethod.GET,
                null
            )
            if (vakitlerResponseStatusType !== EHttpResponseStatusType.Success) {
                return null
            }
            val todayDate = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now())
            val listOfDiyanetPrayerTimeEntity =
                object : TypeToken<ArrayList<DiyanetPrayerTimeDayEntity?>?>() {}.type
            val outputList = gson.fromJson<List<DiyanetPrayerTimeDayEntity>>(
                vakitlerList.toString(),
                listOfDiyanetPrayerTimeEntity
            )
            var element =
                outputList.stream().filter { x: DiyanetPrayerTimeDayEntity -> todayDate == x.date }
                    .findFirst()

            //TODO: API schickt heutige Daten manchmal nicht
            if (!element.isPresent) {
                val tomorrowDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    .format(LocalDateTime.now().plusDays(1))
                element = outputList.stream()
                    .filter { x: DiyanetPrayerTimeDayEntity -> todayDate == x.date }
                    .findFirst()
            }
            if (element.isPresent) {
                timesPackageEntity = element.get()
            }
        } catch (e: Exception) {
            throw Exception("Could not process Diyanet vakit information!", e)
        }
        if (timesPackageEntity == null) {
            throw Exception(
                "Could not retrieve Diyanet prayer time information for an unknown reason!",
                null
            )
        }
        return timesPackageEntity
    }

    @Throws(Exception::class)
    fun RetrieveAlAdhanTimes(
        targetLocation: CustomLocation?,
        fajrDegree: Double?,
        ishaDegree: Double?,
        ishtibaqAngle: Double?
    ): AlAdhanPrayerTimeDayEntity? {

        if (targetLocation == null) {
            throw Exception(
                "Can not retrieve Diyanet prayer time data without a provided address!",
                null
            )
        }

        val alAdhanJSONList = StringBuilder()

        try {

            val fajrDegreeText = if(fajrDegree == null) { "null" } else { "" + abs(fajrDegree) }
            val ishaDegreeText = if(ishaDegree == null) { "null" } else { "" + abs(ishaDegree) }
            val ishtibaqDegreeText = if(ishtibaqAngle == null) { "null" } else { "" + abs(ishtibaqAngle) }

            val queryParameters = hashMapOf(
                "latitude" to targetLocation.latitude.toString(),
                "longitude" to targetLocation.longitude.toString(),
                "method" to "99",
                "methodSettings" to "$fajrDegreeText,$ishtibaqDegreeText,$ishaDegreeText",
                "month" to LocalDate.now().monthValue.toString(),
                "year" to LocalDate.now().year.toString()
            )

            val alAdhanResponseStatusType = retrieveAPIFeedback(
                alAdhanJSONList,
                ALADHAN_JSON_URL,
                EHttpRequestMethod.GET,
                queryParameters
            )

            if (alAdhanResponseStatusType !== EHttpResponseStatusType.Success) {
                return null
            }

            val jsonObject = JSONObject(alAdhanJSONList.toString())
            val arrayListJson = jsonObject.getJSONArray("data")
            val gson = BuildGSON("HH:mm")

            for (i in 0 until arrayListJson.length()) {

                val jsonObject = arrayListJson.getJSONObject(i)

                val timingsJSONObject = jsonObject.getJSONObject("timings")

                val dateString =
                    jsonObject
                        .getJSONObject("date")
                        .getJSONObject("gregorian")
                        .getString("date")

                val dateTimeFormat = "HH:mm dd-MM-yyyy"

                val fajrTime =              "${timingsJSONObject.getString("Fajr")      .substring(0, 5)} $dateString".parse(dateTimeFormat)
                val sunriseTime =           "${timingsJSONObject.getString("Sunrise")   .substring(0, 5)} $dateString".parse(dateTimeFormat)
                val dhuhrTime =             "${timingsJSONObject.getString("Dhuhr")     .substring(0, 5)} $dateString".parse(dateTimeFormat)
                val asrTime =               "${timingsJSONObject.getString("Asr")       .substring(0, 5)} $dateString".parse(dateTimeFormat)
                val maghribTime =           "${timingsJSONObject.getString("Sunset")    .substring(0, 5)} $dateString".parse(dateTimeFormat)
                val ishtibaqAnNujumTime =   "${timingsJSONObject.getString("Maghrib")   .substring(0, 5)} $dateString".parse(dateTimeFormat)
                val ishaTime =              "${timingsJSONObject.getString("Isha")      .substring(0, 5)} $dateString".parse(dateTimeFormat)

                if (fajrTime.toLocalDate().isEqual(LocalDate.now())) {

                    return AlAdhanPrayerTimeDayEntity(
                        fajrTime = fajrTime,
                        sunriseTime = sunriseTime,
                        dhuhrTime = dhuhrTime,
                        asrTime = asrTime,
                        mithlaynTime = null,
                        maghribTime = maghribTime,
                        ishtibaqAnNujumTime = ishtibaqAnNujumTime,
                        ishaTime = ishaTime
                    )

                }
            }
        } catch (e: Exception) {
            throw Exception("Could not process Diyanet ulke information!", e)
        }
        return null
    }

    @Throws(Exception::class)
    fun RetrieveMuwaqqitTimes(
        targetLocation: CustomLocation,
        fajrDegree: Double?,
        ishaDegree: Double?,
        karahaDegree: Double?
    ): MuwaqqitPrayerTimeDayEntity? {
        val todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now())

        // TODO: CHECK TODAY DATE AS WELL
        var storedMuwaqqitTime: MuwaqqitPrayerTimeDayEntity? = null
        if (false && karahaDegree == null) {
            storedMuwaqqitTime =
                AppEnvironment.dbHelper!!.GetMuwaqqitPrayerTimesByDateLocationAndDegrees(
                    todayDate,
                    targetLocation.longitude,
                    targetLocation.latitude,
                    fajrDegree,
                    ishaDegree
                )
        }
        if (storedMuwaqqitTime != null) {
            return storedMuwaqqitTime
        }

        val queryParameters = hashMapOf(
            "d" to todayDate,
            "ln" to targetLocation.longitude.toString(),
            "lt" to targetLocation.latitude.toString(),
            "tz" to targetLocation.timezone!!
        )

        if (fajrDegree != null) {
            queryParameters["fa"] = fajrDegree.toString()
        }

        if (ishaDegree != null) {
            queryParameters["ea"] = ishaDegree.toString()
        }

        if (karahaDegree != null) {
            queryParameters["ia"] = karahaDegree.toString()
        }

        var response = StringBuilder()
        var muwaqqitResponseStatusType = retrieveAPIFeedback(
            response,
            MUWAQQIT_JSON_URL,
            EHttpRequestMethod.POST,
            queryParameters
        )

        // Muwaqqit API requires 10 seconds cool down after every successful or unsuccessful request
        if (muwaqqitResponseStatusType === EHttpResponseStatusType.TooManyRequests
            ||
            response.toString().startsWith("429 TOO MANY REQUESTS")
        ) {
            try {
                TimeUnit.MILLISECONDS.sleep(MUWAQQIT_API_COOLDOWN_MILLISECONDS)
            } catch (e: Exception) { /* DO NOTHING */ }

            response = StringBuilder()

            muwaqqitResponseStatusType = retrieveAPIFeedback(
                response,
                MUWAQQIT_JSON_URL,
                EHttpRequestMethod.POST,
                queryParameters
            )
        }

        if (muwaqqitResponseStatusType !== EHttpResponseStatusType.Success) {
            return null
        }

        var timesPackageEntity: MuwaqqitPrayerTimeDayEntity? = null

        try {
            val gson = BuildGSON("HH:mm:ss")
            val outputList = gson.fromJson<List<MuwaqqitPrayerTimeDayEntity>>(
                JSONObject(response.toString()).getJSONArray("list").toString(),
                object : TypeToken<ArrayList<MuwaqqitPrayerTimeDayEntity?>?>() {}.type
            )
            AppEnvironment.dbHelper!!.DeleteAllMuwaqqitPrayerTimesByDegrees(fajrDegree, ishaDegree)

            for (time in outputList) {
                AppEnvironment.dbHelper!!.AddMuwaqqitPrayerTime(time, targetLocation)
                if (todayDate == time.date) {
                    timesPackageEntity = time
                }
            }

        } catch (e: Exception) {
            throw Exception("Could not process Muwaqqit prayer times for an unknown reason!", e)
        }

        if (timesPackageEntity == null) {
            throw Exception("Could not retrieve Muwaqqit prayer times for an unknown reason!", null)
        }

        return timesPackageEntity
    }

    @Throws(Exception::class)
    fun RetrieveTimeZoneByLocation(longitude: Double, latitude: Double): String? {

        val urlText = "$BING_MAPS_URL$latitude,$longitude"
        val parameters = hashMapOf(
            "key" to BuildConfig.BING_API_KEY
        )

        val response = StringBuilder()

        val timezoneResponseStatusType = retrieveAPIFeedback(response, urlText, EHttpRequestMethod.GET, parameters)

        if (timezoneResponseStatusType !== EHttpResponseStatusType.Success) {
            return null
        }

        val timezone: String = try {

            JSONObject(response.toString())
                .getJSONArray("resourceSets")
                .getJSONObject(0)
                .getJSONArray("resources")
                .getJSONObject(0)
                .getJSONObject("timeZone")
                .getString("ianaTimeZoneId")

        } catch (e: Exception) {
            throw Exception("Bing timezone response could not be processed!", e)
        }

        if (timezone == "") {
            throw Exception("Could not retrieve time zone for an unknown reason!", null)
        }

        return timezone
    }

    fun retrieveAPIFeedback(
        responseContent: StringBuilder,
        urlText: String?,
        requestMethod: EHttpRequestMethod,
        queryParameters: Map<String, String>?
    ): EHttpResponseStatusType {

        var urlText = urlText
        var conn: HttpURLConnection? = null

        var responseStatusType = EHttpResponseStatusType.None

        try {
            var line: String?

            if (requestMethod === EHttpRequestMethod.GET && queryParameters != null && queryParameters.isNotEmpty()) {
                var parameterPart = "?"

                for ((key, value) in queryParameters) {
                    parameterPart += "$key=$value&"
                }

                // remove & character at the end
                val sb = StringBuffer(parameterPart)
                sb.deleteCharAt(sb.length - 1)
                urlText += sb.toString()
            }

            val url = URL(urlText)
            conn = url.openConnection() as HttpURLConnection

            // Request setup
            conn.requestMethod = requestMethod.toString()
            conn.connectTimeout = 5000 // 5000 milliseconds = 5 seconds
            conn.readTimeout = 5000
            if (requestMethod === EHttpRequestMethod.POST) {

                val builder = Uri.Builder()

                for ((key, value) in queryParameters!!) {
                    builder.appendQueryParameter(key, value)
                }

                val query = builder.build().encodedQuery
                val os = conn.outputStream
                val writer = BufferedWriter(OutputStreamWriter(os, StandardCharsets.UTF_8))

                if (queryParameters.isNotEmpty()) {
                    writer.write(query)
                }

                writer.flush()
                writer.close()
                os.close()
            }

            val responseCode = conn.responseCode
            responseStatusType = getHttpResponseStatusTypeByResponseCode(responseCode)

            val reader: BufferedReader = if (responseStatusType !== EHttpResponseStatusType.Success) {
                BufferedReader(InputStreamReader(conn.errorStream))
            } else {
                BufferedReader(InputStreamReader(conn.inputStream))
            }

            while (reader.readLine().also { line = it } != null) {
                if (line != null) responseContent.append(line)
            }

            reader.close()

        } finally {
            conn?.disconnect()
        }

        return responseStatusType
    }

    fun getHttpResponseStatusTypeByResponseCode(statusCode: Int): EHttpResponseStatusType {

        if (statusCode > 299) {
            return if (statusCode == 429) EHttpResponseStatusType.TooManyRequests
                else EHttpResponseStatusType.UnknownError
        } else if (statusCode in 200..299) {
            return EHttpResponseStatusType.Success
        }

        return EHttpResponseStatusType.None
    }
}