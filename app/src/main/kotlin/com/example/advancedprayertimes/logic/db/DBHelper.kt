package com.example.advancedprayertimes.logic.db

import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetUlkeEntity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.advancedprayertimes.logic.AppEnvironment
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetSehirEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetIlceEntity
import com.example.advancedprayertimes.logic.api_entities.MuwaqqitPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.CustomLocation
import com.example.advancedprayertimes.logic.api_entities.diyanet.AbstractDiyanetSubEntity
import com.example.advancedprayertimes.logic.extensions.parseToTime
import com.example.advancedprayertimes.logic.extensions.toStringByFormat
import com.google.gson.Gson
import java.time.LocalDate
import java.util.ArrayList

class DBHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)
{
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
        private const val karahaDegreeColumn = "KARAHA_DEGREE"
        private const val dateColumn = "DATE"
        private const val longitudeColumn = "LONGITUDE"
        private const val latitudeColumn = "LATITUDE"
        private const val insertDateMilliSecondsColumn = "INSERTDATEMILLISECONDS"

        const val SQL_CREATE_MUWAQQIT_PRAYER_TIME_TABLE =
            "CREATE TABLE $MUWAQQIT_PRAYER_TIME_TABLE " +
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
                "$karahaDegreeColumn REAL NOT NULL, " +
                "$dateColumn TEXT NOT NULL, " +
                "$longitudeColumn REAL NOT NULL, " +
                "$latitudeColumn REAL NOT NULL, " +
                "$insertDateMilliSecondsColumn INT NOT NULL" +
                ");"

        const val SQL_CREATE_DIYANET_PRAYER_TIME_TABLE =
            "CREATE TABLE $DIYANET_PRAYER_TIME_TABLE " +
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

        const val SQL_CREATE_DIYANET_ULKE_TABLE =
            "CREATE TABLE $DIYANET_ULKE_TABLE " +
                "(" +
                "$idColumn INTEGER PRIMARY KEY, " +
                "$nameColumn TEXT NOT NULL" +
                ");"

        const val SQL_CREATE_DIYANET_SEHIR_TABLE =
            "CREATE TABLE $DIYANET_SEHIR_TABLE " +
                "(" +
                "$idColumn INTEGER PRIMARY KEY, " +
                "$parentIDColumn INT NOT NULL, " +
                "$nameColumn TEXT NOT NULL" +
                ");"

