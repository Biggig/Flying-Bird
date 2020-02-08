package com.example.huangzilin.third;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.IntArrayEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private ImageView point;
    private AnimationDrawable bird;
    private AnimationDrawable red_point;
    private ObjectAnimator objectAnimator_replay;
    private String position;
    private String savedPosition;
    private boolean isRecorded;
    private boolean isBack;
    private int screenHeight;
    private int screenWidth;
    private double tol_distance;
    private boolean isFlying;
    private ArrayList<Pos> path_;
    private PathView full;
    private Path path;
    float x_start = 0;
    float y_start = 0;
    float x_final = 0;
    float y_final = 0;
    float saved_x = 0;
    float saved_y = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.bird);
        point = (ImageView) findViewById(R.id.point);
        full = (PathView) findViewById(R.id.full);
        bird = (AnimationDrawable) imageView.getBackground();
        bird.start();

        red_point = (AnimationDrawable) point.getBackground();
        red_point.start();
        position = "right";
        isRecorded = false;
        isBack = false;
        isFlying = false;
        screenHeight = full.getBottom();
        screenWidth = full.getRight();

        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight= wm.getDefaultDisplay().getHeight();
        tol_distance = Math.sqrt(Math.pow(screenHeight, 2) + Math.pow(screenWidth, 2));
        objectAnimator_replay = new ObjectAnimator();
    }


    public void turn(float start, float end, long duration){
        ObjectAnimator objectAnimator = new ObjectAnimator();
        if(end > start && position.equals("left")){
            objectAnimator.ofFloat(imageView,"rotationY", 180, 360)
                    .setDuration(duration)
                    .start();
            position = "right";
        }
        else if(end < start && position.equals("right")){
            objectAnimator.ofFloat(imageView,"rotationY", 0, 180)
                    .setDuration(duration)
                    .start();
            position = "left";
        }
    }

    public void fly_replay(float x_start, float x_final, float y_start, float y_final, final int index, final long duration){
        if(isBack){
            path = new Path();
            double cur_distance = Math.sqrt(Math.pow(x_final - x_start, 2) + Math.pow(y_final - y_start, 2));
            long cur_duration = Math.round(cur_distance / tol_distance * duration);
            path.moveTo(x_start,y_start);
            path.lineTo(x_final, y_final);
            objectAnimator_replay = new ObjectAnimator();
            turn(x_start, x_final, duration/10);
            //Log.d("position", String.valueOf(position));
            objectAnimator_replay = ObjectAnimator.ofFloat(imageView,"translationX", "translationY",path);
            objectAnimator_replay.setDuration(cur_duration);
            objectAnimator_replay.setStartDelay(duration/10);
            objectAnimator_replay.addListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationStart(Animator animation){}
                @Override
                public void onAnimationEnd(Animator animator){
                    if(isBack){
                        if(index < path_.size()-1){
                            //Log.d("index", String.valueOf(index));
                            Pos start = path_.get(index);
                            Pos end = path_.get(index + 1);
                            full.getPath(end.getX() + imageView.getRight()/2, end.getY()+ imageView.getBottom()/2);
                            fly_replay(start.getX(), end.getX(), start.getY(), end.getY(), index + 1,duration);
                        }
                        else{
                            isBack = false;
                            isFlying = false;
                            stateBack();
                        }
                    }
                }
                @Override
                public void onAnimationCancel(Animator animator){
                }
                @Override
                public void onAnimationRepeat(Animator animator){}
            });
            objectAnimator_replay.start();
            if(!isBack){
                objectAnimator_replay.cancel();
                stateBack();
            }
        }
    }

    public void fly(float x_start, float x_final, float y_start, float y_final, final long duration){
        isFlying = true;
        path = new Path();
        double cur_distance = Math.sqrt(Math.pow(x_final - x_start, 2) + Math.pow(y_final - y_start, 2));
        long cur_duration = Math.round(cur_distance / tol_distance * duration);

        if(isRecorded){
            Pos pos = new Pos(position, x_final, y_final);
            path_.add(pos);
        }
        path.moveTo(x_start,y_start);
        path.lineTo(x_final, y_final);
        ObjectAnimator objectAnimator = new ObjectAnimator();
        turn(x_start, x_final, duration/10);
        //Log.d("position", String.valueOf(position));
        objectAnimator = ObjectAnimator.ofFloat(imageView,"translationX", "translationY",path);
        objectAnimator.setDuration(cur_duration);
        objectAnimator.setStartDelay(duration/10);
        objectAnimator.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation){}
            @Override
            public void onAnimationEnd(Animator animator){
                isFlying = false;
            }
            @Override
            public void onAnimationCancel(Animator animator){}
            @Override
            public void onAnimationRepeat(Animator animator){}
        });
        objectAnimator.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(!isBack && !isFlying){//飞行、录像时禁止触摸
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    x_final = event.getRawX() - imageView.getBottom();
                    y_final = event.getRawY() - imageView.getRight();

                    if(x_final <= 0){
                        x_final = 0;
                    }
                    else if(x_final > screenWidth){
                        x_final = screenWidth;
                    }
                    if(y_final <= 0){
                        y_final = 0;
                    }
                    else if(y_final > screenHeight){
                        y_final = screenHeight;
                    }

                    fly(x_start, x_final, y_start, y_final,5000);

                    x_start = x_final;
                    y_start = y_final;
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.support, menu);
        return true;
    }

    public void playBack(ArrayList<Pos> path_){
        //返回录像开始时状态
        Pos pos_ = path_.get(0);
        if(!position.equals(pos_.getPosition())){
            if(pos_.getPosition().equals("right")){
                turn(0, 1, 0);
            }
            else{
                turn(1, 0, 0);
            }
        }
        position = pos_.getPosition();
        x_start = pos_.getX();
        y_start = pos_.getY();
        Log.d("position", position);
        //Log.d("size", String.valueOf(path_.size()));
        Pos start = path_.get(0);
        Pos end = path_.get(1);
        isFlying = true;
        fly_replay(start.getX(), end.getX(), start.getY(), end.getY(), 1, 5000);
        isFlying = false;
        full.getPoint(start.getX() + imageView.getRight()/2, start.getY() + imageView.getBottom()/2);
        full.getPath(end.getX() + imageView.getRight()/2, end.getY()+ imageView.getBottom()/2);

    }

    public void stateBack(){
        full.clear();
        if(!position.equals(savedPosition)){
            if(savedPosition.equals("right")){
                turn(0, 1, 0);
            }
            else{
                turn(1, 0, 0);
            }
        }
        fly(x_final, saved_x, y_final, saved_y, 0);
        x_start = x_final = saved_x;
        y_start = y_final = saved_y;
        position = savedPosition;
        full.back();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                point.setVisibility(View.VISIBLE);
                isRecorded = true;
                path_ = new ArrayList<Pos>();
                Pos pos = new Pos(position, x_start, y_start);
                path_.add(pos);
                break;
            case R.id.menu2:
                point.setVisibility(View.GONE);
                isRecorded = false;
                break;
            case R.id.menu3:
                if(!isRecorded){
                    isBack = true;
                    //保存当前状态
                    saved_x = x_final;
                    saved_y = y_final;
                    savedPosition = position;

                    Log.d("x_final", String.valueOf(x_final));
                    Log.d("y_final", String.valueOf(y_final));
                    Log.d("position", position);
                    playBack(path_);
                }
                break;
            case R.id.menu4:
                isBack = false;
                objectAnimator_replay.cancel();
                stateBack();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
