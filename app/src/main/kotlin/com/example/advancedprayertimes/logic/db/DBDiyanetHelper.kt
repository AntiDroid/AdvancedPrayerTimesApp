package com.example.advancedprayertimes.logic.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.advancedprayertimes.logic.AppEnvironment
import com.example.advancedprayertimes.logic.CustomLocation
import com.example.advancedprayertimes.logic.api_entities.DiyanetPrayerTimeDayEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.AbstractDiyanetSubEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetIlceEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetSehirEntity
import com.example.advancedprayertimes.logic.api_entities.diyanet.DiyanetUlkeEntity
import com.example.advancedprayertimes.logic.extensions.parseToDate
import com.example.advancedprayertimes.logic.extensions.parseToTime
import com.example.advancedprayertimes.logic.extensions.toStringByFormat

class DBDiyanetHelper(context: Context?) : DBHelper(context) {

    companion object {

        fun addDiyanetPrayerTime(
            diyanetPrayerTimeDayEntity: DiyanetPrayerTimeDayEntity,
            location: CustomLocation
        ): Boolean {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            val cv = ContentValues()

            cv.put(fajrTimeColumn, diyanetPrayerTimeDayEntity.fajrTime!!.toStringByFormat("HH:mm"))
            cv.put(
                sunriseTimeColumn,
                diyanetPrayerTimeDayEntity.sunriseTime!!.toStringByFormat("HH:mm")
            )
            cv.put(
                dhuhrTimeColumn,
                diyanetPrayerTimeDayEntity.dhuhrTime!!.toStringByFormat("HH:mm")
            )
            cv.put(
                asrMithlTimeColumn,
                diyanetPrayerTimeDayEntity.asrTime!!.toStringByFormat("HH:mm")
            )
            cv.put(
                maghribTimeColumn,
                diyanetPrayerTimeDayEntity.maghribTime!!.toStringByFormat("HH:mm")
            )
            cv.put(ishaTimeColumn, diyanetPrayerTimeDayEntity.ishaTime!!.toStringByFormat("HH:mm"))
            cv.put(dateColumn, diyanetPrayerTimeDayEntity.date!!.toStringByFormat("dd.MM.yyyy"))
            cv.put(longitudeColumn, location.longitude)
            cv.put(latitudeColumn, location.latitude)
            cv.put(insertDateMilliSecondsColumn, System.currentTimeMillis())

            var returnValue = false

            try {
                returnValue = db.insertOrThrow(DIYANET_PRAYER_TIME_TABLE, null, cv) != -1L
            } catch (exc: Exception) {
                cv.put(insertDateMilliSecondsColumn, System.currentTimeMillis())
            }

            db.close()
            return returnValue
        }

        fun addDiyanetUlke(ulke: DiyanetUlkeEntity): Boolean {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            val cv = ContentValues()
            cv.put(idColumn, ulke.ulkeID)
            cv.put(nameColumn, ulke.ulkeNameEn)
            val returnValue = db.insert(DIYANET_ULKE_TABLE, null, cv) != -1L
            db.close()
            return returnValue
        }

        fun addDiyanetSehir(parentID: String?, sehirEntity: DiyanetSehirEntity): Boolean {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            val cv = ContentValues()
            cv.put(idColumn, sehirEntity.sehirID)
            cv.put(parentIDColumn, parentID)
            cv.put(nameColumn, sehirEntity.sehirNameEn)
            val returnValue = db.insert(DIYANET_SEHIR_TABLE, null, cv) != -1L
            db.close()
            return returnValue
        }

        fun addDiyanetIlce(parentID: String?, ilceEntity: DiyanetIlceEntity): Boolean {
            val db = AppEnvironment.dbHelper!!.writableDatabase
            val cv = ContentValues()
            cv.put(idColumn, ilceEntity.ilceID)
            cv.put(parentIDColumn, parentID)
            cv.put(nameColumn, ilceEntity.ilceNameEn)
            val returnValue = db.insert(DIYANET_ILCE_TABLE, null, cv) != -1L
            db.close()
            return returnValue
        }

        fun createDiyanetTimeIfNotExist(
            diyanetTimeEntity: DiyanetPrayerTimeDayEntity,
            longitude: Double,
            latitude: Double
        ) {
            if (this.getDiyanetPrayerTimesByDateLocation(
                    diyanetTimeEntity.date!!.toStringByFormat("dd.MM.yyyy"),
                    longitude,
                    latitude
                ) == null
            ) {
                this.addDiyanetPrayerTime(
                    diyanetTimeEntity,
                    CustomLocation(longitude, latitude, "")
                )
            }
        }

        fun getDiyanetPrayerTimesByDateLocation(
            todayDateString: String,
            longitude: Double,
            latitude: Double
        ): DiyanetPrayerTimeDayEntity? {
            var queryString =
                "SELECT * FROM $DIYANET_PRAYER_TIME_TABLE" +
                        " WHERE " +
                        "$longitudeColumn = $longitude AND $latitudeColumn = $latitude" +
                        " AND $dateColumn = '$todayDateString'"

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
            val cursor = db.rawQuery(queryString, null)
            var targetTime: DiyanetPrayerTimeDayEntity? = null

            // true if there are any results
            if (cursor.moveToFirst()) {
                targetTime = getDiyanetDataFromCursor(cursor)
            }

            cursor.close()
            db.close()
            return targetTime
        }

        private fun createDiyanetUlkeIfNotExist(diyanetUlkeEntity: DiyanetUlkeEntity) {
            if (this.getDiyanetUlkeIDByName(diyanetUlkeEntity.nameEn!!) == null) {
                this.addDiyanetUlke(diyanetUlkeEntity)
            }
        }

        private fun createDiyanetIlceIfNotExist(
            parentID: String,
            diyanetIlceEntity: DiyanetIlceEntity
        ) {
            if (this.GetDiyanetIlceIDByName(diyanetIlceEntity.nameEn!!) == null) {
                this.addDiyanetIlce(parentID, diyanetIlceEntity)
            }
        }

        fun createDiyanetSubEntityIfNotExist(
            diyanetSubEntity: AbstractDiyanetSubEntity,
            parentID: String?
        ) {

            if (diyanetSubEntity is DiyanetUlkeEntity) {
                createDiyanetUlkeIfNotExist(diyanetSubEntity as DiyanetUlkeEntity)
            } else if (diyanetSubEntity is DiyanetSehirEntity) {
                return
            } else if (diyanetSubEntity is DiyanetIlceEntity) {

                if (parentID == null) {
                    throw IllegalArgumentException()
                }

                createDiyanetIlceIfNotExist(parentID, diyanetSubEntity as DiyanetIlceEntity)
            } else {
                throw NotImplementedError()
            }
        }

        fun getDiyanetUlkeIDByName(name: String): String? {

            val queryString =
                "SELECT $idColumn FROM $DIYANET_ULKE_TABLE WHERE $nameColumn = '$name'"

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
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

        fun getDiyanetSehirIDByName(name: String): String? {
            val queryString =
                "SELECT $idColumn FROM $DIYANET_SEHIR_TABLE WHERE $nameColumn = '$name'"

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
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

        fun GetDiyanetIlceIDByName(name: String): String? {
            val queryString =
                "SELECT $idColumn FROM $DIYANET_ILCE_TABLE WHERE $nameColumn = '$name'"

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
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

        fun GetDiyanetIlceIDByCountryAndCityName(countryName: String, cityName: String): String? {
            val queryString =
                "SELECT $DIYANET_ILCE_TABLE .$idColumn FROM $DIYANET_ULKE_TABLE" +
                        " INNER JOIN $DIYANET_SEHIR_TABLE ON $DIYANET_SEHIR_TABLE.$parentIDColumn = $DIYANET_ULKE_TABLE.$idColumn" +
                        " INNER JOIN $DIYANET_ILCE_TABLE ON $DIYANET_ILCE_TABLE.$parentIDColumn = $DIYANET_SEHIR_TABLE.$idColumn" +
                        " WHERE $DIYANET_ULKE_TABLE.$nameColumn  = '$countryName' AND $DIYANET_ILCE_TABLE.$nameColumn = '$cityName'"

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
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

        fun getAllDiyanetPrayerTimes(): List<DiyanetPrayerTimeDayEntity> {

            val returnList: MutableList<DiyanetPrayerTimeDayEntity> = ArrayList()
            val queryString = "SELECT * FROM $DIYANET_PRAYER_TIME_TABLE"

            // Writable database instances lock access for others
            val db = AppEnvironment.dbHelper!!.readableDatabase
            val cursor = db.rawQuery(queryString, null)

            // true if there are any results
            if (cursor.moveToFirst()) {
                do {
                    returnList.add(getDiyanetDataFromCursor(cursor))
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            return returnList
        }

        private fun getDiyanetDataFromCursor(cursor: Cursor): DiyanetPrayerTimeDayEntity {

            val fajrTimeColumnIndex = cursor.getColumnIndexOrThrow(fajrTimeColumn)
            val sunriseTimeColumnIndex = cursor.getColumnIndexOrThrow(dhuhrTimeColumn)

            val dhuhrTimeColumnIndex = cursor.getColumnIndexOrThrow(dhuhrTimeColumn)
            val asrTimeColumnIndex = cursor.getColumnIndexOrThrow(asrMithlTimeColumn)

            val maghribTimeColumnIndex = cursor.getColumnIndexOrThrow(maghribTimeColumn)
            val ishaTimeColumnIndex = cursor.getColumnIndexOrThrow(ishaTimeColumn)

            val dateColumnIndex = cursor.getColumnIndexOrThrow(dateColumn)

            val time = DiyanetPrayerTimeDayEntity()

            time.fajrTime = cursor.getString(fajrTimeColumnIndex).parseToTime("HH:mm")
            time.sunriseTime = cursor.getString(sunriseTimeColumnIndex).parseToTime("HH:mm")
            time.dhuhrTime = cursor.getString(dhuhrTimeColumnIndex).parseToTime("HH:mm")
            time.asrTime = cursor.getString(asrTimeColumnIndex).parseToTime("HH:mm")
            time.maghribTime = cursor.getString(maghribTimeColumnIndex).parseToTime("HH:mm")
            time.ishaTime = cursor.getString(ishaTimeColumnIndex).parseToTime("HH:mm")
            time.date = cursor.getString(dateColumnIndex).parseToDate("dd.MM.yyyy")

            return time
        }
    }
}