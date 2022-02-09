package com.example.advancedprayertimes.logic.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

open class DBHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)
{
    companion object
    {
        public val LOG_TAG = DBHelper::class.java.simpleName

        // TODO: GPS-POSITION NICHT VERGESSEN!!!!
        public const val DB_NAME = "AdvancedPrayerTime"
        public const val DB_VERSION = 1
        public const val MUWAQQIT_PRAYER_TIME_TABLE = "MUWAQQITPRAYERTIMEDAY"
        public const val DIYANET_PRAYER_TIME_TABLE = "DIYANETPRAYERTIMEDAY"
        public const val ALADHAN_PRAYER_TIME_TABLE = "ALADHANPRAYERTIMEDAY"
        public const val DIYANET_ULKE_TABLE = "DIYANET_ULKE_TABLE"
        public const val DIYANET_SEHIR_TABLE = "DIYANET_SEHIR_TABLE"
        public const val DIYANET_ILCE_TABLE = "DIYANET_ILCE_TABLE"
        public const val parentIDColumn = "PARENTID"
        public const val idColumn = "ID"
        public const val nameColumn = "NAME"
        public const val fajrTimeColumn = "FAJR_TIME"
        public const val fajrDegreeColumn = "FAJR_DEGREE"
        public const val sunriseTimeColumn = "SUNRISE_TIME"
        public const val duhaTimeColumn = "DUHA_TIME"
        public const val dhuhrTimeColumn = "DHUHR_TIME"
        public const val asrMithlTimeColumn = "ASR_MITHL_TIME"
        public const val asrMithlaynTimeColumn = "ASR_MITHLAYN_TIME"
        public const val asrKarahaTimeColumn = "ASR_KARAHA_TIME"
        public const val maghribTimeColumn = "MAGHRIB_TIME"
        public const val ishaTimeColumn = "ISHA_TIME"
        public const val ishaDegreeColumn = "ISHA_DEGREE"
        public const val karahaDegreeColumn = "KARAHA_DEGREE"
        public const val ishtibaqDegreeColumn = "ISHTIBAQ_DEGREE"
        public const val dateColumn = "DATE"
        public const val longitudeColumn = "LONGITUDE"
        public const val latitudeColumn = "LATITUDE"
        public const val insertDateMilliSecondsColumn = "INSERTDATEMILLISECONDS"

        const val SQL_CREATE_MUWAQQIT_PRAYER_TIME_TABLE =
            "CREATE TABLE $MUWAQQIT_PRAYER_TIME_TABLE " +
                "(" +
                "$idColumn INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$fajrTimeColumn TEXT NOT NULL, " +
                "$sunriseTimeColumn NOT NULL, " +
                "$duhaTimeColumn NOT NULL, " +
                "$dhuhrTimeColumn NOT NULL, " +
                "$asrMithlTimeColumn TEXT NOT NULL, " +
                "$asrMithlaynTimeColumn TEXT NOT NULL, " +
                "$asrKarahaTimeColumn TEXT NOT NULL, " +
                "$maghribTimeColumn TEXT NOT NULL, " +
                "$ishaTimeColumn TEXT NOT NULL, " +

                "$fajrDegreeColumn REAL NOT NULL, " +
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

        const val SQL_CREATE_ALADHAN_PRAYER_TIME_TABLE =
            "CREATE TABLE $ALADHAN_PRAYER_TIME_TABLE " +
                "(" +
                "$idColumn INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$fajrTimeColumn TEXT NOT NULL, " +
                "$sunriseTimeColumn TEXT NOT NULL, " +
                "$dhuhrTimeColumn TEXT NOT NULL, " +
                "$asrMithlTimeColumn TEXT NOT NULL, " +
                "$maghribTimeColumn TEXT NOT NULL, " +
                "$ishaTimeColumn TEXT NOT NULL, " +

                "$fajrDegreeColumn REAL NULL, " +
                "$ishaDegreeColumn REAL NULL, " +
                "$ishtibaqDegreeColumn REAL NULL, " +
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
        db.execSQL(SQL_CREATE_ALADHAN_PRAYER_TIME_TABLE)
        db.execSQL(SQL_CREATE_DIYANET_PRAYER_TIME_TABLE)
        db.execSQL(SQL_CREATE_DIYANET_ULKE_TABLE)
        db.execSQL(SQL_CREATE_DIYANET_SEHIR_TABLE)
        db.execSQL(SQL_CREATE_DIYANET_ILCE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}