        const val SQL_CREATE_DIYANET_ILCE_TABLE =
            "CREATE TABLE $DIYANET_ILCE_TABLE " +
                "(" +
                "$idColumn INTEGER PRIMARY KEY, " +
                "$parentIDColumn INT NOT NULL, " +
                "$nameColumn TEXT NOT NULL" +
                ");"
    }

    // Die onCreate-Methode wird nur aufgerufen, falls die Datenbank noch nicht existiert
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_MUWAQQIT_PRAYER_TIME_TABLE)
        db.execSQL(SQL_CREATE_DIYANET_PRAYER_TIME_TABLE)
        db.execSQL(SQL_CREATE_DIYANET_ULKE_TABLE)
        db.execSQL(SQL_CREATE_DIYANET_SEHIR_TABLE)
        db.execSQL(SQL_CREATE_DIYANET_ILCE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    // ######################################################
    // ######################################################
    // ################ DIYANET EXTRA TABLES ###############
    // ######################################################
    // ######################################################
    fun AddDiyanetUlke(ulke: DiyanetUlkeEntity): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(idColumn, ulke.ulkeID)
        cv.put(nameColumn, ulke.ulkeNameEn)
        val returnValue = db.insert(DIYANET_ULKE_TABLE, null, cv) != -1L
        db.close()
        return returnValue
    }

    fun AddDiyanetSehir(parentID: String?, sehirEntity: DiyanetSehirEntity): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(idColumn, sehirEntity.sehirID)
        cv.put(parentIDColumn, parentID)
        cv.put(nameColumn, sehirEntity.sehirNameEn)
        val returnValue = db.insert(DIYANET_SEHIR_TABLE, null, cv) != -1L
        db.close()
        return returnValue
    }

    fun AddDiyanetIlce(parentID: String?, ilceEntity: DiyanetIlceEntity): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(idColumn, ilceEntity.ilceID)
        cv.put(parentIDColumn, parentID)
        cv.put(nameColumn, ilceEntity.ilceNameEn)
        val returnValue = db.insert(DIYANET_ILCE_TABLE, null, cv) != -1L
        db.close()
        return returnValue
    }

    private fun createDiyanetUlkeIfNotExist(diyanetUlkeEntity: DiyanetUlkeEntity) {
        if (AppEnvironment.dbHelper.getDiyanetUlkeIDByName(diyanetUlkeEntity.nameEn!!) == null) {
            AppEnvironment.dbHelper.AddDiyanetUlke(diyanetUlkeEntity)
        }
    }

    private fun createDiyanetIlceIfNotExist(parentID: String, diyanetIlceEntity: DiyanetIlceEntity) {
        if (AppEnvironment.dbHelper.GetDiyanetIlceIDByName(diyanetIlceEntity.nameEn!!) == null) {
            AppEnvironment.dbHelper.AddDiyanetIlce(parentID, diyanetIlceEntity)
        }
    }

    fun createDiyanetSubEntityIfNotExist(diyanetSubEntity: AbstractDiyanetSubEntity, parentID: String?) {

        if(diyanetSubEntity is DiyanetUlkeEntity) {
            createDiyanetUlkeIfNotExist(diyanetSubEntity as DiyanetUlkeEntity)
        }
        else if (diyanetSubEntity is DiyanetSehirEntity){
            return
        }
        else if (diyanetSubEntity is DiyanetIlceEntity) {

            if(parentID == null) {
                throw IllegalArgumentException()
            }

            createDiyanetIlceIfNotExist(parentID, diyanetSubEntity as DiyanetIlceEntity)
        } else {
            throw NotImplementedError()
        }
    }

    fun getDiyanetUlkeIDByName(name: String): String? {

        val queryString = "SELECT $idColumn FROM $DIYANET_ULKE_TABLE WHERE $nameColumn = '$name'"

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

    fun getDiyanetSehirIDByName(name: String): String?
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

    // ######################################################
    // ######################################################
    // ############## MUWAQQIT_PRAYER_TIME_TABLE ############
    // ######################################################
    // ######################################################
    fun addMuwaqqitPrayerTime(
        muwaqqitPrayerTimeDayEntity: MuwaqqitPrayerTimeDayEntity,
        location: CustomLocation
    ): Boolean
    {
        val gson = Gson()
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(fajrTimeColumn, muwaqqitPrayerTimeDayEntity.fajrTime!!.toStringByFormat("HH:mm"))
        cv.put(fajrDegreeColumn, muwaqqitPrayerTimeDayEntity.fajrAngle)
        cv.put(sunriseTimeColumn, muwaqqitPrayerTimeDayEntity.sunriseTime!!.toStringByFormat("HH:mm"))
        cv.put(dhuhrTimeColumn, muwaqqitPrayerTimeDayEntity.dhuhrTime!!.toStringByFormat("HH:mm"))
        cv.put(asrMithlTimeColumn, muwaqqitPrayerTimeDayEntity.asrTime!!.toStringByFormat("HH:mm"))
        cv.put(maghribTimeColumn, muwaqqitPrayerTimeDayEntity.maghribTime!!.toStringByFormat("HH:mm"))
        cv.put(ishaTimeColumn, muwaqqitPrayerTimeDayEntity.ishaTime!!.toStringByFormat("HH:mm"))
        cv.put(ishaDegreeColumn, muwaqqitPrayerTimeDayEntity.ishaAngle)
        cv.put(karahaDegreeColumn, muwaqqitPrayerTimeDayEntity.asrKarahaAngle)
        cv.put(dateColumn, muwaqqitPrayerTimeDayEntity.date)
        cv.put(longitudeColumn, location.longitude)
        cv.put(latitudeColumn, location.latitude)
        cv.put(insertDateMilliSecondsColumn, System.currentTimeMillis())
        val returnValue = db.insert(MUWAQQIT_PRAYER_TIME_TABLE, null, cv) != -1L
        db.close()
        return returnValue
    }

    fun getMuwaqqitPrayerTimesByDateLocationAndDegrees(
        todayDateString: String,
        longitude: Double,
        latitude: Double,
        fajrDegree: Double?,
        ishaDegree: Double?,
        karahaDegree: Double?
    ): MuwaqqitPrayerTimeDayEntity?
    {
        var queryString =
            "SELECT * FROM $MUWAQQIT_PRAYER_TIME_TABLE" +
                    " WHERE " +
                    "$longitudeColumn = $longitude AND $latitudeColumn = $latitude" +
                    " AND $dateColumn = '$todayDateString'"

        if (fajrDegree != null) {
            queryString += " AND $fajrDegreeColumn = $fajrDegree"
        }
        if (ishaDegree != null) {
            queryString += " AND $ishaDegreeColumn = $ishaDegree"
        }
        if (karahaDegree != null) {
            queryString += " AND $karahaDegreeColumn = $karahaDegree"
        }

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)
        var targetTime: MuwaqqitPrayerTimeDayEntity? = null

        // true if there are any results
        if (cursor.moveToFirst()) {
            targetTime = getMuwaqqitDataFromCursor(cursor)
        }

        cursor.close()
        db.close()
        return targetTime
    }

    fun getAllMuwaqqitPrayerTimes() : List<MuwaqqitPrayerTimeDayEntity> {

        val returnList: MutableList<MuwaqqitPrayerTimeDayEntity> = ArrayList()
        val queryString = "SELECT * FROM $MUWAQQIT_PRAYER_TIME_TABLE"

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)

        // true if there are any results
        if (cursor.moveToFirst())
        {
            do
            {
                returnList.add(getMuwaqqitDataFromCursor(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return returnList
    }

    fun getMuwaqqitPrayerTimesByLocation(
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
                returnList.add(getMuwaqqitDataFromCursor(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return returnList
    }

    fun existsMuwaqqitPrayerTimesByLocationAndDegrees(
        longitude: Double,
        latitude: Double,
        fajrDegree: Double?,
        ishaDegree: Double?,
        karahaDegree: Double?
    ): Boolean
    {
        var queryString =
            "SELECT ID FROM $MUWAQQIT_PRAYER_TIME_TABLE" +
                 " WHERE $longitudeColumn = $longitude" +
                 " AND $latitudeColumn = $latitude"

        if (fajrDegree != null) {
            queryString += " AND $fajrDegreeColumn = $fajrDegree"
        }
        if (ishaDegree != null) {
            queryString += " AND $ishaDegreeColumn = $ishaDegree"
        }
        if (karahaDegree != null) {
            queryString += " AND $karahaDegreeColumn = $karahaDegree"
        }

        // Writable database instances lock access for others
        val db = this.readableDatabase
        val cursor = db.rawQuery(queryString, null)
        val returnValue = cursor.count > 0
        cursor.close()
        db.close()
        return returnValue
    }

    fun deleteAllMuwaqqitPrayerTimes()
    {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE")
        db.close()
    }

    fun deleteMuwaqqitPrayerTimesBelowCertainInsertDate(insertDateMilliSeconds: Long)
    {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE WHERE $insertDateMilliSecondsColumn < $insertDateMilliSeconds")
        db.close()
    }

    fun deleteAllMuwaqqitPrayerTimesByDegrees(fajrDegree: Double?, ishaDegree: Double?, karahaDegree: Double?)
    {
        val db = this.writableDatabase
        var sql = "DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE"

        if (fajrDegree != null || ishaDegree != null)
        {
            sql += " WHERE "
            if (fajrDegree != null) {
                sql += "$fajrDegreeColumn = $fajrDegree"
            }

            if (fajrDegree != null && ishaDegree != null) {
                sql += " AND "
            }

            if (ishaDegree != null) {
                sql += "$ishaDegreeColumn  = $ishaDegree"
            }

            if (karahaDegree != null) {
                sql += "$karahaDegreeColumn  = $karahaDegree"
            }
        }
        db.execSQL(sql)
        db.close()
    }

    private fun getMuwaqqitDataFromCursor(cursor: Cursor): MuwaqqitPrayerTimeDayEntity {

        val fajrTimeColumnIndex = cursor.getColumnIndexOrThrow(fajrTimeColumn)
        val sunriseTimeColumnIndex = cursor.getColumnIndexOrThrow(dhuhrTimeColumn)

        val dhuhrTimeColumnIndex = cursor.getColumnIndexOrThrow(dhuhrTimeColumn)
        val asrTimeColumnIndex = cursor.getColumnIndexOrThrow(asrMithlTimeColumn)

        val maghribTimeColumnIndex = cursor.getColumnIndexOrThrow(maghribTimeColumn)
        val ishaTimeColumnIndex = cursor.getColumnIndexOrThrow(ishaTimeColumn)

        val dateColumnIndex = cursor.getColumnIndexOrThrow(dateColumn)

        return MuwaqqitPrayerTimeDayEntity(
            fajrTime = cursor.getString(fajrTimeColumnIndex).parseToTime("HH:mm").atDate(LocalDate.MAX),
            sunriseTime = cursor.getString(sunriseTimeColumnIndex).parseToTime("HH:mm").atDate(LocalDate.MAX),
            null,
            dhuhrTime = cursor.getString(dhuhrTimeColumnIndex).parseToTime("HH:mm").atDate(LocalDate.MAX),
            asrTime = cursor.getString(asrTimeColumnIndex).parseToTime("HH:mm").atDate(LocalDate.MAX),
            null,
            maghribTime = cursor.getString(maghribTimeColumnIndex).parseToTime("HH:mm").atDate(LocalDate.MAX),
            ishaTime = cursor.getString(ishaTimeColumnIndex).parseToTime("HH:mm").atDate(LocalDate.MAX),
            date = cursor.getString(dateColumnIndex)
        )
    }
}