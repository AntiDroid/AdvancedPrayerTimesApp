package com.example.advancedprayertimes.UI;

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

public class PrayerTimeGraphicView extends View
{
    private static final int backgroundRectangleColor = Color.argb(255, 35,41,53);
    private static Paint backgroundRectanglePaint = new Paint()
    {
        {
            setColor(backgroundRectangleColor);
            setAntiAlias(true);
        }
    };

    private static final int innerBackgroundRectangleColor = Color.rgb(0, 204, 102);
    private static Paint innerBackgroundRectanglePaint = new Paint()
    {
        {
            setAntiAlias(true);
            setColor(innerBackgroundRectangleColor);
        }
    };

    private static final int prayerTimeMainTextColor = Color.YELLOW;
    Paint textPaint = new Paint()
    {
        {
            setAntiAlias(true);
            setTextSize(60);
            setColor(prayerTimeMainTextColor);
        }
    };

    Paint prayerTimeTextPaint = new Paint()
    {
        {
            setAntiAlias(true);
            setTextSize(40);
            setColor(prayerTimeMainTextColor);
        }
    };

    private static final int currentTimeTextColor = Color.RED;
    Paint currentTimeTextPaint = new Paint()
    {
        {
            setAntiAlias(true);
            setTextSize(40);
            setColor(currentTimeTextColor);
        }
    };

    Paint indicatorRectanglePaint = new Paint()
    {
        {
            setAntiAlias(true);
            setColor(currentTimeIndicatorLineColor);
        }
    };

    private static final int currentTimeIndicatorLineColor = Color.WHITE;

    public static DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private PrayerEntity _displayPrayerEntity = null;

    public PrayerTimeGraphicView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // BACKGROUND RECTANGLE
        Rect backgroundRectangle = new Rect(40, 0, 1000, 600);
        canvas.drawRect(backgroundRectangle, backgroundRectanglePaint);

        if(this.getDisplayPrayerEntity() == null)
        {
            return;
        }

        //TODO: Move rectangle instantation outside of draw

        // INNER BACKGROUND RECTANGLE
        RectF innerBackgroundRectangle = new RectF(180, 110, 950, 550);
        canvas.drawRoundRect(innerBackgroundRectangle, 20, 20, innerBackgroundRectanglePaint);

        // PRAYER TIME TEXT
        canvas.drawText(this.getDisplayPrayerEntity().getTitle(), 450, 80, textPaint);

        // PRAYER TIME BEGINNING TEXT
        canvas.drawText(dateFormat.format(this.getDisplayPrayerEntity().getBeginningTime()), 60, 125, prayerTimeTextPaint);

        // PRAYER TIME END TEXT
        canvas.drawText(dateFormat.format(this.getDisplayPrayerEntity().getEndTime()), 60, innerBackgroundRectangle.height() + 120, prayerTimeTextPaint);

        drawCurrentTime(canvas, innerBackgroundRectangle);
    }

    private void drawCurrentTime(Canvas canvas, RectF innerBackgroundRectangle)
    {
        LocalDateTime currentDate = LocalDateTime.now();

        long indicatorTime = new Time(currentDate.getHour(), currentDate.getMinute(), currentDate.getSecond()).getTime();

        long g = 0;

        g = this.getDisplayPrayerEntity().getEndTime().getTime() - this.getDisplayPrayerEntity().getBeginningTime().getTime();

        // TODO: Fix Isha
        if(this.getDisplayPrayerEntity().getEndTime().getTime() < this.getDisplayPrayerEntity().getBeginningTime().getTime())
        {
            long timeOfOneDay = new Date(0, 0, 1, 0, 0, 0).getTime() - new Date(0, 0, 0, 0, 0, 0).getTime();;

            g += timeOfOneDay;

            if(indicatorTime < this.getDisplayPrayerEntity().getBeginningTime().getTime() && indicatorTime < this.getDisplayPrayerEntity().getEndTime().getTime())
            {
                indicatorTime += timeOfOneDay;
            }
        }

        long a = indicatorTime - this.getDisplayPrayerEntity().getBeginningTime().getTime();

        double percentage = (double)a/g;

        int relativePos = (int) (innerBackgroundRectangle.height() * percentage);

        int calculatedTop = (int)innerBackgroundRectangle.top + relativePos;
        int calculatedBottom = calculatedTop + 2;

        Rect indicatorRectangle = new Rect((int)innerBackgroundRectangle.left, calculatedTop, (int)innerBackgroundRectangle.right, calculatedBottom);
        canvas.drawRect(indicatorRectangle, indicatorRectanglePaint);

        // CURRENT TIME TEXT
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
