package com.example.advancedprayertimes.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.advancedprayertimes.Logic.Entities.PrayerEntity;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class TestCustomView extends View
{
    public static DateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private static final int backgroundRectangleColor = Color.argb(255, 35,41,53);
    private static final int innerBackgroundRectangleColor = Color.rgb(0, 204, 102);
    private static final int prayerTimeMainTextColor = Color.YELLOW;
    private static final int currentTimeIndicatorLineColor = Color.WHITE;
    private static final int currentTimeTextColor = Color.RED;

    private PrayerEntity _displayPrayerEntity = null;

    public TestCustomView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // BACKGROUND RECTANGLE
        Rect backgroundRectangle = new Rect(40, 0, 1000, 600);
        Paint backgroundRectanglePaint = new Paint();
        backgroundRectanglePaint.setColor(backgroundRectangleColor);
        canvas.drawRect(backgroundRectangle, backgroundRectanglePaint);

        if(this.getDisplayPrayerEntity() == null)
        {
            return;
        }

        //TODO: Move drawing information to setter of prayer time entity

        // INNER BACKGROUND RECTANGLE
        RectF innerBackgroundRectangle = new RectF(180, 110, 950, 550);
        Paint innerBackgroundRectanglePaint = new Paint();
        innerBackgroundRectanglePaint.setColor(innerBackgroundRectangleColor);
        canvas.drawRoundRect(innerBackgroundRectangle, 20, 20, innerBackgroundRectanglePaint);

        // PRAYER TIME TEXT
        Paint textPaint = new Paint();
        textPaint.setTextSize(60);
        textPaint.setColor(prayerTimeMainTextColor);
        canvas.drawText(this.getDisplayPrayerEntity().getTitle(), 800, 80, textPaint);

        // PRAYER TIME BEGINNING TEXT
        Paint prayerTimeTextPaint = new Paint();
        prayerTimeTextPaint.setTextSize(40);
        prayerTimeTextPaint.setColor(prayerTimeMainTextColor);
        canvas.drawText(dateFormat.format(this.getDisplayPrayerEntity().getBeginningTime()), 60, 125, prayerTimeTextPaint);

        // PRAYER TIME END TEXT
        canvas.drawText(dateFormat.format(this.getDisplayPrayerEntity().getEndTime()), 60, innerBackgroundRectangle.height() + 120, prayerTimeTextPaint);

        drawCurrentTime(canvas, innerBackgroundRectangle);
    }

    private void drawCurrentTime(Canvas canvas, RectF innerBackgroundRectangle)
    {
        LocalDateTime currentDate = LocalDateTime.now();

        long indicatorTime = new Time(currentDate.getHour(), currentDate.getMinute(), currentDate.getSecond()).getTime();

        long g = this.getDisplayPrayerEntity().getEndTime().getTime() - this.getDisplayPrayerEntity().getBeginningTime().getTime();
        long a = indicatorTime - this.getDisplayPrayerEntity().getBeginningTime().getTime();

        double percentage = (double)a/g;

        int relativePos = (int) (innerBackgroundRectangle.height() * percentage);

        int calculatedTop = (int)innerBackgroundRectangle.top + relativePos;
        int calculatedBottom = calculatedTop + 2;

        Rect indicatorRectangle = new Rect((int)innerBackgroundRectangle.left, calculatedTop, (int)innerBackgroundRectangle.right, calculatedBottom);
        Paint indicatorRectanglePaint = new Paint();
        indicatorRectanglePaint.setColor(currentTimeIndicatorLineColor);
        canvas.drawRect(indicatorRectangle, indicatorRectanglePaint);

        // CURRENT TIME TEXT
        Paint currentTimeTextPaint = new Paint();
        currentTimeTextPaint.setTextSize(40);
        currentTimeTextPaint.setColor(currentTimeTextColor);
        canvas.drawText(dateFormat.format(indicatorTime), 60, indicatorRectangle.top + 16, currentTimeTextPaint);
    }

    public PrayerEntity getDisplayPrayerEntity()
    {
        return _displayPrayerEntity;
    }

    public void setDisplayPrayerEntity(PrayerEntity displayPrayerEntity)
    {
        _displayPrayerEntity = displayPrayerEntity;
    }
}
