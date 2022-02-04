package com.example.advancedprayertimes.logic.db

import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetUlkeEntity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetSehirEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetIlceEntity
import com.example.advancedprayertimes.logic.api_entities.MuwaqqitPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.CustomLocation
import com.google.gson.Gson
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import kotlin.Throws

class DBHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)
{
    // Die onCreate-Methode wird nur aufgerufen, falls die Datenbank noch nicht existiert
    override fun onCreate(db: SQLiteDatabase)
    {
        val muwaqqitTableSQLCreate = "CREATE TABLE $MUWAQQIT_PRAYER_TIME_TABLE " +
            "(" +
                "$idColumn INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$fajrTimeColumn TEXT NOT NULL, " +
                "$fajrDegreeColumn REAL NOT NULL, " +
                "$sunriseTimeColumn NOT NULL, " +
                "$dhuhrTimeColumn NOT NULL, " +
                "$asrMithlTimeColumn TEXT NOT NULL, " +
                "$maghribTimeColumn TEXT NOT NULL, " +
                "$ishaTimeColumn TEXT NOT NULL, " +
                "$ishaDegreeColumn REAL NOT NULL, " +
                "$dateColumn TEXT NOT NULL, " +
                "$longitudeColumn REAL NOT NULL, " +
                "$latitudeColumn REAL NOT NULL, " +
                "$insertDateMilliSecondsColumn INT NOT NULL" +
            ");"

        val diyanetTableSQLCreate = "CREATE TABLE $DIYANET_PRAYER_TIME_TABLE " +
            "(" +
                "$idColumn INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$fajrTimeColumn TEXT NOT NULL, " +
                "$sunriseTimeColumn TEXT NOT NULL, " +
                "$dhuhrTimeColumn TEXT NOT NULL, " +
                "$asrMithlTimeColumn TEXT NOT NULL, " +
                "$maghribTimeColumn TEXT NOT NULL, " +
                "$ishaTimeColumn TEXT NOT NULL, " +
                "$dateColumn TEXT NOT NULL, " +
                "$longitudeColumn REAL NOT NULL, " +
                "$latitudeColumn REAL NOT NULL, " +
                "$insertDateMilliSecondsColumn INT NOT NULL" +
            ");"

        val diyanetUlkeTableSQLCreate = "CREATE TABLE $DIYANET_ULKE_TABLE " +
            "(" +
                "$idColumn INTEGER PRIMARY KEY, " +
                "$nameColumn TEXT NOT NULL" +
            ");"

        val diyanetSehirTableSQLCreate = "CREATE TABLE $DIYANET_SEHIR_TABLE " +
            "(" +
                "$idColumn INTEGER PRIMARY KEY, " +
                "$parentIDColumn INT NOT NULL, " +
                "$nameColumn TEXT NOT NULL" +
            ");"

        val diyanetIlceTableSQLCreate = "CREATE TABLE $DIYANET_ILCE_TABLE " +
            "(" +
                "$idColumn INTEGER PRIMARY KEY, " +
                "$parentIDColumn INT NOT NULL, " +
                "$nameColumn TEXT NOT NULL" +
            ");"

        db.execSQL(muwaqqitTableSQLCreate)
        db.execSQL(diyanetTableSQLCreate)
        db.execSQL(diyanetUlkeTableSQLCreate)
        db.execSQL(diyanetSehirTableSQLCreate)
        db.execSQL(diyanetIlceTableSQLCreate)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    {

    }

    var dateFormat = DateTimeFormatter.ofPattern("HH:mm")

    // ######################################################
    // ######################################################
    // ################ DIYANET EXTRA TABLES ###############
    // ######################################################
    // ######################################################
    fun AddDiyanetUlke(ulke: DiyanetUlkeEntity): Boolean
    {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(idColumn, ulke.ulkeID)
        cv.put(nameColumn, ulke.ulkeAdiEn)
        val returnValue = db.insert(DIYANET_ULKE_TABLE, null, cv) != -1L
        db.close()
        return returnValue
    }

    fun AddDiyanetSehir(parentID: String?, sehirEntity: DiyanetSehirEntity): Boolean
    {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(idColumn, sehirEntity.sehirID)
        cv.put(parentIDColumn, parentID)
        cv.put(nameColumn, sehirEntity.sehirAdiEn)
        val returnValue = db.insert(DIYANET_SEHIR_TABLE, null, cv) != -1L
        db.close()
        return returnValue
    }

    fun AddDiyanetIlce(parentID: String?, ilceEntity: DiyanetIlceEntity): Boolean
    {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(idColumn, ilceEntity.ilceID)
        cv.put(parentIDColumn, parentID)
        cv.put(nameColumn, ilceEntity.ilceAdiEn)
        val returnValue = db.insert(DIYANET_ILCE_TABLE, null, cv) != -1L
        db.close()
        return returnValue
    }

    fun GetDiyanetUlkeIDByName(name: String): String?
    {
        val queryString =
            "SELECT $idColumn FROM $DIYANET_ULKE_TABLE WHERE $nameColumn = '$name'"

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)
        var returnID: String? = null

        // true if there are any results
        if (cursor.moveToFirst()) {
            returnID = "" + cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return returnID
    }

    fun GetDiyanetSehirIDByName(name: String): String?
    {
        val queryString =
            "SELECT $idColumn FROM $DIYANET_SEHIR_TABLE WHERE $nameColumn = '$name'"

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)
        var returnID: String? = null

        // true if there are any results
        if (cursor.moveToFirst()) {
            returnID = "" + cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return returnID
    }

    fun GetDiyanetIlceIDByName(name: String): String?
    {
        val queryString =
            "SELECT $idColumn FROM $DIYANET_ILCE_TABLE WHERE $nameColumn = '$name'"

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)
        var returnID: String? = null

        // true if there are any results
        if (cursor.moveToFirst()) {
            returnID = "" + cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return returnID
    }

    fun GetDiyanetIlceIDByCountryAndCityName(countryName: String, cityName: String): String?
    {
        val queryString =
            "SELECT $DIYANET_ILCE_TABLE .$idColumn FROM $DIYANET_ULKE_TABLE" +
            " INNER JOIN $DIYANET_SEHIR_TABLE ON $DIYANET_SEHIR_TABLE.$parentIDColumn = $DIYANET_ULKE_TABLE.$idColumn" +
            " INNER JOIN $DIYANET_ILCE_TABLE ON $DIYANET_ILCE_TABLE.$parentIDColumn = $DIYANET_SEHIR_TABLE.$idColumn" +
            " WHERE $DIYANET_ULKE_TABLE.$nameColumn  = '$countryName' AND $DIYANET_ILCE_TABLE.$nameColumn = '$cityName'"

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)
        var returnID: String? = null

        // true if there are any results
        if (cursor.moveToFirst()) {
            returnID = "" + cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return returnID
    }

    "SELECT $DIYANET_ILCE_TABLE.$idColumn FROM $DIYANET_ULKE_TABLE" +
    " INNER JOIN $DIYANET_SEHIR_TABLE ON $DIYANET_SEHIR_TABLE.$parentIDColumn = $DIYANET_ULKE_TABLE.$idColumn" +
    " INNER JOIN $DIYANET_ILCE_TABLE ON $DIYANET_ILCE_TABLE.$parentIDColumn = $DIYANET_SEHIR_TABLE.$idColumn" +
    " WHERE $DIYANET_ULKE_TABLE.$nameColumn  = '$countryName' AND $DIYANET_ILCE_TABLE.$nameColumn = '$cityName'"


    // ######################################################
    // ######################################################
    // ############## MUWAQQIT_PRAYER_TIME_TABLE ############
    // ######################################################
    // ######################################################
    fun AddMuwaqqitPrayerTime(
        muwaqqitPrayerTimeDayEntity: MuwaqqitPrayerTimeDayEntity,
        location: CustomLocation
    ): Boolean
    {
        val gson = Gson()
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(fajrTimeColumn, muwaqqitPrayerTimeDayEntity.fajrTime!!.format(dateFormat))
        cv.put(fajrDegreeColumn, muwaqqitPrayerTimeDayEntity.fajrAngle)
        cv.put(sunriseTimeColumn, muwaqqitPrayerTimeDayEntity.sunriseTime!!.format(dateFormat))
        cv.put(dhuhrTimeColumn, muwaqqitPrayerTimeDayEntity.dhuhrTime!!.format(dateFormat))
        cv.put(asrMithlTimeColumn, muwaqqitPrayerTimeDayEntity.asrTime!!.format(dateFormat))
        cv.put(maghribTimeColumn, muwaqqitPrayerTimeDayEntity.maghribTime!!.format(dateFormat))
        cv.put(ishaTimeColumn, muwaqqitPrayerTimeDayEntity.ishaTime!!.format(dateFormat))
        cv.put(ishaDegreeColumn, muwaqqitPrayerTimeDayEntity.ishaAngle)
        cv.put(dateColumn, muwaqqitPrayerTimeDayEntity.date)
        cv.put(longitudeColumn, location.longitude)
        cv.put(latitudeColumn, location.latitude)
        cv.put(insertDateMilliSecondsColumn, System.currentTimeMillis())
        val returnValue = db.insert(MUWAQQIT_PRAYER_TIME_TABLE, null, cv) != -1L
        db.close()
        return returnValue
    }

    fun GetMuwaqqitPrayerTimesByDateLocationAndDegrees(
        todayDateString: String,
        longitude: Double,
        latitude: Double,
        fajrDegree: Double?,
        ishaDegree: Double?
    ): MuwaqqitPrayerTimeDayEntity?
    {
        var queryString =
            "SELECT * FROM $MUWAQQIT_PRAYER_TIME_TABLE" +
                    " WHERE " +
                    "$longitudeColumn = $longitude AND $latitudeColumn = $latitude" +
                    " AND $dateColumn = '$todayDateString'"

        if (fajrDegree != null)
        {
            queryString += " AND $fajrDegreeColumn = $fajrDegree"
        }
        if (ishaDegree != null)
        {
            queryString += " AND $ishaDegreeColumn = $ishaDegree"
        }

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)
        var targetTime: MuwaqqitPrayerTimeDayEntity? = null

        // true if there are any results
        if (cursor.moveToFirst()) {
            try {
                targetTime = getFromCursor(cursor)
            } catch (e: Exception) {
                e.printStackTrace()
                // DO NOTHING
            }
        }
        cursor.close()
        db.close()
        return targetTime
    }

