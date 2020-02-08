package com.example.huangzilin.third;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by isszym on 2017/5/6.
 */

public class PathView extends View {
    Context context;
    Path path;
    Paint paint;
    boolean isClear = false;
    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.path = new Path();
        this.paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(30);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.SOLID));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAlpha(60);
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        if(!isClear){
            canvas.drawPath(path,paint);
        }
    }

    public void getPoint(float x, float y){
        path.moveTo(x, y);
        invalidate();
    }

    public void getPath(float x, float y){
        path.lineTo(x, y);
        invalidate();
    }

    public void clear(){
        isClear = true;
        path.reset();
        invalidate();
    }

    public void back(){
        this.isClear = false;
    }
}
