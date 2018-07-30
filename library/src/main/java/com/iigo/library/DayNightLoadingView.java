package com.iigo.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author SamLeung
 * @e-mail 729717222@qq.com
 * @github https://github.com/samlss
 * @csdn https://blog.csdn.net/Samlss
 * @description  The day and night loading view.
 */
public class DayNightLoadingView extends View {
    //sun or moon state: raise
    private static final int RAISE = 0;
    //sun or moon state: fall
    private static final int FALL  = 1;

    //alpha component [0..255]
    private static final int MIN_ALPHA_COMPONENT = 0;
    private static final int MAX_ALPHA_COMPONENT = 255;

    //the default background color
    private int DEFAULT_BG_COLOR = Color.parseColor("#062734");
    //the default color of sun , moon, stars
    private int DEFAULT_MAIN_COLOR = Color.parseColor("#00f99e");

    //the stars count
    private final static int STAR_COUNT = 20;

    // to record the end position of the stars
    private List<PointF> starsEndPosPointList = new ArrayList(STAR_COUNT);

    //This defines the animators duration.
    private static final int MOON_ANIMATOR_DURATION = 2000; //2000ms
    private static final int SUN_ANIMATOR_DURATION = 2000; //2000ms
    private static final int STAR_ANIMATOR_DURATION = 2000; //2000ms
    private static final int STAR_SHOW_START_DELAY = 400; //400ms
    private static final int STAR_FLASH_ANIMATOR_DURATION = 400; //400ms

    private static final int SUNSHINE_ROTATE_ANIMATOR_DURATION = 3000; //3000ms

    //The ARC constant
    private static final double SUN_ARC_LENGTH = Math.PI * 2;
    private static final double SUN_ARC_LENGTH_OFFSET = Math.PI * 2 / 12d;

    //the start moving distance of sun or moon
    private float sunMoonStartMovingDistance;

    //The gap between the sun and the light
    private float sunshineGap;

    //The max length of the line of sunshine
    private float maxSunshineLineLength;

    //the path of stars, they have the same path
    private Path starPath;

    //the radius of sun and moon
    private float sunAndMoonRadius;

    //the computational star radius, the true star radius is 2 * starRadius
    private float starRadius;
    private int sunAndMoonStroke;

    //this defines the paints
    private Paint sunPaint;
    private Paint moonPaint;
    private Paint sunShinePaint;
    private Paint starPaint;

    //center position
    private float centerX;
    private float centerY;

    // Used to calculate the start and end points of each line of sunshine.
    private double sunshineStartX, sunshineStartY, sunshineStopX, sunshineStopY;

    //The animators
//    private ValueAnimator sunMoonAnimator;
//    private ValueAnimator starAnimator;

    private AnimatorActuator sunMoonAnimatorActuator;
    private AnimatorActuator starAnimatorActuator;

    //The attr's defines
    private int sunColor = DEFAULT_MAIN_COLOR;
    private int sunshineColor = DEFAULT_MAIN_COLOR;
    private int moonColor = DEFAULT_MAIN_COLOR;
    private int starColor = DEFAULT_MAIN_COLOR;
    private int bgColor = DEFAULT_BG_COLOR;

    public DayNightLoadingView(Context context) {
        super(context);

        init();
    }

