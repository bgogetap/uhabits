/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

public class HabitHistoryView extends ScrollableDataView
{
    private Habit habit;
    private int[] checkmarks;
    private Paint pSquareBg, pSquareFg, pTextHeader;
    private int squareSpacing;

    private float squareTextOffset;
    private float headerTextOffset;

    private int columnWidth;
    private int columnHeight;
    private int nColumns;
    private int baseSize;

    private String wdays[];
    private SimpleDateFormat dfMonth;
    private SimpleDateFormat dfYear;

    private Calendar baseDate;
    private int nDays;
    private int todayWeekday;
    private int colors[];
    private Rect baseLocation;
    private int primaryColor;

    private boolean isBackgroundTransparent;
    private int textColor;

    public HabitHistoryView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.primaryColor = ColorHelper.palette[7];
        init();
    }

    public void setHabit(Habit habit)
    {
        this.habit = habit;
        createColors();
        fetchData();
        postInvalidate();
    }

    private void init()
    {
        createPaints();
        createColors();

        wdays = DateHelper.getShortDayNames();
        dfMonth = new SimpleDateFormat("MMM", Locale.getDefault());
        dfYear = new SimpleDateFormat("yyyy", Locale.getDefault());

        baseLocation = new Rect();
    }

    private void updateDate()
    {
        baseDate = new GregorianCalendar();
        baseDate.add(Calendar.DAY_OF_YEAR, -(getDataOffset() - 1) * 7);

        nDays = (nColumns - 1) * 7;
        todayWeekday = new GregorianCalendar().get(Calendar.DAY_OF_WEEK) % 7;

        baseDate.add(Calendar.DAY_OF_YEAR, -nDays);
        baseDate.add(Calendar.DAY_OF_YEAR, -todayWeekday);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        if(height < 8) height = 200;

        baseSize = height / 8;
        setScrollerBucketSize(baseSize);

        columnWidth = baseSize;
        columnHeight = 8 * baseSize;
        nColumns = width / baseSize;

        squareSpacing = (int) Math.floor(baseSize / 15.0);
        pSquareFg.setTextSize(baseSize * 0.5f);
        pTextHeader.setTextSize(baseSize * 0.5f);
        squareTextOffset = pSquareFg.getFontSpacing() * 0.4f;
        headerTextOffset = pTextHeader.getFontSpacing() * 0.3f;

        updateDate();
    }

    private void createColors()
    {
        if(habit != null)
            this.primaryColor = habit.color;

        if(isBackgroundTransparent)
            primaryColor = ColorHelper.setMinValue(primaryColor, 0.75f);

        int red = Color.red(primaryColor);
        int green = Color.green(primaryColor);
        int blue = Color.blue(primaryColor);

        if(isBackgroundTransparent)
        {
            colors = new int[3];
            colors[0] = Color.argb(16, 255, 255, 255);
            colors[1] = Color.argb(128, red, green, blue);
            colors[2] = primaryColor;
            textColor = Color.rgb(255, 255, 255);
        }
        else
        {
            colors = new int[3];
            colors[0] = Color.argb(25, 0, 0, 0);
            colors[1] = Color.argb(127, red, green, blue);
            colors[2] = primaryColor;
            textColor = Color.argb(64, 0, 0, 0);
        }
    }

    protected void createPaints()
    {
        pTextHeader = new Paint();
        pTextHeader.setTextAlign(Align.LEFT);
        pTextHeader.setAntiAlias(true);

        pSquareBg = new Paint();
        pSquareBg.setColor(primaryColor);

        pSquareFg = new Paint();
        pSquareFg.setColor(Color.WHITE);
        pSquareFg.setAntiAlias(true);
        pSquareFg.setTextAlign(Align.CENTER);
    }

    protected void fetchData()
    {
        if(isInEditMode())
            generateRandomData();
        else
        {
            if(habit == null)
            {
                checkmarks = new int[0];
                return;
            }

            checkmarks = habit.checkmarks.getAllValues();
        }

        updateDate();
    }

    private void generateRandomData()
    {
        Random random = new Random();
        checkmarks = new int[100];

        for(int i = 0; i < 100; i++)
            if(random.nextFloat() < 0.3) checkmarks[i] = 2;

        for(int i = 0; i < 100 - 7; i++)
        {
            int count = 0;
            for (int j = 0; j < 7; j++)
                if(checkmarks[i + j] != 0)
                    count++;

            if(count >= 3) checkmarks[i] = Math.max(checkmarks[i], 1);
        }
    }

    private String previousMonth;
    private String previousYear;
    private boolean justPrintedYear;

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        baseLocation.set(0, 0, columnWidth - squareSpacing, columnWidth - squareSpacing);

        previousMonth = "";
        previousYear = "";
        justPrintedYear = false;

        pTextHeader.setColor(textColor);

        updateDate();
        GregorianCalendar currentDate = (GregorianCalendar) baseDate.clone();

        for (int column = 0; column < nColumns - 1; column++)
        {
            drawColumn(canvas, baseLocation, currentDate, column);
            baseLocation.offset(columnWidth, - columnHeight);
        }

        drawAxis(canvas, baseLocation);
    }

    private void drawColumn(Canvas canvas, Rect location, GregorianCalendar date, int column)
    {
        drawColumnHeader(canvas, location, date);
        location.offset(0, columnWidth);

        for (int j = 0; j < 7; j++)
        {
            if (!(column == nColumns - 2 && getDataOffset() == 0 && j > todayWeekday))
            {
                int checkmarkOffset = getDataOffset() * 7 + nDays - 7 * (column + 1) + todayWeekday - j;
                drawSquare(canvas, location, date, checkmarkOffset);
            }

            date.add(Calendar.DAY_OF_MONTH, 1);
            location.offset(0, columnWidth);
        }
    }

    private void drawSquare(Canvas canvas, Rect location, GregorianCalendar date,
                            int checkmarkOffset)
    {
        if (checkmarkOffset >= checkmarks.length) pSquareBg.setColor(colors[0]);
        else pSquareBg.setColor(colors[checkmarks[checkmarkOffset]]);

        canvas.drawRect(location, pSquareBg);
        String text = Integer.toString(date.get(Calendar.DAY_OF_MONTH));
        canvas.drawText(text, location.centerX(), location.centerY() + squareTextOffset, pSquareFg);
    }

    private void drawAxis(Canvas canvas, Rect location)
    {
        for (int i = 0; i < 7; i++)
        {
            location.offset(0, columnWidth);
            canvas.drawText(wdays[i], location.left + headerTextOffset,
                    location.bottom - headerTextOffset, pTextHeader);
        }
    }

    private boolean justSkippedColumn = false;

    private void drawColumnHeader(Canvas canvas, Rect location, GregorianCalendar date)
    {
        String month = dfMonth.format(date.getTime());
        String year = dfYear.format(date.getTime());

        if (!month.equals(previousMonth))
        {
            int offset = 0;
            if (justPrintedYear)
            {
                offset += columnWidth;
                justSkippedColumn = true;
            }

            canvas.drawText(month, location.left + offset, location.bottom - headerTextOffset,
                    pTextHeader);

            previousMonth = month;
            justPrintedYear = false;
        }
        else if (!year.equals(previousYear))
        {
            if(!justSkippedColumn)
            {
                canvas.drawText(year, location.left, location.bottom - headerTextOffset, pTextHeader);
                previousYear = year;
                justPrintedYear = true;
            }

            justSkippedColumn = false;
        }
        else
        {
            justSkippedColumn = false;
            justPrintedYear = false;
        }
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent)
    {
        this.isBackgroundTransparent = isBackgroundTransparent;
        createColors();
    }
}
