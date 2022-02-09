package com.example.advancedprayertimes.logic.db

import android.content.ContentValues
import android.database.Cursor
import androidx.core.database.getDoubleOrNull
import com.example.advancedprayertimes.logic.AppEnvironment
import com.example.advancedprayertimes.logic.CustomLocation
import com.example.advancedprayertimes.logic.api_entities.AlAdhanPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.ALADHAN_PRAYER_TIME_TABLE
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.asrMithlTimeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.dateColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.dhuhrTimeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.fajrDegreeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.fajrTimeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.insertDateMilliSecondsColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.ishaDegreeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.ishaTimeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.ishtibaqDegreeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.latitudeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.longitudeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.maghribTimeColumn
import com.example.advancedprayertimes.logic.db.DBHelper.Companion.sunriseTimeColumn
import com.example.advancedprayertimes.logic.extensions.parseToDate
import com.example.advancedprayertimes.logic.extensions.parseToTime
import com.example.advancedprayertimes.logic.extensions.toStringByFormat

class DBAlAdhanHelper {

    companion object {

        fun createAlAdhanPrayerTimesByDateLocationAndDegreesIfNotExist(
            prayerTime: AlAdhanPrayerTimeDayEntity,
            location: CustomLocation
        ) {
            if (this.getAlAdhanPrayerTimesByDateLocationAndDegrees(
                    prayerTime.date!!.toStringByFormat("dd-MM-yyyy"),
                    location.longitude,
                    location.latitude,
                    prayerTime.fajrAngle,
                    prayerTime.ishaAngle,
                    prayerTime.ishtibaqAngle
                ) == null
            ) {
                this.addAlAdhanPrayerTime(prayerTime, location)
            }
        }

        fun getAllAlAdhanPrayerTimes(): List<AlAdhanPrayerTimeDayEntity> {

            val returnList: MutableList<AlAdhanPrayerTimeDayEntity> = ArrayList()
            val queryString = "SELECT * FROM $ALADHAN_PRAYER_TIME_TABLE"

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
            val cursor = db.rawQuery(queryString, null)

            // true if there are any results
            if (cursor.moveToFirst()) {
                do {
                    returnList.add(getAlAdhanDataFromCursor(cursor))
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            return returnList
        }

        private fun getAlAdhanDataFromCursor(cursor: Cursor): AlAdhanPrayerTimeDayEntity {

            val fajrTimeColumnIndex = cursor.getColumnIndexOrThrow(fajrTimeColumn)
            val sunriseTimeColumnIndex = cursor.getColumnIndexOrThrow(sunriseTimeColumn)

            val dhuhrTimeColumnIndex = cursor.getColumnIndexOrThrow(dhuhrTimeColumn)
            val asrTimeColumnIndex = cursor.getColumnIndexOrThrow(asrMithlTimeColumn)

            val maghribTimeColumnIndex = cursor.getColumnIndexOrThrow(maghribTimeColumn)
            val ishaTimeColumnIndex = cursor.getColumnIndexOrThrow(ishaTimeColumn)

            val dateColumnIndex = cursor.getColumnIndexOrThrow(dateColumn)

            val fajrDegreeColumnIndex = cursor.getColumnIndexOrThrow(fajrDegreeColumn)
            val ishaDegreeColumnIndex = cursor.getColumnIndexOrThrow(ishaDegreeColumn)
            val ishtibaqDegreeColumnIndex = cursor.getColumnIndexOrThrow(ishtibaqDegreeColumn)

            val maghribTime =
                if (cursor.getDoubleOrNull(ishtibaqDegreeColumnIndex) == null)
                    cursor.getString(maghribTimeColumnIndex).parseToTime("HH:mm")
                else
                    null

            val ishtibaqTime =
                if (maghribTime == null)
                    cursor.getString(maghribTimeColumnIndex).parseToTime("HH:mm")
                else
                    null

            return AlAdhanPrayerTimeDayEntity(
                fajrTime = cursor.getString(fajrTimeColumnIndex).parseToTime("HH:mm"),
                sunriseTime = cursor.getString(sunriseTimeColumnIndex).parseToTime("HH:mm"),
                dhuhrTime = cursor.getString(dhuhrTimeColumnIndex).parseToTime("HH:mm"),
                asrTime = cursor.getString(asrTimeColumnIndex).parseToTime("HH:mm"),
                mithlaynTime = null,
                maghribTime = maghribTime,
                ishtibaqAnNujumTime = ishtibaqTime,
                ishaTime = cursor.getString(ishaTimeColumnIndex).parseToTime("HH:mm"),
                date = cursor.getString(dateColumnIndex).parseToDate("dd-MM-yyyy"),

                fajrAngle = cursor.getDoubleOrNull(fajrDegreeColumnIndex),
                ishaAngle = cursor.getDoubleOrNull(ishaDegreeColumnIndex),
                ishtibaqAngle = cursor.getDoubleOrNull(ishtibaqDegreeColumnIndex),
            )
        }

        fun getAlAdhanPrayerTimesByDateLocationAndDegrees(
            todayDateString: String,
            longitude: Double,
            latitude: Double,
            fajrDegree: Double?,
            ishaDegree: Double?,
            ishtibaqDegree: Double?
        ): AlAdhanPrayerTimeDayEntity? {
            var queryString =
                "SELECT * FROM $ALADHAN_PRAYER_TIME_TABLE" +
                        " WHERE " +
                        "$longitudeColumn = $longitude AND $latitudeColumn = $latitude" +
                        " AND $dateColumn = '$todayDateString'"

            if (fajrDegree != null) {
                queryString += " AND $fajrDegreeColumn = $fajrDegree"
            }
            if (ishaDegree != null) {
                queryString += " AND $ishaDegreeColumn = $ishaDegree"
            }
            if (ishtibaqDegree != null) {
                queryString += " AND $ishtibaqDegreeColumn = $ishtibaqDegree"
            }

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
            val cursor = db.rawQuery(queryString, null)
            var targetTime: AlAdhanPrayerTimeDayEntity? = null

            // true if there are any results
            if (cursor.moveToFirst()) {
                targetTime = getAlAdhanDataFromCursor(cursor)
            }

            cursor.close()
            db.close()
            return targetTime
        }

        fun addAlAdhanPrayerTime(
            alAdhanPrayerTimeDayEntity: AlAdhanPrayerTimeDayEntity,
            location: CustomLocation
        ): Boolean {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            val cv = ContentValues()

            with(alAdhanPrayerTimeDayEntity) {

                val maghribOrIshtibaqTime = if (ishtibaqAngle != null) ishtibaqAnNujumTime else maghribTime

                cv.put(fajrTimeColumn, fajrTime!!.toStringByFormat("HH:mm"))
                cv.put(sunriseTimeColumn, sunriseTime!!.toStringByFormat("HH:mm"))
                cv.put(dhuhrTimeColumn, dhuhrTime!!.toStringByFormat("HH:mm"))
                cv.put(asrMithlTimeColumn, asrTime!!.toStringByFormat("HH:mm"))
                cv.put(maghribTimeColumn, maghribOrIshtibaqTime!!.toStringByFormat("HH:mm"))
                cv.put(ishaTimeColumn, ishaTime!!.toStringByFormat("HH:mm"))

                cv.put(fajrDegreeColumn, fajrAngle)
                cv.put(ishaDegreeColumn, ishaAngle)
                cv.put(ishtibaqDegreeColumn, ishtibaqAngle)
                cv.put(dateColumn, date!!.toStringByFormat("dd-MM-yyyy"))
                cv.put(longitudeColumn, location.longitude)
                cv.put(latitudeColumn, location.latitude)
                cv.put(insertDateMilliSecondsColumn, java.lang.System.currentTimeMillis())
            }

            val returnValue = db.insertOrThrow(ALADHAN_PRAYER_TIME_TABLE, null, cv) != -1L
            db.close()
            return returnValue
        }

        fun deleteAllAlAdhanPrayerTimes() {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            db.execSQL("DELETE FROM $ALADHAN_PRAYER_TIME_TABLE")
            db.close()
        }
    }
}