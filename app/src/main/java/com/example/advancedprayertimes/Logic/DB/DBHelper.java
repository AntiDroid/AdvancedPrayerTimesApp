package com.example.advancedprayertimes.Logic.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet.DiyanetIlceEntity;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet.DiyanetSehirEntity;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet.DiyanetUlkeEntity;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.MuwaqqitPrayerTimeDayEntity;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    private static final String LOG_TAG = DBHelper.class.getSimpleName();

    // TODO: GPS-POSITION NICHT VERGESSEN!!!!

    private static final String DB_NAME = "AdvancedPrayerTime";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    private static final String MUWAQQIT_PRAYER_TIME_TABLE = "MUWAQQITPRAYERTIMEDAY";
    private static final String DIYANET_PRAYER_TIME_TABLE = "DIYANETPRAYERTIMEDAY";

    private static final String DIYANET_ULKE_TABLE = "DIYANET_ULKE_TABLE";
    private static final String DIYANET_SEHIR_TABLE = "DIYANET_SEHIR_TABLE";
    private static final String DIYANET_ILCE_TABLE = "DIYANET_ILCE_TABLE";

    private static final String parentIDColumn = "PARENTID";
    private static final String idColumn = "ID";
    private static final String nameColumn = "NAME";

    private static final String fajrTimeColumn = "FAJR_TIME";
    private static final String fajrDegreeColumn = "FAJR_DEGREE";
    private static final String sunriseTimeColumn = "SUNRISE_TIME";
    private static final String dhuhrTimeColumn = "DHUHR_TIME";
    private static final String asrMithlTimeColumn = "ASR_MITHL_TIME";
    private static final String maghribTimeColumn = "MAGHRIB_TIME";
    private static final String ishaTimeColumn = "ISHA_TIME";
    private static final String ishaDegreeColumn = "ISHA_DEGREE";

    private static final String dateColumn = "DATE";
    private static final String longitudeColumn = "LONGITUDE";
    private static final String latitudeColumn = "LATITUDE";
    private static final String insertDateMilliSecondsColumn = "INSERTDATEMILLISECONDS";

    // Die onCreate-Methode wird nur aufgerufen, falls die Datenbank noch nicht existiert
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String muwaqqitTableSQLCreate =
            "CREATE TABLE " + MUWAQQIT_PRAYER_TIME_TABLE +
                "(" +
                    idColumn + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                    fajrTimeColumn + " TEXT NOT NULL, " +
                    fajrDegreeColumn + " REAL NOT NULL, " +

                    sunriseTimeColumn + " NOT NULL, " +
                    dhuhrTimeColumn + " NOT NULL, " +
                    asrMithlTimeColumn + " TEXT NOT NULL, " +
                    maghribTimeColumn + " TEXT NOT NULL, " +

                    ishaTimeColumn + " TEXT NOT NULL, " +
                    ishaDegreeColumn + " REAL NOT NULL, " +

                    dateColumn + " TEXT NOT NULL, " +
                    longitudeColumn + " REAL NOT NULL, " +
                    latitudeColumn + " REAL NOT NULL, " +

                    insertDateMilliSecondsColumn + " INT NOT NULL" +
                ");";

        String diyanetTableSQLCreate =
            "CREATE TABLE " + DIYANET_PRAYER_TIME_TABLE +
                    "(" +
                    idColumn + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                    fajrTimeColumn + " TEXT NOT NULL, " +
                    sunriseTimeColumn + " TEXT NOT NULL, " +
                    dhuhrTimeColumn + " TEXT NOT NULL, " +
                    asrMithlTimeColumn + " TEXT NOT NULL, " +
                    maghribTimeColumn + " TEXT NOT NULL, " +
                    ishaTimeColumn + "TEXT NOT NULL, " +

                    dateColumn + " TEXT NOT NULL, " +
                    longitudeColumn + " REAL NOT NULL, " +
                    latitudeColumn + " REAL NOT NULL, " +

                    insertDateMilliSecondsColumn + " INT NOT NULL" +
                    ");";

        String diyanetUlkeTableSQLCreate =
                "CREATE TABLE " + DIYANET_ULKE_TABLE +
                        "(" +
                        idColumn + " INTEGER PRIMARY KEY, " +
                        nameColumn + " STRING NOT NULL);";

        String diyanetSehirTableSQLCreate =
                "CREATE TABLE " + DIYANET_SEHIR_TABLE +
                        "(" +
                        idColumn + " INTEGER PRIMARY KEY, " +
                        parentIDColumn + " INT NOT NULL, " +
                        nameColumn + " STRING NOT NULL);";

        String diyanetIlceTableSQLCreate =
                "CREATE TABLE " + DIYANET_ILCE_TABLE +
                        "(" +
                        idColumn + " INTEGER PRIMARY KEY, " +
                        parentIDColumn + " INT NOT NULL, " +
                        nameColumn + " STRING NOT NULL);";

        db.execSQL(muwaqqitTableSQLCreate);
        db.execSQL(diyanetTableSQLCreate);

        db.execSQL(diyanetUlkeTableSQLCreate);
        db.execSQL(diyanetSehirTableSQLCreate);
        db.execSQL(diyanetIlceTableSQLCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm");

    // ######################################################
    // ######################################################
    // ################ DIYANET EXTRA TABLES ###############
    // ######################################################
    // ######################################################

    public boolean AddDiyanetUlke(DiyanetUlkeEntity ulke)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(idColumn, ulke.getUlkeID());
        cv.put(nameColumn, ulke.getUlkeAdiEn());

        boolean returnValue = db.insert(DIYANET_ULKE_TABLE, null, cv) != -1;

        db.close();
        return returnValue;
    }

    public boolean AddDiyanetSehir(String parentID, DiyanetSehirEntity sehirEntity)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(idColumn, sehirEntity.getSehirID());
        cv.put(parentIDColumn, parentID);
        cv.put(nameColumn, sehirEntity.getSehirAdiEn());

        boolean returnValue = db.insert(DIYANET_SEHIR_TABLE, null, cv) != -1;

        db.close();
        return returnValue;
    }

    public boolean AddDiyanetIlce(String parentID, DiyanetIlceEntity ilceEntity)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(idColumn, ilceEntity.getIlceID());
        cv.put(parentIDColumn, parentID);
        cv.put(nameColumn, ilceEntity.getIlceAdiEn());

        boolean returnValue = db.insert(DIYANET_ILCE_TABLE, null, cv) != -1;

        db.close();
        return returnValue;
    }

    public String GetDiyanetUlkeIDByName(String name)
    {
        String queryString =
                "SELECT " + idColumn + " FROM " + DIYANET_ULKE_TABLE
                        + " WHERE " + nameColumn + " = '" + name + "'";

        // Writable database instances lock access for others
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        String returnID = null;

        // true if there are any results
        if(cursor.moveToFirst())
        {
            returnID = "" + cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return returnID;
    }

    public String GetDiyanetSehirIDByName(String name)
    {
        String queryString =
                "SELECT " + idColumn + " FROM " + DIYANET_SEHIR_TABLE
                        + " WHERE " + nameColumn + " = '" + name + "'";

        // Writable database instances lock access for others
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        String returnID = null;

        // true if there are any results
        if(cursor.moveToFirst())
        {
            returnID = "" + cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return returnID;
    }

    public String GetDiyanetIlceIDByName(String name)
    {
        String queryString =
                "SELECT " + idColumn + " FROM " + DIYANET_ILCE_TABLE
                        + " WHERE " + nameColumn + " = '" + name + "'";

        // Writable database instances lock access for others
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        String returnID = null;

        // true if there are any results
        if(cursor.moveToFirst())
        {
            returnID = "" + cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return returnID;
    }

    public String GetDiyanetIlceIDByCountryAndCityName(String countryName, String cityName)
    {
        String queryString =
                "SELECT " + DIYANET_ILCE_TABLE + "." + idColumn + " FROM " + DIYANET_ULKE_TABLE +
                " INNER JOIN " + DIYANET_SEHIR_TABLE + " ON " + DIYANET_SEHIR_TABLE + "." + parentIDColumn + " = " + DIYANET_ULKE_TABLE + "." + idColumn +
                " INNER JOIN " + DIYANET_ILCE_TABLE + " ON " + DIYANET_ILCE_TABLE + "." + parentIDColumn + " = " + DIYANET_SEHIR_TABLE + "." + idColumn +
                " WHERE " + DIYANET_ULKE_TABLE + "." + nameColumn + " = '" + countryName + "' AND " + DIYANET_ILCE_TABLE + "." + nameColumn + " = '" + cityName + "'";

        // Writable database instances lock access for others
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        String returnID = null;

        // true if there are any results
        if(cursor.moveToFirst())
        {
            returnID = "" + cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return returnID;
    }

    // ######################################################
    // ######################################################
    // ############## MUWAQQIT_PRAYER_TIME_TABLE ############
    // ######################################################
    // ######################################################

    public boolean AddMuwaqqitPrayerTime(MuwaqqitPrayerTimeDayEntity muwaqqitPrayerTimeDayEntity, Location location)
    {
        Gson gson = new Gson();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(fajrTimeColumn, muwaqqitPrayerTimeDayEntity.getFajrTime().format(dateFormat));
        cv.put(fajrDegreeColumn, muwaqqitPrayerTimeDayEntity.getFajrAngle());

        cv.put(sunriseTimeColumn, muwaqqitPrayerTimeDayEntity.getSunriseTime().format(dateFormat));
        cv.put(dhuhrTimeColumn, muwaqqitPrayerTimeDayEntity.getDhuhrTime().format(dateFormat));
        cv.put(asrMithlTimeColumn, muwaqqitPrayerTimeDayEntity.getAsrMithlTime().format(dateFormat));
        cv.put(maghribTimeColumn, muwaqqitPrayerTimeDayEntity.getMaghribTime().format(dateFormat));

        cv.put(ishaTimeColumn, muwaqqitPrayerTimeDayEntity.getIshaTime().format(dateFormat));
        cv.put(ishaDegreeColumn, muwaqqitPrayerTimeDayEntity.getIshaAngle());

        cv.put(dateColumn, muwaqqitPrayerTimeDayEntity.getFajrDate());
        cv.put(longitudeColumn, location.getLongitude());
        cv.put(latitudeColumn, location.getLatitude());
        cv.put(insertDateMilliSecondsColumn, System.currentTimeMillis());

        boolean returnValue = db.insert(MUWAQQIT_PRAYER_TIME_TABLE, null, cv) != -1;

        db.close();

        return returnValue;
    }

    public MuwaqqitPrayerTimeDayEntity GetMuwaqqitPrayerTimesByDateLocationAndDegrees(String todayDateString, double longitude, double latitude, Double fajrDegree, Double ishaDegree)
    {
        String queryString =
                "SELECT * FROM " + MUWAQQIT_PRAYER_TIME_TABLE
                        + " WHERE "
                        + longitudeColumn + " = " + longitude
                        + " AND "
                        + latitudeColumn + " = " + latitude
                        + " AND "
                        + dateColumn + " = '" + todayDateString + "'";

        if(fajrDegree != null)
        {
            queryString += " AND " + fajrDegreeColumn + " = " + fajrDegree;
        }

        if(ishaDegree != null)
        {
            queryString += " AND " + ishaDegreeColumn + " = " + ishaDegree;
        }

        // Writable database instances lock access for others
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        MuwaqqitPrayerTimeDayEntity targetTime = null;

        // true if there are any results
        if(cursor.moveToFirst())
        {
            try
            {
                targetTime = getFromCursor(cursor);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                // DO NOTHING
            }
        }

        cursor.close();
        db.close();
        return targetTime;
    }

    public List<MuwaqqitPrayerTimeDayEntity> GetMuwaqqitPrayerTimesByLocation(double longitude, double latitude)
    {
        List<MuwaqqitPrayerTimeDayEntity> returnList = new ArrayList<>();

        String queryString =
                "SELECT * FROM " + MUWAQQIT_PRAYER_TIME_TABLE
                        + " WHERE " + longitudeColumn + " = " + longitude
                        + " AND " + latitudeColumn + " = " + latitude;

        // Writable database instances lock access for others
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        // true if there are any results
        if(cursor.moveToFirst())
        {
            do
            {
                try
                {
                    returnList.add(getFromCursor(cursor));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            } while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return returnList;
    }

    public boolean ExistsMuwaqqitPrayerTimesByLocationAndDegrees(double longitude, double latitude, Double fajrDegree, Double ishaDegree)
    {
        String queryString =
                "SELECT ID FROM " + MUWAQQIT_PRAYER_TIME_TABLE
                        + " WHERE "
                        + longitudeColumn + " = " + longitude
                        + " AND "
                        + latitudeColumn + " = " + latitude;

        if(fajrDegree != null)
        {
            queryString += " AND " + fajrDegreeColumn + " = " + fajrDegree;
        }

        if(ishaDegree != null)
        {
            queryString += " AND " + ishaDegreeColumn + " = " + ishaDegree;
        }

        // Writable database instances lock access for others
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        boolean returnValue = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return returnValue;
    }

    public void DeleteAllMuwaqqitPrayerTimes()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ MUWAQQIT_PRAYER_TIME_TABLE);
        db.close();
    }

    public void DeleteMuwaqqitPrayerTimesBelowCertainInsertDate(long insertDateMilliSeconds)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ MUWAQQIT_PRAYER_TIME_TABLE + " WHERE " + insertDateMilliSecondsColumn + " < " + insertDateMilliSeconds);
        db.close();
    }

    public void DeleteAllMuwaqqitPrayerTimesByDegrees(Double fajrDegree,Double ishaDegree)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "DELETE FROM "+ MUWAQQIT_PRAYER_TIME_TABLE;

        if(fajrDegree != null || ishaDegree != null)
        {
            sql += " WHERE ";

            if(fajrDegree != null)
            {
                sql += fajrDegreeColumn + " = " + fajrDegree;
            }

            if(fajrDegree != null && ishaDegree != null)
            {
                sql += " AND ";
            }

            if(ishaDegree != null)
            {
                sql += ishaDegreeColumn + " = " + ishaDegree;
            }
        }

        db.execSQL(sql);
        db.close();
    }

    private MuwaqqitPrayerTimeDayEntity getFromCursor(Cursor cursor) throws Exception
    {
        int fajrTimeIndex = cursor.getColumnIndex(fajrTimeColumn);
        if(fajrTimeIndex < 0)
            fajrTimeIndex = 0;
        LocalDateTime fajrTime = LocalDateTime.parse(cursor.getString(fajrTimeIndex), dateFormat);

        int sunriseTimeColumnIndex = cursor.getColumnIndex(sunriseTimeColumn);
        if(sunriseTimeColumnIndex < 0)
            sunriseTimeColumnIndex = 0;
        LocalDateTime sunriseTime = LocalDateTime.parse(cursor.getString(sunriseTimeColumnIndex), dateFormat);

        int dhuhrTimeColumnIndex = cursor.getColumnIndex(dhuhrTimeColumn);
        if(dhuhrTimeColumnIndex < 0)
            dhuhrTimeColumnIndex = 0;
        LocalDateTime dhuhrTime = LocalDateTime.parse(cursor.getString(dhuhrTimeColumnIndex), dateFormat);

        int asrTimeColumnIndex = cursor.getColumnIndex(asrMithlTimeColumn);
        if(asrTimeColumnIndex < 0)
            asrTimeColumnIndex = 0;
        LocalDateTime asrTime = LocalDateTime.parse(cursor.getString(asrTimeColumnIndex), dateFormat);

        int maghribTimeColumnIndex = cursor.getColumnIndex(maghribTimeColumn);
        if(maghribTimeColumnIndex < 0)
            maghribTimeColumnIndex = 0;
        LocalDateTime maghribTime = LocalDateTime.parse(cursor.getString(maghribTimeColumnIndex), dateFormat);

        int ishaTimeColumnIndex = cursor.getColumnIndex(ishaTimeColumn);
        if(ishaTimeColumnIndex < 0)
            ishaTimeColumnIndex = 0;
        LocalDateTime ishaTime = LocalDateTime.parse(cursor.getString(ishaTimeColumnIndex), dateFormat);

        int dateColumnIndex = cursor.getColumnIndex(dateColumn);
        if(dateColumnIndex < 0)
            dateColumnIndex = 0;
        String dateTime = cursor.getString(dateColumnIndex);

        return new MuwaqqitPrayerTimeDayEntity(
                        fajrTime,
                        sunriseTime,
                        null,
                        dhuhrTime,
                        asrTime,
                        null,
                        maghribTime,
                        ishaTime,
                        dateTime
                );
    }
}
