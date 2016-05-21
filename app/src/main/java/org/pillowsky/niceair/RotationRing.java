package org.pillowsky.niceair;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RotationRing extends View {
    private static final String TAG = "RotationRing";

    private float value = 0;
    private float startAngle = 0;
    private float spanAngle = 120;
    private float ringWidth = 6;
    private int ringColor = Color.MAGENTA;
    private int backColor = Color.GRAY;
    private Paint ringPaint;
    private Paint backPaint;
    private RectF rectF;
    private ObjectAnimator animator;

    public RotationRing(Context context, AttributeSet attrs) {
        super(context, attrs);
        rectF = new RectF();
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RotationRing, 0, 0);

        try {
            startAngle = typedArray.getFloat(R.styleable.RotationRing_startAngle, startAngle);
            spanAngle = typedArray.getFloat(R.styleable.RotationRing_spanAngle, spanAngle);
            ringWidth = typedArray.getDimension(R.styleable.RotationRing_ringWidth, ringWidth);
            ringColor = typedArray.getInt(R.styleable.RotationRing_ringColor, ringColor);
            backColor = typedArray.getInt(R.styleable.RotationRing_backgroundColor, backColor);
        } finally {
            typedArray.recycle();
        }

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setColor(ringColor);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(ringWidth);

        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setColor(backColor);
        backPaint.setStyle(Paint.Style.STROKE);
        backPaint.setStrokeWidth(ringWidth);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
        invalidate();
    }

    public float getSpanAngle() {
        return spanAngle;
    }

    public void setSpanAngle(float spanAngle) {
        this.spanAngle = spanAngle;
        invalidate();
    }

    public float getRingWidth() {
        return ringWidth;
    }

    public void setRingWidth(float ringWidth) {
        this.ringWidth = ringWidth;
        ringPaint.setStrokeWidth(ringWidth);
        backPaint.setStrokeWidth(ringWidth);
        invalidate();
        requestLayout();
    }

    public int getRingColor() {
        return ringColor;
    }

    public void setRingColor(int ringColor) {
        this.ringColor = ringColor;
        ringPaint.setColor(ringColor);
        invalidate();
    }

    public int getBackColor() {
        return backColor;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
        backPaint.setColor(backColor);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        rectF.set(0 + ringWidth / 2, 0 + ringWidth / 2, min - ringWidth / 2, min - ringWidth / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(rectF, backPaint);
        canvas.drawArc(rectF, startAngle, spanAngle, false, ringPaint);
    }

    public void startRotation() {
        Log.d(TAG, "startRotation");
        if (animator != null) {
            animator.cancel();
        } else {
            animator = ObjectAnimator.ofFloat(this, "startAngle", 0, 360);
        }
        animator.setDuration(2000);
        animator.setRepeatMode(ObjectAnimator.RESTART);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public void stopRotation() {
        if (animator != null) {
            animator.cancel();
        }
    }

}
