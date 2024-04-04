package dev.dqw4w9wgxcq.pathfinder.pathfinder;

import java.awt.Point;


public class PlayerTime {
    private int time;
    private int x;
    private int y;

    private int actionObserved;
    private int plane;
    private int[] equip;

    public PlayerTime() {
        // Default constructor
    }

    public PlayerTime(int time, int x, int y, int actionObserved, int plane, int[] equip) {
        this.time = time;
       this.x = x;
       this.y = y;
        this.actionObserved = actionObserved;
        this.plane = plane;
        this.equip = equip;
    }

    public int getTime() {
        return time;
    }

    public int getX() {return x;}

    public int getY() {return y;}

    public int getActionObserved() {
        return actionObserved;
    }

    public int getPlane() {
        return plane;
    }

    public int[] getEquip() {
        return equip;
    }
}
