package me.sweetll.tucao.widget;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RingDrawable extends Drawable implements Animatable {

    private int alpha = 255;

    private Paint paint = new Paint();

    private int rotate = 0;

    private RectF rectF;

    private float dashWidth;
    private float dashGap;

    private ValueAnimator rotateAnimator;

    public RingDrawable() {
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setPathEffect(new DashPathEffect(new float[] {10, 20}, 0));
    }

    @Override
    public void start() {
        if (isRunning()) return;
        if (rotateAnimator == null) {
            rotateAnimator = ValueAnimator.ofInt(0, 360);
            rotateAnimator.setInterpolator(new LinearInterpolator());
            rotateAnimator.setDuration(500);
            rotateAnimator.setRepeatCount(-1);
            rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    rotate = (int) animation.getAnimatedValue();
                    invalidateSelf();
                }
            });
        }
        rotateAnimator.start();
    }

    @Override
    public void stop() {
        if (isRunning()) {
            rotateAnimator.cancel();
        }
    }

    @Override
    public boolean isRunning() {
        return rotateAnimator != null && rotateAnimator.isRunning();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        rectF = new RectF(left, top, right, bottom);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float x = rectF.centerX();
        float y = rectF.centerY();
        canvas.save();
        canvas.rotate(rotate, x, y);
        canvas.drawArc(rectF, 0, 220, false, paint);
        canvas.restore();
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
