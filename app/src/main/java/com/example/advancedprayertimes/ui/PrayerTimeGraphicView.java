package com.example.advancedprayertimes.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.advancedprayertimes.Logic.Entities.PrayerTimeEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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

    public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm");
    private PrayerTimeEntity _displayPrayerTimeEntity = null;

    public PrayerTimeGraphicView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.setBackgroundColor(backgroundRectangleColor);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if(this.getDisplayPrayerEntity() == null || this.getDisplayPrayerEntity().getDuration() == 0)
        {
            return;
        }

        //TODO: Move rectangle instantation outside of draw

        // INNER BACKGROUND RECTANGLE
        RectF innerBackgroundRectangle = new RectF(180, 110, this.getRight() - 100, 550);
        canvas.drawRoundRect(innerBackgroundRectangle, 20, 20, innerBackgroundRectanglePaint);

        // PRAYER TIME TEXT
        canvas.drawText(this.getDisplayPrayerEntity().getTitle(), (this.getWidth()/2)-50, 80, textPaint);

        // PRAYER TIME BEGINNING TEXT
        canvas.drawText(dateFormat.format(this.getDisplayPrayerEntity().getBeginningTime()), 60, 125, prayerTimeTextPaint);

        // PRAYER TIME END TEXT
        canvas.drawText(dateFormat.format(this.getDisplayPrayerEntity().getEndTime()), 60, innerBackgroundRectangle.height() + 120, prayerTimeTextPaint);

        drawCurrentTime(canvas, innerBackgroundRectangle);
    }

    private void drawCurrentTime(Canvas canvas, RectF innerBackgroundRectangle)
    {
        LocalDateTime currentDate = LocalDateTime.now();

        long timeShare = 0;

        // TODO: Fix Isha
        if(this.getDisplayPrayerEntity().getEndTime().isBefore(this.getDisplayPrayerEntity().getBeginningTime()) && currentDate.isBefore(this.getDisplayPrayerEntity().getBeginningTime()))
        {
            timeShare = ChronoUnit.MILLIS.between(this.getDisplayPrayerEntity().getBeginningTime().minusDays(1), currentDate);
        }
        else
        {
            timeShare = ChronoUnit.MILLIS.between(this.getDisplayPrayerEntity().getBeginningTime(), currentDate);
        }

        double percentage = (double) timeShare/this.getDisplayPrayerEntity().getDuration();

        int relativePos = (int) (innerBackgroundRectangle.height() * percentage);

        int calculatedTop = (int)innerBackgroundRectangle.top + relativePos;
        int calculatedBottom = calculatedTop + 2;

        Rect indicatorRectangle = new Rect((int)innerBackgroundRectangle.left, calculatedTop, (int)innerBackgroundRectangle.right, calculatedBottom);
        canvas.drawRect(indicatorRectangle, indicatorRectanglePaint);

        // CURRENT TIME TEXT
        canvas.drawText(currentDate.format(dateFormat), 60, indicatorRectangle.top + 16, currentTimeTextPaint);
    }

    public PrayerTimeEntity getDisplayPrayerEntity()
    {
        return _displayPrayerTimeEntity;
    }

    public void setDisplayPrayerEntity(PrayerTimeEntity displayPrayerTimeEntity)
    {
        _displayPrayerTimeEntity = displayPrayerTimeEntity;
    }
}
