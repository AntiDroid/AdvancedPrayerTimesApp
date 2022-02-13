package com.example.advancedprayertimes.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.advancedprayertimes.logic.PrayerTimeEntity
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.extensions.toStringByFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class PrayerTimeGraphicView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    companion object {

        private val MAIN_BACKGROUND_COLOR = Color.argb(255, 35, 41, 53)
        private val PRAYER_TIME_COLOR = Color.rgb(124,197,107)

        private val PRAYER_MAIN_TEXT_COLOR = Color.YELLOW
        private val CURRENT_TIME_TEXT_COLOR = Color.RED
        private val CURRENT_TIME_INDICATOR_COLOR = Color.BLACK
        private val PRAYER_SUB_TIME_BACKGROUND_COLOR = Color.rgb(90,187,71)
        private val PRAYER_SUB_TIME_BORDER_COLOR = Color.WHITE
        private val PRAYER_SUB_TIME_TEXT_COLOR = Color.CYAN
    }

    private var innerBackgroundRectangle: RectF =
        RectF(180F, 110F, this.right - 100F, 550F)

    init {
        setBackgroundColor(MAIN_BACKGROUND_COLOR)
    }

    // region Paint Objects

    private var mainRectanglePaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            textSize = 60f
            color = PRAYER_TIME_COLOR
            style = Style.FILL_AND_STROKE
            strokeWidth = 3F
        }
    }

    private var prayerNameTextPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            textSize = 60f
            color = PRAYER_MAIN_TEXT_COLOR
        }
    }

    private var prayerSubTimeNameTextPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            textSize = 40f
            color = PRAYER_SUB_TIME_TEXT_COLOR
        }
    }

    private var prayerTimeBeginningEndTimeTextPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            textSize = 40f
            color = PRAYER_MAIN_TEXT_COLOR
        }
    }

    private var currentTimeTextPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            textSize = 40f
            color = CURRENT_TIME_TEXT_COLOR
        }
    }

    private var indicatorRectanglePaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            color = CURRENT_TIME_INDICATOR_COLOR
            style = Style.STROKE
            strokeWidth = 3F
        }
    }

    private var prayerSubTimeRectangleFillPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            color = PRAYER_SUB_TIME_BACKGROUND_COLOR
            style = Style.FILL_AND_STROKE
            strokeWidth = 3F
        }
    }

    private var prayerSubTimeRectangleBorderPaint: Paint = object : Paint() {
        init {
            isAntiAlias = true
            color = PRAYER_SUB_TIME_BORDER_COLOR
            style = Style.STROKE
            strokeWidth = 1F
        }
    }

    // endregion Paint Objects

    var displayPrayerEntity: PrayerTimeEntity? = null

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        innerBackgroundRectangle.right = this.right - 100F

        if (displayPrayerEntity == null || displayPrayerEntity!!.durationMS == 0L) {
            return
        }

        drawBaseInformation(canvas)
    }

    private fun drawAsrSubtimes(canvas: Canvas) {

        // FIRST THIRD OF NIGHT
        val innerSubtimeOneBackgroundRectangle =
            RectF(
                innerBackgroundRectangle.width()/2,
                innerBackgroundRectangle.top,
                innerBackgroundRectangle.right,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.subtime1EndTime!!, innerBackgroundRectangle)
            )
        canvas.drawRoundRect(innerSubtimeOneBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeOneBackgroundRectangle, prayerSubTimeRectangleBorderPaint)
        canvas.drawText("Ikhtiyar", 675F, 185F, prayerSubTimeNameTextPaint)

        // SECOND THIRD OF NIGHT
        val innerSubtimeTwoBackgroundRectangle =
            RectF(
                innerSubtimeOneBackgroundRectangle.left,
                innerSubtimeOneBackgroundRectangle.bottom,
                innerBackgroundRectangle.right,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.subtime2EndTime!!, innerBackgroundRectangle)
            )
        canvas.drawRoundRect(innerSubtimeTwoBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeTwoBackgroundRectangle, prayerSubTimeRectangleBorderPaint)
        canvas.drawText("Normal", 675F, 340F, prayerSubTimeNameTextPaint)

        // THIRD THIRD OF NIGHT
        val innerSubtimeThreeBackgroundRectangle =
            RectF(
                innerSubtimeOneBackgroundRectangle.left,
                innerSubtimeTwoBackgroundRectangle.bottom,
                innerBackgroundRectangle.right,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.endTime!!, innerBackgroundRectangle).toFloat()
            )
        canvas.drawRoundRect(innerSubtimeThreeBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeThreeBackgroundRectangle, prayerSubTimeRectangleBorderPaint)
        canvas.drawText("Karaha", 675F, 500F, prayerSubTimeNameTextPaint)
    }

    private fun drawMaghribSubtimes(canvas: Canvas) {

        // FIRST THIRD OF NIGHT
        val innerSubtimeOneBackgroundRectangle =
            RectF(
                innerBackgroundRectangle.width()/2,
                innerBackgroundRectangle.top,
                innerBackgroundRectangle.right,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.subtime1EndTime!!, innerBackgroundRectangle)
            )
        canvas.drawRoundRect(innerSubtimeOneBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeOneBackgroundRectangle, prayerSubTimeRectangleBorderPaint)
        canvas.drawText("Normal", 675F, 285F, prayerSubTimeNameTextPaint)

        // THIRD THIRD OF NIGHT
        val innerSubtimeThreeBackgroundRectangle =
            RectF(
                innerSubtimeOneBackgroundRectangle.left,
                innerSubtimeOneBackgroundRectangle.bottom,
                innerBackgroundRectangle.right,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.endTime!!, innerBackgroundRectangle).toFloat()
            )
        canvas.drawRoundRect(innerSubtimeThreeBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeThreeBackgroundRectangle, prayerSubTimeRectangleBorderPaint)
        canvas.drawText("Karaha", 675F, 500F, prayerSubTimeNameTextPaint)
    }

    private fun drawIshaSubtimes(canvas: Canvas) {

        // FIRST THIRD OF NIGHT
        val innerSubtimeOneBackgroundRectangle =
            RectF(
                innerBackgroundRectangle.width()/2,
                innerBackgroundRectangle.top,
                innerBackgroundRectangle.right,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.subtime1EndTime!!, innerBackgroundRectangle)
            )
        canvas.drawRoundRect(innerSubtimeOneBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeOneBackgroundRectangle, prayerSubTimeRectangleBorderPaint)
        canvas.drawText("1/3", 555F, 185F, prayerSubTimeNameTextPaint)

        // SECOND THIRD OF NIGHT
        val innerSubtimeTwoBackgroundRectangle =
            RectF(
                innerSubtimeOneBackgroundRectangle.left,
                innerSubtimeOneBackgroundRectangle.bottom,
                innerBackgroundRectangle.right,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.subtime2EndTime!!, innerBackgroundRectangle)
            )
        canvas.drawRoundRect(innerSubtimeTwoBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeTwoBackgroundRectangle, prayerSubTimeRectangleBorderPaint)
        canvas.drawText("2/3", 555F, 322F, prayerSubTimeNameTextPaint)

        // THIRD THIRD OF NIGHT
        val innerSubtimeThreeBackgroundRectangle =
            RectF(
                innerSubtimeOneBackgroundRectangle.left,
                innerSubtimeTwoBackgroundRectangle.bottom,
                innerBackgroundRectangle.right,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.endTime!!, innerBackgroundRectangle).toFloat()
            )
        canvas.drawRoundRect(innerSubtimeThreeBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeThreeBackgroundRectangle, prayerSubTimeRectangleBorderPaint)
        canvas.drawText("3/3", 555F, 485F, prayerSubTimeNameTextPaint)

        // FIRST HALF OF NIGHT
        val innerSubtimeFourBackgroundRectangle =
            RectF(
                300 + innerSubtimeOneBackgroundRectangle.left,
                innerBackgroundRectangle.top,
                this.right - 100F,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.subtime3EndTime!!, innerBackgroundRectangle)
            )
        canvas.drawRoundRect(innerSubtimeFourBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeFourBackgroundRectangle, prayerSubTimeRectangleBorderPaint);
        canvas.drawText("1/2", 860F, 220F, prayerSubTimeNameTextPaint)

        // SECOND HALF OF NIGHT
        val innerSubtimeFiveBackgroundRectangle =
            RectF(
                300 + innerSubtimeOneBackgroundRectangle.left,
                innerSubtimeFourBackgroundRectangle.bottom,
                this.right - 100F,
                innerBackgroundRectangle.top + getRelativeDepthByTime(displayPrayerEntity!!.endTime!!, innerBackgroundRectangle)
            )
        canvas.drawRoundRect(innerSubtimeFiveBackgroundRectangle, 0f, 0f, prayerSubTimeRectangleFillPaint)
        canvas.drawRect(innerSubtimeFiveBackgroundRectangle, prayerSubTimeRectangleBorderPaint);
        canvas.drawText("2/2", 860F, 440F, prayerSubTimeNameTextPaint)
    }

    private fun drawBaseInformation(canvas: Canvas) {

        mainRectanglePaint.color = PRAYER_TIME_COLOR
        canvas.drawRoundRect(innerBackgroundRectangle, 20f, 20f, mainRectanglePaint)
        canvas.drawRoundRect(innerBackgroundRectangle, 20f, 20f, mainRectanglePaint)

        drawPrayerTimeTexts(canvas)

        if(displayPrayerEntity!!.prayerTimeType == EPrayerTimeType.Isha) {
            drawIshaSubtimes(canvas)
        } else if (displayPrayerEntity!!.prayerTimeType == EPrayerTimeType.Maghrib) {
            drawMaghribSubtimes(canvas)
        }else if (displayPrayerEntity!!.prayerTimeType == EPrayerTimeType.Asr) {
            drawAsrSubtimes(canvas)
        }

        drawCurrentTimeIndicator(canvas, innerBackgroundRectangle)
    }

    private fun drawPrayerTimeTexts(canvas: Canvas) {

        // PRAYER NAME TEXT
        canvas.drawText(
            displayPrayerEntity!!.title,
            (this.width / 2 - 50).toFloat(),
            80f,
            prayerNameTextPaint
        )

        // PRAYER TIME BEGINNING TEXT
        canvas.drawText(
            displayPrayerEntity!!.beginningTime!!.toStringByFormat("HH:mm"),
            60f,
            125f,
            prayerTimeBeginningEndTimeTextPaint
        )

        // PRAYER TIME END TEXT
        canvas.drawText(
            displayPrayerEntity!!.endTime!!.toStringByFormat("HH:mm"),
            60f,
            560f,
            prayerTimeBeginningEndTimeTextPaint
        )
    }

    private fun drawCurrentTimeIndicator(canvas: Canvas, innerBackgroundRectangle: RectF) {
        val currentDate = LocalDateTime.now()

        if(displayPrayerEntity?.durationMS == null) { return }

        val relativePos = getRelativeDepthByTime(LocalTime.now(), innerBackgroundRectangle) ?: return
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
            currentDate.toStringByFormat("HH:mm"),
            60f,
            (indicatorRectangle.top + 16).toFloat(),
            currentTimeTextPaint
        )
    }

    private fun getRelativeDepthByTime(time: LocalTime, innerBackgroundRectangle: RectF): Int {

        val stuff = if (displayPrayerEntity!!.endTime!!.atDate(LocalDate.now()).isBefore(displayPrayerEntity!!.beginningTime!!.atDate(LocalDate.now()))
            &&
            time.isBefore(displayPrayerEntity!!.beginningTime)
        ) {
            ChronoUnit.MILLIS.between(displayPrayerEntity!!.beginningTime!!.atDate(LocalDate.now()).minusDays(1), time.atDate(LocalDate.now()))
        } else {
            ChronoUnit.MILLIS.between(displayPrayerEntity!!.beginningTime, time)
        }

        val percentage = stuff.toDouble() / displayPrayerEntity!!.durationMS!!
        return (innerBackgroundRectangle.height() * percentage).toInt()
    }
}