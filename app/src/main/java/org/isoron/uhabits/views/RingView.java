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
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DialogHelper;
import org.isoron.uhabits.R;

public class RingView extends View
{

    private int size;
    private int color;
    private float percentage;
    private Paint pRing;
    private float lineHeight;
    private String label;
    private RectF rect;

    public RingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.size = (int) context.getResources().getDimension(R.dimen.small_square_size) * 4;
        this.label = DialogHelper.getAttribute(context, attrs, "label");
        this.color = ColorHelper.palette[7];
        this.percentage = 0.75f;
        init();
    }

    public void setColor(int color)
    {
        this.color = color;
        pRing.setColor(color);
        postInvalidate();
    }

    public void setPercentage(float percentage)
    {
        this.percentage = percentage;
        postInvalidate();
    }

    private void init()
    {
        pRing = new Paint();
        pRing.setAntiAlias(true);
        pRing.setColor(color);
        pRing.setTextAlign(Paint.Align.CENTER);
        pRing.setTextSize(size * 0.2f);
        lineHeight = pRing.getFontSpacing();
        rect = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(size, size + (int) (2 * lineHeight));
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        float thickness = size * 0.15f;

        pRing.setColor(color);
        rect.set(0, 0, size, size);
        canvas.drawArc(rect, -90, 360 * percentage, true, pRing);

        pRing.setColor(Color.rgb(230, 230, 230));
        canvas.drawArc(rect, 360 * percentage - 90 + 2, 360 * (1 - percentage) - 4, true, pRing);

        pRing.setColor(Color.WHITE);
        rect.inset(thickness, thickness);
        canvas.drawArc(rect, -90, 360, true, pRing);

        pRing.setColor(Color.GRAY);
        pRing.setTextSize(size * 0.2f);
        canvas.drawText(String.format("%.0f%%", percentage * 100), rect.centerX(),
                rect.centerY() + lineHeight / 3, pRing);

        pRing.setTextSize(size * 0.15f);
        canvas.drawText(label, size / 2, size + lineHeight * 1.2f, pRing);
    }
}