    fun GetMuwaqqitPrayerTimesByLocation(
        longitude: Double,
        latitude: Double
    ): List<MuwaqqitPrayerTimeDayEntity> {
        val returnList: MutableList<MuwaqqitPrayerTimeDayEntity> = ArrayList()
        val queryString =
            "SELECT * FROM $MUWAQQIT_PRAYER_TIME_TABLE" +
                 " WHERE $longitudeColumn = $longitude" +
                 " AND $latitudeColumn = $latitude"

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)

        // true if there are any results
        if (cursor.moveToFirst())
        {
            do
            {
                try
                {
                    returnList.add(getFromCursor(cursor))
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return returnList
    }

    fun ExistsMuwaqqitPrayerTimesByLocationAndDegrees(
        longitude: Double,
        latitude: Double,
        fajrDegree: Double?,
        ishaDegree: Double?
    ): Boolean
    {
        var queryString =
            "SELECT ID FROM $MUWAQQIT_PRAYER_TIME_TABLE" +
                 " WHERE $longitudeColumn = $longitude" +
                 " AND $latitudeColumn = $latitude"

        if (fajrDegree != null)
        {
            queryString += " AND $fajrDegreeColumn = $fajrDegree"
        }
        if (ishaDegree != null)
        {
            queryString += " AND $ishaDegreeColumn = $ishaDegree"
        }

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)
        val returnValue = cursor.count > 0
        cursor.close()
        db.close()
        return returnValue
    }

    fun DeleteAllMuwaqqitPrayerTimes()
    {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE")
        db.close()
    }

    fun DeleteMuwaqqitPrayerTimesBelowCertainInsertDate(insertDateMilliSeconds: Long)
    {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE WHERE $insertDateMilliSecondsColumn < $insertDateMilliSeconds")
        db.close()
    }

    fun DeleteAllMuwaqqitPrayerTimesByDegrees(fajrDegree: Double?, ishaDegree: Double?)
    {
        val db = this.writableDatabase
        var sql = "DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE"

        if (fajrDegree != null || ishaDegree != null)
        {
            sql += " WHERE "
            if (fajrDegree != null)
            {
                sql += "$fajrDegreeColumn = $fajrDegree"
            }

            if (fajrDegree != null && ishaDegree != null)
            {
                sql += " AND "
            }

            if (ishaDegree != null)
            {
                sql += "$ishaDegreeColumn  = $ishaDegree"
            }
        }
        db.execSQL(sql)
        db.close()
    }

    @Throws(Exception::class)
    private fun getFromCursor(cursor: Cursor): MuwaqqitPrayerTimeDayEntity
    {
        var fajrTimeIndex = cursor.getColumnIndex(fajrTimeColumn)
        if (fajrTimeIndex < 0) fajrTimeIndex = 0
        val fajrTime = LocalDateTime.parse(cursor.getString(fajrTimeIndex), dateFormat)

        var sunriseTimeColumnIndex = cursor.getColumnIndex(sunriseTimeColumn)
        if (sunriseTimeColumnIndex < 0) sunriseTimeColumnIndex = 0
        val sunriseTime = LocalDateTime.parse(cursor.getString(sunriseTimeColumnIndex), dateFormat)

        var dhuhrTimeColumnIndex = cursor.getColumnIndex(dhuhrTimeColumn)
        if (dhuhrTimeColumnIndex < 0) dhuhrTimeColumnIndex = 0
        val dhuhrTime = LocalDateTime.parse(cursor.getString(dhuhrTimeColumnIndex), dateFormat)

        var asrTimeColumnIndex = cursor.getColumnIndex(asrMithlTimeColumn)
        if (asrTimeColumnIndex < 0) asrTimeColumnIndex = 0
        val asrTime = LocalDateTime.parse(cursor.getString(asrTimeColumnIndex), dateFormat)

        var maghribTimeColumnIndex = cursor.getColumnIndex(maghribTimeColumn)
        if (maghribTimeColumnIndex < 0) maghribTimeColumnIndex = 0
        val maghribTime = LocalDateTime.parse(cursor.getString(maghribTimeColumnIndex), dateFormat)

        var ishaTimeColumnIndex = cursor.getColumnIndex(ishaTimeColumn)
        if (ishaTimeColumnIndex < 0) ishaTimeColumnIndex = 0
        val ishaTime = LocalDateTime.parse(cursor.getString(ishaTimeColumnIndex), dateFormat)

        var dateColumnIndex = cursor.getColumnIndex(dateColumn)
        if (dateColumnIndex < 0) dateColumnIndex = 0
        val dateTime = cursor.getString(dateColumnIndex)

        return MuwaqqitPrayerTimeDayEntity(
            fajrTime,
            sunriseTime,
            null,
            dhuhrTime,
            asrTime,
            null,
            maghribTime,
            ishaTime,
            dateTime
        )
    }

    companion object
    {
        private val LOG_TAG = DBHelper::class.java.simpleName

        // TODO: GPS-POSITION NICHT VERGESSEN!!!!
        private const val DB_NAME = "AdvancedPrayerTime"
        private const val DB_VERSION = 1
        private const val MUWAQQIT_PRAYER_TIME_TABLE = "MUWAQQITPRAYERTIMEDAY"
        private const val DIYANET_PRAYER_TIME_TABLE = "DIYANETPRAYERTIMEDAY"
        private const val DIYANET_ULKE_TABLE = "DIYANET_ULKE_TABLE"
        private const val DIYANET_SEHIR_TABLE = "DIYANET_SEHIR_TABLE"
        private const val DIYANET_ILCE_TABLE = "DIYANET_ILCE_TABLE"
        private const val parentIDColumn = "PARENTID"
        private const val idColumn = "ID"
        private const val nameColumn = "NAME"
        private const val fajrTimeColumn = "FAJR_TIME"
        private const val fajrDegreeColumn = "FAJR_DEGREE"
        private const val sunriseTimeColumn = "SUNRISE_TIME"
        private const val dhuhrTimeColumn = "DHUHR_TIME"
        private const val asrMithlTimeColumn = "ASR_MITHL_TIME"
        private const val maghribTimeColumn = "MAGHRIB_TIME"
        private const val ishaTimeColumn = "ISHA_TIME"
        private const val ishaDegreeColumn = "ISHA_DEGREE"
        private const val dateColumn = "DATE"
        private const val longitudeColumn = "LONGITUDE"
        private const val latitudeColumn = "LATITUDE"
        private const val insertDateMilliSecondsColumn = "INSERTDATEMILLISECONDS"
    }

    init {
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: $databaseName erzeugt.")
    }
}