package com.example.huangzilin.third;

public class Pos {
    private String position;
    private float x;
    private float y;
    public Pos(String position, float x, float y){
        this.position = position;
        this.x = x;
        this.y = y;
    }
    String getPosition(){
        return this.position;
    }
    float getX(){
        return this.x;
    }
    float getY(){
        return this.y;
    }
}
