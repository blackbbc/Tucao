package me.sweetll.tucao.widget;

import android.graphics.Canvas;
import android.graphics.Paint;

import android.animation.ValueAnimator;

import java.util.ArrayList;

import me.sweetll.tucao.extension.FloatExtensionsKt;

/**
 * Created by Jack on 2015/10/16.
 */
public class BallPulseIndicator extends Indicator {

    public static final float SCALE = 1.0f;

    //scale x ,y
    private float[] scaleFloats = new float[]{
            SCALE,
            SCALE,
            SCALE};


    @Override
    public void draw(Canvas canvas, Paint paint) {
        float circleSpacing = FloatExtensionsKt.dp2px(4f);
        float radius = (getHeight() - strokeWidth * 2) / 2;
        float x = getWidth() / 2 - (radius * 2 + circleSpacing + 2 * strokeWidth);
        float y = getHeight() / 2;
        for (int i = 0; i < 3; i++) {
            canvas.save();
            float translateX = x + (radius * 2) * i + circleSpacing * i * strokeWidth * 2 * i;
            canvas.translate(translateX, y);
            canvas.scale(scaleFloats[i], scaleFloats[i]);
            canvas.drawCircle(0, 0, radius, paint);
            canvas.restore();
        }
    }

    @Override
    public ArrayList<ValueAnimator> onCreateAnimators() {
        ArrayList<ValueAnimator> animators = new ArrayList<>();
        int[] delays = new int[]{200, 400, 600};
        for (int i = 0; i < 3; i++) {
            final int index = i;

            ValueAnimator scaleAnim = ValueAnimator.ofFloat(1, 0f, 1);

            scaleAnim.setDuration(800);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);

            addUpdateListener(scaleAnim, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scaleFloats[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            animators.add(scaleAnim);
        }
        return animators;
    }


}