package com.android.jay.pandorabox.Item;

/**
 * Created by H151136 on 9/14/2016.
 */
public class SettingItem {
    private int index;
    private float speed;
    private float keep;
    private int time;

    public SettingItem(int index, float speed, float keep, int time) {
        this.index = index;
        this.speed = speed;
        this.keep = keep;
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getKeep() {
        return keep;
    }

    public void setKeep(float keep) {
        this.keep = keep;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
