package com.example.advancedprayertimes.logic

import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import kotlin.NotImplementedError
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class PrayerTimeEntity private constructor(val prayerTimeType: EPrayerTimeType, val title: String)
{
    init
    {
        if(this.prayerTimeType == EPrayerTimeType.Asr)
        {
            //this.prayerTimeType = this.prayerTimeType;
        }
    }

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

    fun GetTimeByMomentType(prayerTimeMomentType: EPrayerTimeMomentType): LocalDateTime?
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

    fun SetTimeByMomentType(prayerTimeMomentType: EPrayerTimeMomentType, date: LocalDateTime?)
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

        @kotlin.jvm.JvmStatic
        fun GetPrayerByTime(time: LocalDateTime): PrayerTimeEntity?
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

            if (targetPrayer != null)
            {
                return targetPrayer
            }
            else if (
                (// before fajr beginning
                time.isAfter(Prayers[5].beginningTime)
                ||  // before isha end
                time.isBefore(Prayers[5].endTime))
            )
            {
                return Prayers[5]
            }

            return null
        }
    }
}