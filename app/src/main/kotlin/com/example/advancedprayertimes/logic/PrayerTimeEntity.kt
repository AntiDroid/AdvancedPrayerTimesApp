package com.example.advancedprayertimes.logic

import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class PrayerTimeEntity private constructor(val prayerTimeType: EPrayerTimeType, val title: String)
{
    var beginningTime: LocalTime? = null
    var endTime: LocalTime? = null
    var subtime1BeginningTime: LocalTime? = null
    var subtime1EndTime: LocalTime? = null
    var subtime2BeginningTime: LocalTime? = null
    var subtime2EndTime: LocalTime? = null
    var subtime3BeginningTime: LocalTime? = null
    var subtime3EndTime: LocalTime? = null

    // TODO: Fix Isha
    val durationMS: Long?
        get()
        {
            if (endTime == null || beginningTime == null) {
                return null
            }

            // TODO: Fix Isha
            return if(endTime!!.isBefore(beginningTime)) {
                ChronoUnit.MILLIS.between(beginningTime!!.atDate(LocalDate.MIN), endTime!!.atDate(LocalDate.MIN).plusDays(1))
            } else {
                ChronoUnit.MILLIS.between(beginningTime, endTime)
            }
        }

    fun getTimeByMomentType(prayerTimeMomentType: EPrayerTimeMomentType): LocalTime?
    {
        return when (prayerTimeMomentType)
        {
            EPrayerTimeMomentType.Beginning -> beginningTime
            EPrayerTimeMomentType.End -> endTime
            EPrayerTimeMomentType.SubTimeOne -> subtime1EndTime
            EPrayerTimeMomentType.SubTimeTwo -> subtime2EndTime
            EPrayerTimeMomentType.SubTimeThree -> subtime3EndTime
            else -> null
        }
    }

    fun setTimeByMomentType(prayerTimeMomentType: EPrayerTimeMomentType, date: LocalTime?)
    {
        when (prayerTimeMomentType)
        {
            EPrayerTimeMomentType.Beginning -> beginningTime = date
            EPrayerTimeMomentType.End -> endTime = date
            EPrayerTimeMomentType.SubTimeOne -> subtime1EndTime = date
            EPrayerTimeMomentType.SubTimeTwo -> subtime2EndTime = date
            EPrayerTimeMomentType.SubTimeThree -> subtime3EndTime = date
            else -> throw NotImplementedError()
        }
    }

    companion object
    {
        @kotlin.jvm.JvmField
        var Prayers: ArrayList<PrayerTimeEntity> = object : ArrayList<PrayerTimeEntity>()
        {
            init
            {
                add(PrayerTimeEntity(EPrayerTimeType.Fajr, "Fajr"))
                add(PrayerTimeEntity(EPrayerTimeType.Duha, "Duha"))
                add(PrayerTimeEntity(EPrayerTimeType.Dhuhr, "Dhuhr"))
                add(PrayerTimeEntity(EPrayerTimeType.Asr, "Asr"))
                add(PrayerTimeEntity(EPrayerTimeType.Maghrib, "Maghrib"))
                add(PrayerTimeEntity(EPrayerTimeType.Isha, "Isha"))
            }
        }

        val Fajr: PrayerTimeEntity
            get() = Prayers[0]
        val Duha: PrayerTimeEntity
            get() = Prayers[1]
        val Dhuhr: PrayerTimeEntity
            get() = Prayers[2]
        val Asr: PrayerTimeEntity
            get() = Prayers[3]
        val Maghrib: PrayerTimeEntity
            get() = Prayers[4]
        val Isha: PrayerTimeEntity
            get() = Prayers[5]

        val NightDurationMS: Long?
            get() {

                if (PrayerTimeEntity.Isha.durationMS == null || PrayerTimeEntity.Maghrib.durationMS == null) {
                    return null
                }

                val timeBetweenIshaBeginningAndMaghribEnd = ChronoUnit.MILLIS.between(
                    PrayerTimeEntity.Maghrib.endTime,
                    PrayerTimeEntity.Isha.beginningTime
                )

                return PrayerTimeEntity.Isha.durationMS!! + PrayerTimeEntity.Maghrib.durationMS!! + timeBetweenIshaBeginningAndMaghribEnd
            }

        fun getPrayerByTime(time: LocalTime): PrayerTimeEntity?
        {
            val targetPrayer: PrayerTimeEntity? =
                Prayers.asSequence().filter { prayerTimeEntity: PrayerTimeEntity ->
                        prayerTimeEntity.beginningTime != null
                        &&
                        prayerTimeEntity.endTime != null
                        &&
                        time.isAfter(prayerTimeEntity.beginningTime)
                        &&
                        time.isBefore(prayerTimeEntity.endTime)
                }.firstOrNull();

            return if (targetPrayer != null)
            {
                targetPrayer
            }
            else if (
                (// before fajr beginning
                time.isAfter(Isha.beginningTime)
                ||  // before isha end
                time.isBefore(Isha.endTime))
            )
            {
                Isha
            }
            else null
        }
    }
}