package me.sweetll.tucao.widget;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.view.animation.PathInterpolatorCompat;

import java.util.ArrayList;

/**
 * Created by Jack on 2015/10/16.
 */
public class BallPulseIndicator extends Indicator {

    public static final float SCALE = 0f;
    public static final int ALPHA = 0;

    //scale x ,y
    private float[] scaleFloats = new float[] {SCALE, SCALE, SCALE};
    private int[] alphaInts = new int[] {ALPHA, ALPHA, ALPHA};


    @Override
    public void draw(Canvas canvas, Paint paint) {
        float x = getWidth() / 2 - (radius * 2 + circleSpacing + 2 * strokeWidth);
        float y = getHeight() / 2;
        for (int i = 0; i < 3; i++) {
            paint.setAlpha(alphaInts[i]);
            canvas.save();
            float translateX = x + (radius * 2) * i + circleSpacing * i + strokeWidth * 2 * i;
            canvas.translate(translateX, y);
            canvas.scale(scaleFloats[i], scaleFloats[i]);
            canvas.drawCircle(0, 0, radius, paint);
            canvas.restore();
        }
    }

    @Override
    public ArrayList<ValueAnimator> onCreateAnimators() {
        ArrayList<ValueAnimator> animators = new ArrayList<>();
        int[] delays = new int[]{0, 300, 600};
        for (int i = 0; i < 3; i++) {
            final int index = i;

            ValueAnimator scaleAnim = ValueAnimator.ofFloat(0, 1f, 0);

            scaleAnim.setDuration(1000);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);
            scaleAnim.setInterpolator(PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f));

            addUpdateListener(scaleAnim, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scaleFloats[index] = (float) animation.getAnimatedValue();
                    alphaInts[index] =  (int)(255 * (float)animation.getAnimatedValue());
                    postInvalidate();
                }
            });
            animators.add(scaleAnim);
        }
        return animators;
    }


}