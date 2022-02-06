package com.example.advancedprayertimes.ui

import android.content.Context
import android.graphics.*
import com.example.advancedprayertimes.logic.PrayerTimeEntity
import android.util.AttributeSet
import android.view.View
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class PrayerTimeGraphicView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    var textPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            textSize = 60f
            color = prayerTimeMainTextColor
        }
    }
    var prayerTimeTextPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            textSize = 40f
            color = prayerTimeMainTextColor
        }
    }
    var currentTimeTextPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            textSize = 40f
            color = currentTimeTextColor
        }
    }
    var indicatorRectanglePaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            color = currentTimeIndicatorLineColor
        }
    }
    var displayPrayerEntity: PrayerTimeEntity? = null
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (displayPrayerEntity == null || displayPrayerEntity!!.durationMS == 0L) {
            return
        }

        //TODO: Move rectangle instantation outside of draw

        // INNER BACKGROUND RECTANGLE
        val innerBackgroundRectangle = RectF(180F, 110F, (this.right - 100F).toFloat(), 550F)
        canvas.drawRoundRect(innerBackgroundRectangle, 20f, 20f, innerBackgroundRectanglePaint)

        // PRAYER TIME TEXT
        canvas.drawText(
            displayPrayerEntity!!.title,
            (this.width / 2 - 50).toFloat(),
            80f,
            textPaint
        )

        // PRAYER TIME BEGINNING TEXT
        canvas.drawText(
            dateFormat.format(displayPrayerEntity!!.beginningTime),
            60f,
            125f,
            prayerTimeTextPaint
        )

        // PRAYER TIME END TEXT
        canvas.drawText(
            dateFormat.format(displayPrayerEntity!!.endTime),
            60f,
            innerBackgroundRectangle.height() + 120,
            prayerTimeTextPaint
        )
        drawCurrentTime(canvas, innerBackgroundRectangle)
    }

    private fun drawCurrentTime(canvas: Canvas, innerBackgroundRectangle: RectF) {
        val currentDate = LocalDateTime.now()
        var timeShare: Long = 0

        // TODO: Fix Isha
        timeShare =
            if (displayPrayerEntity!!.endTime!!.isBefore(displayPrayerEntity!!.beginningTime) && currentDate.isBefore(
                    displayPrayerEntity!!.beginningTime
                )
            ) {
                ChronoUnit.MILLIS.between(
                    displayPrayerEntity!!.beginningTime!!.minusDays(
                        1
                    ), currentDate
                )
            } else {
                ChronoUnit.MILLIS.between(
                    displayPrayerEntity!!.beginningTime,
                    currentDate
                )
            }

        if(displayPrayerEntity?.durationMS == null) { return }

        val percentage = timeShare.toDouble() / displayPrayerEntity!!.durationMS!!
        val relativePos = (innerBackgroundRectangle.height() * percentage).toInt()
        val calculatedTop = innerBackgroundRectangle.top.toInt() + relativePos
        val calculatedBottom = calculatedTop + 2
        val indicatorRectangle = Rect(
            innerBackgroundRectangle.left.toInt(),
            calculatedTop,
            innerBackgroundRectangle.right.toInt(),
            calculatedBottom
        )
        canvas.drawRect(indicatorRectangle, indicatorRectanglePaint)

        // CURRENT TIME TEXT
        canvas.drawText(
            currentDate.format(dateFormat),
            60f,
            (indicatorRectangle.top + 16).toFloat(),
            currentTimeTextPaint
        )
    }

    companion object {
        private val backgroundRectangleColor = Color.argb(255, 35, 41, 53)
        private val backgroundRectanglePaint: Paint = object : Paint() {
            init {
                color = backgroundRectangleColor
                isAntiAlias = true
            }
        }
        private val innerBackgroundRectangleColor = Color.rgb(0, 204, 102)
        private val innerBackgroundRectanglePaint: Paint = object : Paint() {
            init {
                isAntiAlias = true
                color = innerBackgroundRectangleColor
            }
        }
        private const val prayerTimeMainTextColor = Color.YELLOW
        private const val currentTimeTextColor = Color.RED
        private const val currentTimeIndicatorLineColor = Color.WHITE
        var dateFormat = DateTimeFormatter.ofPattern("HH:mm")
    }

    init {
        setBackgroundColor(backgroundRectangleColor)
    }
}