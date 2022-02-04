package com.example.advancedprayertimes.logic

import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import kotlin.NotImplementedError
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class PrayerTimeEntity private constructor(val prayerTimeType: EPrayerTimeType, val title: String)
{
    var beginningTime: LocalDateTime? = null
    var endTime: LocalDateTime? = null
    var subtime1BeginningTime: LocalDateTime? = null
    var subtime1EndTime: LocalDateTime? = null
    var subtime2BeginningTime: LocalDateTime? = null
    var subtime2EndTime: LocalDateTime? = null
    var subtime3BeginningTime: LocalDateTime? = null
    var subtime3EndTime: LocalDateTime? = null

    // TODO: Fix Isha
    val Duration: Long
        get()
        {
            if (endTime == null || beginningTime == null)
            {
                return 0
            }

            var duration: Long = ChronoUnit.MILLIS.between(beginningTime, endTime)

            // TODO: Fix Isha
            if (endTime!!.isBefore(beginningTime))
            {
                duration = ChronoUnit.MILLIS.between(beginningTime, endTime!!.plusDays(1))
            }

            return duration
        }

    fun getTimeByMomentType(prayerTimeMomentType: EPrayerTimeMomentType): LocalDateTime?
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

    fun setTimeByMomentType(prayerTimeMomentType: EPrayerTimeMomentType, date: LocalDateTime?)
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

        fun getPrayerByTime(time: LocalDateTime): PrayerTimeEntity?
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