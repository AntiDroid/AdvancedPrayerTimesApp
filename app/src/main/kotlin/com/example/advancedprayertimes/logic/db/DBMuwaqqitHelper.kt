package com.example.advancedprayertimes.logic.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.advancedprayertimes.logic.AppEnvironment
import com.example.advancedprayertimes.logic.CustomLocation
import com.example.advancedprayertimes.logic.api_entities.MuwaqqitPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.extensions.parseToDate
import com.example.advancedprayertimes.logic.extensions.parseToTime
import com.example.advancedprayertimes.logic.extensions.toStringByFormat

class DBMuwaqqitHelper(context: Context?) : DBHelper(context) {

    companion object {

        fun addMuwaqqitPrayerTime(
            muwaqqitPrayerTimeDayEntity: MuwaqqitPrayerTimeDayEntity,
            location: CustomLocation
        ): Boolean {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            val cv = ContentValues()

            cv.put(fajrTimeColumn, muwaqqitPrayerTimeDayEntity.fajrTime!!.toStringByFormat("HH:mm"))
            cv.put(sunriseTimeColumn, muwaqqitPrayerTimeDayEntity.sunriseTime!!.toStringByFormat("HH:mm"))
            cv.put(duhaTimeColumn, muwaqqitPrayerTimeDayEntity.duhaTime!!.toStringByFormat("HH:mm"))
            cv.put(dhuhrTimeColumn, muwaqqitPrayerTimeDayEntity.dhuhrTime!!.toStringByFormat("HH:mm"))
            cv.put(asrMithlTimeColumn, muwaqqitPrayerTimeDayEntity.asrTime!!.toStringByFormat("HH:mm"))
            cv.put(asrMithlaynTimeColumn, muwaqqitPrayerTimeDayEntity.mithlaynTime!!.toStringByFormat("HH:mm"))
            cv.put(asrKarahaTimeColumn, muwaqqitPrayerTimeDayEntity.asrKarahaTime!!.toStringByFormat("HH:mm"))
            cv.put(maghribTimeColumn, muwaqqitPrayerTimeDayEntity.maghribTime!!.toStringByFormat("HH:mm"))
            cv.put(ishaTimeColumn, muwaqqitPrayerTimeDayEntity.ishaTime!!.toStringByFormat("HH:mm"))

            cv.put(fajrDegreeColumn, muwaqqitPrayerTimeDayEntity.fajrAngle)
            cv.put(ishaDegreeColumn, muwaqqitPrayerTimeDayEntity.ishaAngle)
            cv.put(karahaDegreeColumn, muwaqqitPrayerTimeDayEntity.asrKarahaAngle)

            cv.put(dateColumn, muwaqqitPrayerTimeDayEntity.date!!.toStringByFormat("dd-MM-yyyy"))
            cv.put(longitudeColumn, location.longitude)
            cv.put(latitudeColumn, location.latitude)
            cv.put(insertDateMilliSecondsColumn, System.currentTimeMillis())

            val returnValue = db.insert(MUWAQQIT_PRAYER_TIME_TABLE, null, cv) != -1L
            db.close()
            return returnValue
        }

        fun createMuwaqqitPrayerTimesByDateLocationAndDegreesIfNotExist(
            prayerTime: MuwaqqitPrayerTimeDayEntity,
            location: CustomLocation
        ) {
            if (this.getMuwaqqitPrayerTimesByDateLocationAndDegrees(
                    prayerTime.date!!.toStringByFormat("dd-MM-yyyy"),
                    location.longitude,
                    location.latitude,
                    prayerTime.fajrAngle,
                    prayerTime.ishaAngle,
                    prayerTime.asrKarahaAngle
                ) == null
            ) {
                this.addMuwaqqitPrayerTime(prayerTime, location)
            }
        }

        fun getMuwaqqitPrayerTimesByDateLocationAndDegrees(
            todayDateString: String,
            longitude: Double,
            latitude: Double,
            fajrDegree: Double?,
            ishaDegree: Double?,
            karahaDegree: Double?
        ): MuwaqqitPrayerTimeDayEntity? {
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
            val db = AppEnvironment.dbHelper!!.readableDatabase
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

        fun getAllMuwaqqitPrayerTimes(): List<MuwaqqitPrayerTimeDayEntity> {

            val returnList: MutableList<MuwaqqitPrayerTimeDayEntity> = ArrayList()
            val queryString = "SELECT * FROM $MUWAQQIT_PRAYER_TIME_TABLE"

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
            val cursor = db.rawQuery(queryString, null)

            // true if there are any results
            if (cursor.moveToFirst()) {
                do {
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
            val db = AppEnvironment.dbHelper!!.readableDatabase
            val cursor = db.rawQuery(queryString, null)

            // true if there are any results
            if (cursor.moveToFirst()) {
                do {
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
        ): Boolean {
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
            val db = AppEnvironment.dbHelper!!.readableDatabase
            val cursor = db.rawQuery(queryString, null)
            val returnValue = cursor.count > 0
            cursor.close()
            db.close()
            return returnValue
        }

        fun deleteMuwaqqitPrayerTimesBelowCertainInsertDate(insertDateMilliSeconds: Long) {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            db.execSQL("DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE WHERE $insertDateMilliSecondsColumn < $insertDateMilliSeconds")
            db.close()
        }

        fun deleteAllMuwaqqitPrayerTimesByDegrees(
            fajrDegree: Double?,
            ishaDegree: Double?,
            karahaDegree: Double?
        ) {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            var sql = "DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE"

            if (fajrDegree != null || ishaDegree != null) {
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
            val sunriseTimeColumnIndex = cursor.getColumnIndexOrThrow(sunriseTimeColumn)
            val duhaTimeColumnIndex = cursor.getColumnIndexOrThrow(duhaTimeColumn)

            val dhuhrTimeColumnIndex = cursor.getColumnIndexOrThrow(dhuhrTimeColumn)
            val asrTimeColumnIndex = cursor.getColumnIndexOrThrow(asrMithlTimeColumn)
            val asrMithlaynTimeColumnIndex = cursor.getColumnIndexOrThrow(asrMithlaynTimeColumn)
            val asrKarahaTimeColumnIndex = cursor.getColumnIndexOrThrow(asrKarahaTimeColumn)

            val maghribTimeColumnIndex = cursor.getColumnIndexOrThrow(maghribTimeColumn)
            val ishaTimeColumnIndex = cursor.getColumnIndexOrThrow(ishaTimeColumn)

            val dateColumnIndex = cursor.getColumnIndexOrThrow(dateColumn)

            val fajrDegreeColumnIndex = cursor.getColumnIndexOrThrow(fajrDegreeColumn)
            val karahaDegreeColumnIndex = cursor.getColumnIndexOrThrow(karahaDegreeColumn)
            val ishaDegreeColumnIndex = cursor.getColumnIndexOrThrow(ishaDegreeColumn)

            return MuwaqqitPrayerTimeDayEntity(
                fajrTime = cursor.getString(fajrTimeColumnIndex).parseToTime("HH:mm"),
                sunriseTime = cursor.getString(sunriseTimeColumnIndex).parseToTime("HH:mm"),
                duhaTime = cursor.getString(duhaTimeColumnIndex).parseToTime("HH:mm"),
                dhuhrTime = cursor.getString(dhuhrTimeColumnIndex).parseToTime("HH:mm"),
                asrTime = cursor.getString(asrTimeColumnIndex).parseToTime("HH:mm"),
                mithlaynTime = cursor.getString(asrMithlaynTimeColumnIndex).parseToTime("HH:mm"),
                asrKarahaTime = cursor.getString(asrKarahaTimeColumnIndex).parseToTime("HH:mm"),

                maghribTime = cursor.getString(maghribTimeColumnIndex).parseToTime("HH:mm"),
                ishaTime = cursor.getString(ishaTimeColumnIndex).parseToTime("HH:mm"),
                date = cursor.getString(dateColumnIndex).parseToDate("dd-MM-yyyy"),

                fajrAngle = cursor.getDouble(fajrDegreeColumnIndex),
                asrKarahaAngle = cursor.getDouble(karahaDegreeColumnIndex),
                ishaAngle = cursor.getDouble(ishaDegreeColumnIndex)
            )
        }

        fun deleteAllMuwaqqitPrayerTimes() {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            db.execSQL("DELETE FROM $MUWAQQIT_PRAYER_TIME_TABLE")
            db.close()
        }
    }
}