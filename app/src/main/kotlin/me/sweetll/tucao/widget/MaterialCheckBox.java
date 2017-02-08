package me.sweetll.tucao.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import me.sweetll.tucao.R;

public class MaterialCheckBox extends View {

    private Paint paintBlue;
    private Paint paintWithe;
    private Paint paintCenter;

    private int borderColor = Color.GRAY;     //边框颜色
    private int backgroundColor = Color.BLUE; //填充颜色
    private int doneShapeColor = Color.WHITE; //对号颜色

    private int baseWidth;                    //checkbox 边框宽度
    private int borderWidth;
    private int width, height;                //控件宽高

    private float[] points = new float[8];    //对号的4个点的坐标

    private int DURATION = 200;               //动画时长
    private boolean checked;                  //选择状态
    private float correctProgress;            //划对号的进度

    private boolean drawRecting;
    private boolean isAnim;
    private OnCheckedChangeListener listener;

    private float padding;                    //内切圆的边据边框的距离

    private ValueAnimator cachedRectValueAnimator;
    private ValueAnimator cachedCorrectValueAnimator;

    public MaterialCheckBox(Context context) {
        this(context, null);
    }

    public MaterialCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * init
     *
     * @param context
     */
    private void init(Context context) {

        backgroundColor = getResources().getColor(R.color.colorPrimary);
        borderColor = getResources().getColor(R.color.divider);
        borderWidth = baseWidth = dp2px(0.5f);

        paintBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBlue.setColor(borderColor);
        paintBlue.setStrokeWidth(borderWidth);

        paintWithe = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWithe.setColor(doneShapeColor);
        paintWithe.setStrokeWidth(dp2px(1));

        paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenter.setColor(getResources().getColor(R.color.white));
        paintCenter.setStrokeWidth(borderWidth);

        drawRecting = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = width = Math.max(w, h);

        points[0] = 101 / 378f * width;
        points[1] = 0.5f * width;

        points[2] = 163 / 378f * width;
        points[3] = 251 / 378f * width;

        points[4] = 149 / 378f * width;
        points[5] = 250 / 378f * width;

        points[6] = 278 / 378f * width;
        points[7] = 122 / 378f * width;

        padding = 57 / 378f * width;

    }

    /**
     * draw checkbox
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        RectF rect = new RectF(padding, padding, width - padding, height - padding);
        canvas.drawRoundRect(rect, baseWidth, baseWidth, paintBlue);
        if (drawRecting) {
            canvas.drawRect(padding + borderWidth, padding + borderWidth, width - padding - borderWidth, height - padding - borderWidth, paintCenter);
        } else {
            //画对号
            if (correctProgress > 0) {
                if (correctProgress < 1 / 3f) {
                    float x = points[0] + (points[2] - points[0]) * correctProgress;
                    float y = points[1] + (points[3] - points[1]) * correctProgress;
                    canvas.drawLine(points[0], points[1], x, y, paintWithe);
                } else {
                    float x = points[4] + (points[6] - points[4]) * correctProgress;
                    float y = points[5] + (points[7] - points[5]) * correctProgress;
                    canvas.drawLine(points[0], points[1], points[2], points[3], paintWithe);
                    canvas.drawLine(points[4], points[5], x, y, paintWithe);
                }
            }
        }
    }


    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setDoneShapeColor(int doneShapeColor) {
        this.doneShapeColor = doneShapeColor;
        paintWithe.setColor(doneShapeColor);
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public void setBorderWidth(int baseWidth) {
        this.baseWidth = baseWidth;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if (this.checked == checked) return;
        this.checked = checked;
        if (checked) {
            showRect();
        } else {
            hideCorrect();
        }
    }

    private void hideRect() {
        if (isAnim) {
            if (cachedRectValueAnimator != null) {
                cachedRectValueAnimator.cancel();
            }
            if (cachedCorrectValueAnimator != null) {
                cachedCorrectValueAnimator.cancel();
            }
//            return;
        }
        isAnim = true;
        drawRecting = true;
        ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
        cachedRectValueAnimator = va;
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();
                float c = 1f - p;
                borderWidth = (int) (baseWidth + c * (width - baseWidth));
                paintBlue.setColor(evaluate(c, borderColor, backgroundColor));
                invalidate();
                if (p >= 1) {
                    isAnim = false;
                    if (listener != null) {
                        checked = false;
                        listener.onCheckedChanged(MaterialCheckBox.this, checked);
                    }
                }
            }
        });
        va.start();
    }

    private void showRect() {
        if (isAnim) {
            if (cachedRectValueAnimator != null) {
                cachedRectValueAnimator.cancel();
            }
            if (cachedCorrectValueAnimator != null) {
                cachedCorrectValueAnimator.cancel();
            }
//            return;
        }
        isAnim = true;
        drawRecting = true;
        ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
        cachedRectValueAnimator = va;
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();
                borderWidth = (int) (10 + p * (width - 10));
                paintBlue.setColor(evaluate(p, borderColor, backgroundColor));
                invalidate();
                if (p >= 1) {
                    isAnim = false;
                    drawRecting = false;
                    showCorrect();
                }
            }
        });
        va.start();
    }

    private void showCorrect() {
        if (isAnim) {
            if (cachedRectValueAnimator != null) {
                cachedRectValueAnimator.cancel();
            }
            if (cachedCorrectValueAnimator != null) {
                cachedCorrectValueAnimator.cancel();
            }
//            return;
        }
        isAnim = true;
        correctProgress = 0;
        drawRecting = false;
        ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
        cachedCorrectValueAnimator = va;
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                correctProgress = (float) animation.getAnimatedValue();
                invalidate();
                if (correctProgress >= 1) {
                    isAnim = false;
                    if (listener != null) {
                        checked = true;
                        listener.onCheckedChanged(MaterialCheckBox.this, checked);
                    }
                }
            }
        });
        va.start();
    }

    private void hideCorrect() {
        if (isAnim) {
            if (cachedRectValueAnimator != null) {
                cachedRectValueAnimator.cancel();
            }
            if (cachedCorrectValueAnimator != null) {
                cachedCorrectValueAnimator.cancel();
            }
//            return;
        }
        isAnim = true;
        correctProgress = 1;
        drawRecting = false;
        ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
        cachedCorrectValueAnimator = va;
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();
                correctProgress = 1f - p;
                invalidate();
                if (p >= 1) {
                    isAnim = false;
                    hideRect();
                }
            }
        });
        va.start();
    }

    public void setOnCheckedChangedListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    interface OnCheckedChangeListener {
        void onCheckedChanged(View view, boolean isChecked);
    }

    private int evaluate(float fraction, int startValue, int endValue) {
        int startInt = startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return ((startA + (int) (fraction * (endA - startA))) << 24)
                | ((startR + (int) (fraction * (endR - startR))) << 16)
                | ((startG + (int) (fraction * (endG - startG))) << 8)
                | ((startB + (int) (fraction * (endB - startB))));
    }

    public int dp2px(float value) {
        final float scale = getContext().getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }
}