    public DayNightLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        parseAttr(attrs);
        init();
    }

    public DayNightLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        parseAttr(attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DayNightLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        parseAttr(attrs);
        init();
    }

    private void parseAttr(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DayNightLoadingView);
        sunColor  = typedArray.getColor(R.styleable.DayNightLoadingView_sun_color, DEFAULT_MAIN_COLOR);
        sunshineColor = typedArray.getColor(R.styleable.DayNightLoadingView_sunshine_color, DEFAULT_MAIN_COLOR);
        moonColor = typedArray.getColor(R.styleable.DayNightLoadingView_moon_color, DEFAULT_MAIN_COLOR);
        starColor = typedArray.getColor(R.styleable.DayNightLoadingView_star_color, DEFAULT_MAIN_COLOR);
        bgColor   = typedArray.getColor(R.styleable.DayNightLoadingView_background_color, DEFAULT_BG_COLOR);
        typedArray.recycle();
    }

    private void init(){
        sunPaint = new Paint();
        sunPaint.setColor(sunColor);
        sunPaint.setAntiAlias(true);
        sunPaint.setStyle(Paint.Style.STROKE);

        moonPaint = new Paint();
        moonPaint.setColor(moonColor);
        moonPaint.setAntiAlias(true);
        moonPaint.setStyle(Paint.Style.STROKE);

        sunShinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunShinePaint.setStyle(Paint.Style.STROKE);
        sunShinePaint.setStrokeCap(Paint.Cap.ROUND);
        sunShinePaint.setStrokeJoin(Paint.Join.ROUND);
        sunShinePaint.setColor(sunshineColor);

        starPaint = new Paint();
        starPaint.setColor(starColor);
        starPaint.setAntiAlias(true);
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setPathEffect(new CornerPathEffect(8));

        starPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stop();

        int minSize = Math.min(w, h);

        sunAndMoonRadius = minSize / 12f;
        sunAndMoonStroke = (int) (sunAndMoonRadius / 10f);
        sunAndMoonRadius -= sunAndMoonStroke * 2;

        starRadius = sunAndMoonRadius / 5f;
        initStarPath();

        sunshineGap = sunAndMoonRadius / 3f;
        maxSunshineLineLength =  sunAndMoonRadius / 2.5f;

        sunMoonStartMovingDistance = sunAndMoonRadius + sunAndMoonStroke + 10;

        sunPaint.setStrokeWidth(sunAndMoonStroke);
        moonPaint.setStrokeWidth(sunAndMoonStroke);
        sunShinePaint.setStrokeWidth(sunAndMoonStroke);

        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(bgColor);

        if(sunMoonAnimatorActuator == null){
            sunMoonAnimatorActuator = new SunTranslateAnimatorActuator(RAISE, 0);
        }

        //draw stars firstly.
        if (starAnimatorActuator != null){
            starAnimatorActuator.draw(canvas);
        }

        sunMoonAnimatorActuator.draw(canvas);
    }

    private void drawSun(Canvas canvas, float y){
        canvas.drawCircle(centerX, y, sunAndMoonRadius, sunPaint);
    }

    private void drawSunshine(Canvas canvas, float startLength, float stopLength, float currentRotation, float y){
        for (float a = 0; a <= SUN_ARC_LENGTH; a += SUN_ARC_LENGTH_OFFSET) {
            sunshineStartX = startLength * Math.cos(a+currentRotation) + centerX;
            sunshineStartY = startLength * Math.sin(a+currentRotation) + y;

            sunshineStopX = stopLength * Math.cos(a+currentRotation) + centerX;
            sunshineStopY = stopLength * Math.sin(a+currentRotation) + y;

            canvas.drawLine((float) sunshineStartX, (float) sunshineStartY, (float) sunshineStopX, (float) sunshineStopY, sunShinePaint);
        }
    }

    private class SunTranslateAnimatorActuator extends AnimatorActuator{
        private float animatedValue;
        private float currentRotation;

        public SunTranslateAnimatorActuator(final int action, float currentRotation) {
            this.currentRotation = currentRotation;

            releaseSunMoonAnimator();

            valueAnimator = ValueAnimator.ofFloat(action == RAISE ? 0f : 1f,
                    action == RAISE ? 1f : 0f);

            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    animatedValue = (float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (valueAnimator != null && valueAnimator.getAnimatedFraction() < 1){
                        return;
                    }

                    if (action == RAISE) {
                        sunMoonAnimatorActuator = new SunshineRotationAnimatorActuator();
                    }else{
                        sunMoonAnimatorActuator = new MoonAnimatorActuator(RAISE);
                    }
                }
            });

            if (action == RAISE) {
                valueAnimator.setInterpolator(new DecelerateInterpolator());
            }else{
                valueAnimator.setInterpolator(new AccelerateInterpolator());
            }

            valueAnimator.setDuration(SUN_ANIMATOR_DURATION);
            valueAnimator.start();
        }

        @Override
        public void performDraw(Canvas canvas) {
            float y = (centerY * 2 + sunMoonStartMovingDistance) - animatedValue * (centerY + sunMoonStartMovingDistance);

            drawSun(canvas, y);

            float startLength = sunshineGap + sunAndMoonRadius;
            float stopLength  = sunshineGap + animatedValue * maxSunshineLineLength + sunAndMoonRadius;

            drawSunshine(canvas, startLength, stopLength, currentRotation, y);
        }
    }

    private class SunshineRotationAnimatorActuator extends AnimatorActuator{
        private float animatedValue;

        public SunshineRotationAnimatorActuator() {
            releaseSunMoonAnimator();

            valueAnimator = ValueAnimator.ofFloat(0, (float) SUN_ARC_LENGTH);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    animatedValue = (float) valueAnimator.getAnimatedValue();
                    if (valueAnimator.getAnimatedFraction() >= 0.6f){
                        sunMoonAnimatorActuator = new SunTranslateAnimatorActuator(FALL, animatedValue);
                        starAnimatorActuator = new StarTranslateAnimatorActuator(RAISE);
                    }else{
                        invalidate();
                    }
                }
            });

            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.setDuration(SUNSHINE_ROTATE_ANIMATOR_DURATION);
            valueAnimator.start();
        }


        @Override
        public void performDraw(Canvas canvas) {
            drawSun(canvas,centerY);

            float startLength = sunshineGap + sunAndMoonRadius;
            float stopLength  = startLength + maxSunshineLineLength;

            drawSunshine(canvas, startLength, stopLength, animatedValue, centerY);
        }
    }

    private class MoonAnimatorActuator extends AnimatorActuator{
        private float animatedFraction;
        private float animatedValue;
        private Path moonPath = new Path();
        private Matrix moonMatrix = new Matrix();
        private float currentRotation = 0;
        private int action;

        public MoonAnimatorActuator(final int action) {
            this.action = action;
            createAnimator();
        }

        private void createAnimator(){
            releaseSunMoonAnimator();

            valueAnimator = ValueAnimator.ofFloat(action == RAISE ? 0f : 1f,
                    action == RAISE ? 1f : 0f);

            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    animatedValue = (float) valueAnimator.getAnimatedValue();
                    animatedFraction = valueAnimator.getAnimatedFraction();
                    invalidate();
                }
            });

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (valueAnimator != null && valueAnimator.getAnimatedFraction() < 1){
                        return;
                    }

                    if (action == RAISE) {
                        action = FALL;
                        animatedFraction = 0;
                        createAnimator();
                        starAnimatorActuator = new StarTranslateAnimatorActuator(FALL);
                    }else{
                        releaseStarAnimator();
                        sunMoonAnimatorActuator = new SunTranslateAnimatorActuator(RAISE, 0);
                    }
                }
            });

            if (action == RAISE) {
                valueAnimator.setInterpolator(new DecelerateInterpolator());
            }else{
                valueAnimator.setInterpolator(new AccelerateInterpolator());
            }
            valueAnimator.setDuration(MOON_ANIMATOR_DURATION);
            valueAnimator.start();
        }

        @Override
        public void performDraw(Canvas canvas) {
            moonPath.reset();
            moonMatrix.reset();

            float y = centerY * 2 + sunAndMoonRadius + sunAndMoonStroke + 10 - animatedValue * (centerY + sunAndMoonRadius + sunAndMoonStroke + 10);

            RectF rectFOuter = new RectF(centerX - sunAndMoonRadius, y - sunAndMoonRadius, centerX + sunAndMoonRadius, y + sunAndMoonRadius);
            RectF rectFInner = new RectF(centerX - sunAndMoonRadius / 3, y - sunAndMoonRadius, centerX + sunAndMoonRadius / 3, y + sunAndMoonRadius);

            moonPath.addArc(rectFInner, -90, 180);
            moonPath.addArc(rectFOuter, -90, 180);

            if (action == RAISE && animatedFraction > 0.8f) {
                currentRotation+=0.6f;
            }

            if (action == FALL && animatedFraction < 0.2f){
                currentRotation += 0.35f;
            }

            moonMatrix.postRotate(currentRotation, centerX, y);
            moonPath.transform(moonMatrix);

            canvas.drawPath(moonPath, moonPaint);
        }
    }

    private void initStarPath(){
        starPath.reset();
        List<PointF> starPoints = new ArrayList<>();

        float R = starRadius * 2;
        float r = starRadius;

        for (int i = 0; i < 5; i++){
            //outer
            double outerDoc = (18 + 72 * i) / 180d * Math.PI;
            PointF pointF1 = new PointF((float) (Math.cos(outerDoc) * R) ,
                    - (float) (Math.sin(outerDoc) * R));

            //inner
            double innerDoc = (54 + 72 * i) / 180d * Math.PI;
            PointF pointF2 = new PointF((float)(Math.cos(innerDoc) * r) ,
                    -(float) (Math.sin(innerDoc) * r));

            starPoints.add(pointF1);
            starPoints.add(pointF2);
        }

        starPath.moveTo(starPoints.get(0).x, starPoints.get(0).y);
        starPath.lineTo(starPoints.get(1).x, starPoints.get(1).y);
        starPath.lineTo(starPoints.get(2).x, starPoints.get(2).y);
        starPath.lineTo(starPoints.get(3).x, starPoints.get(4).y);
        starPath.lineTo(starPoints.get(4).x, starPoints.get(4).y);
        starPath.lineTo(starPoints.get(5).x, starPoints.get(5).y);
        starPath.lineTo(starPoints.get(6).x, starPoints.get(6).y);
        starPath.lineTo(starPoints.get(7).x, starPoints.get(7).y);
        starPath.lineTo(starPoints.get(8).x, starPoints.get(8).y);
        starPath.lineTo(starPoints.get(9).x, starPoints.get(9).y);
        starPath.lineTo(starPoints.get(0).x, starPoints.get(0).y);

        Matrix matrix = new Matrix();
        matrix.postTranslate(0, -R);
        starPath.transform(matrix);
    }

    private class StarTranslateAnimatorActuator extends AnimatorActuator{
        private int action;

        private float maxY = 0;
        private float[] starsTranslateArr = new float[20];

        private AnimatorSet animatorSet = new AnimatorSet();

        public StarTranslateAnimatorActuator(int action){
            this.action = action;
            releaseStarAnimator();

            if (action == RAISE){
                initStars();
            }

            setupAnimator();
        }

        private void setupAnimator(){
            List<Animator> valueAnimators = new ArrayList<>();

            for (int i = 0; i < STAR_COUNT; i++){
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
                if (action == RAISE) {
                    long duration = (long) (STAR_ANIMATOR_DURATION * starsEndPosPointList.get(i).y / maxY);
                    if (duration < STAR_ANIMATOR_DURATION - 500){
                        valueAnimator.setStartDelay(getRandom(100, 500));
                    }
                    valueAnimator.setDuration(duration);
                }else{
                    valueAnimator.setDuration(getRandom(STAR_ANIMATOR_DURATION - 500, STAR_ANIMATOR_DURATION));
                }

                final int finalI = i;
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        if (action == RAISE) {
                            starsTranslateArr[finalI] = value * starsEndPosPointList.get(finalI).y;
                        }else{
                            starsTranslateArr[finalI] = starsEndPosPointList.get(finalI).y + value * (getHeight() - starsEndPosPointList.get(finalI).y + sunMoonStartMovingDistance * 2);
                        }
                        invalidate();
                    }
                });

                valueAnimators.add(valueAnimator);
            }

            if (action == RAISE){
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        starAnimatorActuator = new StarFlashAnimatorActuator();
                    }
                });

                animatorSet.setStartDelay(STAR_SHOW_START_DELAY);
                animatorSet.setInterpolator(new DecelerateInterpolator());
            }else{
                animatorSet.setInterpolator(new AccelerateInterpolator());
            }


            animatorSet.playTogether(valueAnimators);
            animatorSet.start();
        }

        private void initStars(){
            starsEndPosPointList.clear();
            float averageWidth = getMeasuredWidth() * 1.0f / STAR_COUNT;
            float firstX = averageWidth / 2;

            for (int i = 0; i < STAR_COUNT; i++){
                float x = firstX + i * averageWidth;

                PointF endPos;
                if (isInMoonRect(x)){
                    int startY = (int) starRadius * 2 * 2;
                    int endY = (int) (centerY - (sunAndMoonRadius + sunAndMoonRadius));

                    endPos  = new PointF(x, getRandom(startY, endY));
                }else{
                    endPos  = new PointF(x, getRandom(getHeight() / 4, getHeight() * 3 / 4));
                }

                if (endPos.y > maxY){
                    maxY = endPos.y;
                }

                starsEndPosPointList.add(endPos);
            }
        }

        private int getRandom(int start, int end){
            if (start >= end){
                return 0;
            }
            return start + new Random().nextInt(end - start);
        }

        private boolean isInMoonRect(float x){
            return x >= (centerX - (sunAndMoonRadius + sunAndMoonRadius))
                    && x <= (centerX  + sunAndMoonRadius + sunAndMoonRadius);
        }

        @Override
        public void performDraw(Canvas canvas) {
            for (int i = 0; i < STAR_COUNT; i++) {
                canvas.save();
                starPaint.setAlpha(MAX_ALPHA_COMPONENT);

                canvas.translate(starsEndPosPointList.get(i).x, starsTranslateArr[i]);

                canvas.drawPath(starPath, starPaint);
                canvas.restore();
            }
        }

        @Override
        public void resume() {
            if (animatorSet != null){
                animatorSet.resume();
            }
        }

        @Override
        public void pause() {
            if (animatorSet != null){
                animatorSet.pause();
            }
        }


        @Override
        public void release() {
            if (animatorSet != null) {
                animatorSet.removeAllListeners();
                animatorSet.cancel();
                animatorSet = null;
            }
        }
    }

    private class StarFlashAnimatorActuator extends AnimatorActuator{
        private int animatedValue;
        private int index;

        public StarFlashAnimatorActuator(){
            index = getRandomIndex();

            setupAnimator();
        }

        private int getRandomIndex(){
            return new Random().nextInt(STAR_COUNT);
        }

        private void setupAnimator(){
            releaseStarAnimator();

            valueAnimator = ValueAnimator.ofInt(MAX_ALPHA_COMPONENT, MIN_ALPHA_COMPONENT);

            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    animatedValue = (int) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    int curIndex = getRandomIndex();
                    if (curIndex == index){
                        if (index == STAR_COUNT - 1){
                            index--;
                        }else{
                            index++;
                        }
                    }else{
                        index = curIndex;
                    }
                }
            });

            valueAnimator.setDuration(STAR_FLASH_ANIMATOR_DURATION);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.start();
        }

        @Override
        public void performDraw(Canvas canvas) {
            for (int i = 0; i < STAR_COUNT; i++){
                canvas.save();
                canvas.translate(starsEndPosPointList.get(i).x,starsEndPosPointList.get(i).y);


                if (i == index){
                    starPaint.setAlpha(animatedValue);
                }else{
                    starPaint.setAlpha(255);
                }

                canvas.drawPath(starPath, starPaint);
                canvas.restore();
            }
        }
    }

    private abstract class AnimatorActuator{
        public ValueAnimator valueAnimator;

        public void draw(Canvas canvas){
            //you can do check here.
            performDraw(canvas);
        }

        public abstract void performDraw(Canvas canvas);

        public void pause(){
            if (valueAnimator != null) {
                valueAnimator.pause();
            }
        }

        public void resume(){
            if (valueAnimator != null) {
                valueAnimator.resume();
            }
        }

        public void release(){
            if (valueAnimator != null) {
                valueAnimator.removeAllUpdateListeners();
                valueAnimator.removeAllListeners();
                valueAnimator.cancel();
                valueAnimator = null;
            }
        }
    }

    private void releaseSunMoonAnimator(){
        if (sunMoonAnimatorActuator != null) {
            sunMoonAnimatorActuator.release();
        }
    }

    private void releaseStarAnimator(){
        if (starAnimatorActuator != null){
            starAnimatorActuator.release();
        }
    }

    //pause the animators
    public void pause(){
        if (sunMoonAnimatorActuator != null) {
            sunMoonAnimatorActuator.pause();
        }

        if (starAnimatorActuator != null) {
            starAnimatorActuator.pause();
        }
    }

    //resume the animators
    public void resume(){
        if (sunMoonAnimatorActuator != null) {
            sunMoonAnimatorActuator.resume();
        }

        if (starAnimatorActuator != null) {
            starAnimatorActuator.resume();
        }
    }

    //Stop and release.
    public void stop(){
        starsEndPosPointList.clear();
        releaseSunMoonAnimator();
        sunMoonAnimatorActuator = null;


        releaseStarAnimator();
        starAnimatorActuator = null;
    }
}

