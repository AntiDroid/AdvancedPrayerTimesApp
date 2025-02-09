package com.example.advancedprayertimes.logic.util

import android.location.Address
import android.net.Uri
import com.example.advancedprayertimes.BuildConfig
import com.example.advancedprayertimes.logic.AppEnvironment.buildGSON
import com.example.advancedprayertimes.logic.CustomLocation
import com.example.advancedprayertimes.logic.CustomPlaceEntity
import com.example.advancedprayertimes.logic.api_entities.AlAdhanPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.api_entities.DiyanetPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.api_entities.MuwaqqitPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.AbstractDiyanetSubEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetIlceEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetSehirEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetUlkeEntity
import com.example.advancedprayertimes.logic.db.DBAlAdhanHelper
import com.example.advancedprayertimes.logic.db.DBDiyanetHelper
import com.example.advancedprayertimes.logic.db.DBMuwaqqitHelper
import com.example.advancedprayertimes.logic.enums.EHttpRequestMethod
import com.example.advancedprayertimes.logic.enums.EHttpResponseStatusType
import com.example.advancedprayertimes.logic.extensions.parseToDate
import com.example.advancedprayertimes.logic.extensions.parseToTime
import com.example.advancedprayertimes.logic.extensions.toStringByFormat
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object HttpRequestUtil {

    private const val MUWAQQIT_JSON_URL = "https://www.muwaqqit.com/api.json"
    private const val DIYANET_JSON_URL = "https://ezanvakti.herokuapp.com"
    private const val ALADHAN_JSON_URL = "https://api.aladhan.com/v1/calendar"
    private const val BING_MAPS_URL = "https://dev.virtualearth.net/REST/v1/timezone/"
    private const val GOOGLE_PLACES_URL = "https://maps.googleapis.com/maps/api/place/details/json"

    private const val MUWAQQIT_API_COOLDOWN_MILLISECONDS: Long = 12000

    // region Diyanet

    private inline fun<reified T : AbstractDiyanetSubEntity> retrieveDiyanetSubEntitiy(gson: Gson, urlText: String, compValue: String, parentID: String?) : String? {

        val stringJSONList = StringBuilder()

        if (EHttpResponseStatusType.Success !== retrieveAPIFeedback(
                responseContent = stringJSONList,
                urlText = urlText,
                requestMethod = EHttpRequestMethod.GET,
                queryParameters = null
            )
        ) {
            return null
        }

        val typedResponseLst = gson.fromJson<List<T>>(
            stringJSONList.toString(),
            object : TypeToken<ArrayList<T?>?>() {}.type
        )

        if (typedResponseLst.any { x -> x.id.isNullOrBlank() && x.nameEn.isNullOrBlank() }) {
            throw Exception(
                "Diyanet data was received incomplete!",
                null
            )
        }

        for (diyanetSubEntity in typedResponseLst) {

            DBDiyanetHelper.createDiyanetSubEntityIfNotExist(
                diyanetSubEntity = diyanetSubEntity,
                parentID = parentID
            )

            if (diyanetSubEntity.nameEn == compValue) {
                return diyanetSubEntity.id
            }
        }

        return null
    }

    private fun retrieveDiyanetIlce(gson: Gson, cityAddress: Address) : String?
    {
        var targetUlkeID: String? = null

        try {
            targetUlkeID = retrieveDiyanetSubEntitiy<DiyanetUlkeEntity>(
                gson = gson,
                urlText = "$DIYANET_JSON_URL/ulkeler",
                compValue = cityAddress.countryName.uppercase(),
                parentID = null
            )

            if (targetUlkeID == null) { return null }

        } catch (e: Exception) {
            throw Exception("Could not process Diyanet ulke information!", e)
        }

        var sehirID: String? = null

        try {
            sehirID = retrieveDiyanetSubEntitiy<DiyanetSehirEntity>(
                gson = gson,
                urlText = "$DIYANET_JSON_URL/sehirler/$targetUlkeID",
                compValue = cityAddress.countryName.uppercase(),
                parentID = targetUlkeID
            )

            if (sehirID == null)  { return null }

        } catch (e: Exception) {
            throw Exception("Could not process Diyanet sehir information!", e)
        }

        try {
            return retrieveDiyanetSubEntitiy<DiyanetIlceEntity>(
                gson = gson,
                urlText = "$DIYANET_JSON_URL/ilceler/$sehirID",
                compValue = cityAddress.locality.uppercase(),
                parentID = sehirID
            )
        } catch (e: Exception) {
            throw Exception("Could not process Diyanet ilce information!", e)
        }
    }

    // Der JSON enthält direkt in der ersten Ebene die Gebetszeiteninformationen für alle Tage des jeweiligen Monats.
    fun retrieveDiyanetTimes(cityAddress: Address, useCaching: Boolean): DiyanetPrayerTimeDayEntity? {

        val gson = buildGSON(
            timeFormatString = "HH:mm",
            dateFormatString = "dd.MM.yyyy",
            dateTimeFormatString = ""
        )
        var ilceID: String? = null

        if(useCaching)
        {
            ilceID = DBDiyanetHelper.GetDiyanetIlceIDByCountryAndCityName(
                cityAddress.countryName.uppercase(),
                cityAddress.locality.uppercase()
            )
        }

        if(ilceID == null)
        {
            ilceID = retrieveDiyanetIlce(gson, cityAddress)
        }

        if(ilceID == null)
        {
            throw Exception(
                "Could not find Diyanet ilce information!",
                null
            )
        }

        val todayDate = LocalDateTime.now().toStringByFormat("dd.MM.yyyy")

        // ######################
        val vakitlerList = StringBuilder()
        var timesPackageEntity: DiyanetPrayerTimeDayEntity? = null

        //timesPackageEntity = DBDiyanetHelper.getDiyanetPrayerTimesByDateLocation(todayDate, cityAddress.longitude, cityAddress.latitude)

        try {

            if (EHttpResponseStatusType.Success !== retrieveAPIFeedback(
                responseContent = vakitlerList,
                urlText = "$DIYANET_JSON_URL/vakitler/$ilceID",
                requestMethod = EHttpRequestMethod.GET,
                queryParameters = null
            )
            ) {
                return null
            }

            // String json, Type typeOfT
            val outputList = gson.fromJson<List<DiyanetPrayerTimeDayEntity>>(
                vakitlerList.toString(),
                (object : TypeToken<ArrayList<DiyanetPrayerTimeDayEntity?>?>() {}.type)
            )

            for (diyanetSubEntity in outputList) {

                DBDiyanetHelper.createDiyanetTimeIfNotExist(diyanetSubEntity, cityAddress.longitude, cityAddress.latitude)

                if (todayDate == diyanetSubEntity.date!!.toStringByFormat("dd.MM.yyyy")) {
                    timesPackageEntity = diyanetSubEntity
                }
            }

            //TODO: Diyanet API schickt manchmal erst Daten für spätere Tage
            if (timesPackageEntity == null) {
                val tomorrowDate = LocalDateTime.now().plusDays(1).toStringByFormat("dd.MM.yyyy")
                timesPackageEntity = outputList.firstOrNull { x -> tomorrowDate == x.date!!.toStringByFormat("dd.MM.yyyy") }
            }

        } catch (e: Exception) {
            throw Exception(
                "Could not process Diyanet vakit information!",
                e
            )
        }

        if (timesPackageEntity == null) {
            throw Exception(
                "Could not retrieve Diyanet prayer time information for an unknown reason!",
                null
            )
        }

        return timesPackageEntity
    }

    // endregion Diyanet

    // region AlAdhan

    fun retrieveAlAdhanTimes(
        targetLocation: CustomLocation?,
        fajrDegree: Double?,
        ishaDegree: Double?,
        ishtibaqDegree: Double?,
        useCaching: Boolean
    ): AlAdhanPrayerTimeDayEntity? {

        if (targetLocation == null) {
            throw Exception(
                "Can not retrieve Diyanet prayer time data without a provided address!",
                null
            )
        }

        val alAdhanJSONList = StringBuilder()

        var returnTime: AlAdhanPrayerTimeDayEntity? = null

        // TODO: Caching is not working as expected
        if(useCaching) {
            returnTime = DBAlAdhanHelper.getAlAdhanPrayerTimesByDateLocationAndDegrees(
                LocalDate.now().toStringByFormat("dd-MM-yyyy"),
                targetLocation.longitude,
                targetLocation.latitude,
                fajrDegree,
                ishaDegree,
                ishtibaqDegree
            )
        }

        if(returnTime != null) {
            return returnTime
        }

        try {

            val fajrDegreeText = if(fajrDegree == null) { "null" } else { "" + abs(fajrDegree) }
            val ishaDegreeText = if(ishaDegree == null) { "null" } else { "" + abs(ishaDegree) }
            val ishtibaqDegreeText = if(ishtibaqDegree == null) { "null" } else { "" + abs(ishtibaqDegree) }

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

            for (i in 0 until arrayListJson.length()) {

                val curJsonObject = arrayListJson.getJSONObject(i)

                val timingsJSONObject = curJsonObject.getJSONObject("timings")

                val date =
                    curJsonObject
                        .getJSONObject("date")
                        .getJSONObject("gregorian")
                        .getString("date")
                        .parseToDate("dd-MM-yyyy")

                val stuff =
                    curJsonObject
                        .getJSONObject("meta")
                        .getJSONObject("method")
                        .getJSONObject("params")

                val fajrAngle:Double? = if(stuff.getString("Fajr") != "null") stuff.getDouble("Fajr") * (-1) else null
                val ishaAngle: Double? = if(stuff.getString("Isha") != "null") stuff.getDouble("Isha") * (-1) else null
                val ishtibaqAngle: Double? = if(stuff.getString("Maghrib") != "null") stuff.getDouble("Maghrib") * (-1) else null

                val dateTimeFormat = "HH:mm"

                val fajrTime =              timingsJSONObject.getString("Fajr")      .substring(0, 5).parseToTime(dateTimeFormat)
                val sunriseTime =           timingsJSONObject.getString("Sunrise")   .substring(0, 5).parseToTime(dateTimeFormat)
                val dhuhrTime =             timingsJSONObject.getString("Dhuhr")     .substring(0, 5).parseToTime(dateTimeFormat)
                val asrTime =               timingsJSONObject.getString("Asr")       .substring(0, 5).parseToTime(dateTimeFormat)
                val maghribTime =           timingsJSONObject.getString("Sunset")    .substring(0, 5).parseToTime(dateTimeFormat)
                val ishtibaqAnNujumTime =   timingsJSONObject.getString("Maghrib")   .substring(0, 5).parseToTime(dateTimeFormat)
                val ishaTime =              timingsJSONObject.getString("Isha")      .substring(0, 5).parseToTime(dateTimeFormat)

                val time = AlAdhanPrayerTimeDayEntity(
                    date = date,
                    fajrTime = fajrTime,
                    sunriseTime = sunriseTime,
                    dhuhrTime = dhuhrTime,
                    asrTime = asrTime,
                    mithlaynTime = null,
                    maghribTime = maghribTime,
                    ishtibaqAnNujumTime = ishtibaqAnNujumTime,
                    ishaTime = ishaTime,
                    fajrAngle = fajrAngle,
                    ishaAngle = ishaAngle,
                    ishtibaqAngle = ishtibaqAngle,
                )

                DBAlAdhanHelper.createAlAdhanPrayerTimesByDateLocationAndDegreesIfNotExist(time, targetLocation)

                if (date == LocalDate.now()) {
                    returnTime = time
                }
            }
        } catch (e: Exception) {
            throw Exception("Could not process Diyanet ulke information!", e)
        }

        return returnTime
    }

    // endregion AlAdhan

    // region Muwaqqit

    fun retrieveMuwaqqitTimes(
        targetLocation: CustomLocation,
        fajrDegree: Double?,
        ishaDegree: Double?,
        karahaDegree: Double?,
        useCaching: Boolean
    ): MuwaqqitPrayerTimeDayEntity? {

        // TODO: CHECK TODAY DATE AS WELL
        if (useCaching) {

            val storedMuwaqqitTime =
                DBMuwaqqitHelper.getMuwaqqitPrayerTimesByDateLocationAndDegrees(
                    todayDateString = LocalDate.now().toStringByFormat("dd-MM-yyyy"),
                    longitude = targetLocation.longitude,
                    latitude = targetLocation.latitude,
                    fajrDegree = fajrDegree,
                    ishaDegree = ishaDegree,
                    karahaDegree = karahaDegree
                )

            if (storedMuwaqqitTime != null) {
                return storedMuwaqqitTime
            }
        }

        val todayDate = LocalDate.now().toStringByFormat("yyyy-MM-dd")

        val queryParameters = hashMapOf(
            "d" to todayDate,
            "ln" to targetLocation.longitude.toString(),
            "lt" to targetLocation.latitude.toString(),
            "tz" to targetLocation.timezone!!
        )

        if (fajrDegree != null) { queryParameters["fa"] = fajrDegree.toString() }
        if (ishaDegree != null) { queryParameters["ea"] = ishaDegree.toString() }
        if (karahaDegree != null) { queryParameters["ia"] = karahaDegree.toString() }

        var response = StringBuilder()
        var muwaqqitResponseStatusType = retrieveAPIFeedback(
            response,
            MUWAQQIT_JSON_URL,
            EHttpRequestMethod.GET,
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
                EHttpRequestMethod.GET,
                queryParameters
            )
        }

        if (muwaqqitResponseStatusType !== EHttpResponseStatusType.Success) {
            return null
        }

        var timesPackageEntity: MuwaqqitPrayerTimeDayEntity? = null

        try {
            val gson = buildGSON(
                timeFormatString = "HH:mm:ss",
                dateFormatString = "yyyy-MM-dd",
                dateTimeFormatString = ""
            )
            val outputList = gson.fromJson<List<MuwaqqitPrayerTimeDayEntity>>(
                JSONObject(response.toString()).getJSONArray("list").toString(),
                object : TypeToken<ArrayList<MuwaqqitPrayerTimeDayEntity?>?>() {}.type
            )

            for (time in outputList) {
                DBMuwaqqitHelper.createMuwaqqitPrayerTimesByDateLocationAndDegreesIfNotExist(time, targetLocation)
                if (todayDate == time.date!!.toStringByFormat("yyyy-MM-dd")) {
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

    // endregion Muwaqqit

    private fun getHttpResponseStatusTypeByResponseCode(statusCode: Int): EHttpResponseStatusType {

        if (statusCode > 299) {
            return if (statusCode == 429) EHttpResponseStatusType.TooManyRequests
            else EHttpResponseStatusType.UnknownError
        } else if (statusCode in 200..299) {
            return EHttpResponseStatusType.Success
        }

        return EHttpResponseStatusType.None
    }

    private fun findPlaceFromGoogle(placeID: String): CustomPlaceEntity? {

        val parameters = hashMapOf(
            "place_id" to placeID,
            "key" to BuildConfig.GP_API_KEY,
        )

        val response = java.lang.StringBuilder()

        var googlePlacesApiRequestStatus = EHttpResponseStatusType.None

        try
        {
            googlePlacesApiRequestStatus = HttpRequestUtil.retrieveAPIFeedback(
                response,
                GOOGLE_PLACES_URL,
                EHttpRequestMethod.GET,
                parameters
            )
        }
        catch (e: Exception)
        {
            // DO STUFF
        }

        if (googlePlacesApiRequestStatus != EHttpResponseStatusType.Success)
        {
            return null
        }

        val jsonBaseObj = JSONObject(response.toString())
        val jsonResultObj = jsonBaseObj.getJSONObject("result")
        val jsonGeometryObj = jsonResultObj.getJSONObject("geometry")
        val jsonLocationObj = jsonGeometryObj.getJSONObject("location")
        val name = jsonResultObj.getString("name")
        val longitude = jsonLocationObj.getDouble("lng")
        val latitude = jsonLocationObj.getDouble("lat")

        return CustomPlaceEntity(placeID, latitude, longitude, name)
    }

    fun retrieveTimeZoneByLocation(longitude: Double, latitude: Double): String? {

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
        urlText: String,
        requestMethod: EHttpRequestMethod,
        queryParameters: Map<String, String>?
    ): EHttpResponseStatusType {

        var adaptedUrlText = urlText
        var conn: HttpURLConnection? = null

        var responseStatusType = EHttpResponseStatusType.None

        try {
            var line: String?

            if (requestMethod === EHttpRequestMethod.GET && queryParameters != null && queryParameters.any()) {
                var parameterPart = "?"

                for ((key, value) in queryParameters) {
                    parameterPart += "$key=$value&"
                }

                // remove & character at the end
                val sb = StringBuffer(parameterPart)
                sb.deleteCharAt(sb.length - 1)
                adaptedUrlText += sb.toString()
            }

            val url = URL(adaptedUrlText)
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

                if (queryParameters.any()) {
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
}