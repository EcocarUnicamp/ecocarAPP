package com.ecocarunicamp.ecoapp;

/**
 * Created by ricardonagaishi on 28/07/16.
 */
public class Information {

    private int id;
    private int run;
    private int time;
    private double speed;
    private double inclination;
    private double energy;
    private double power;
    private double gpsx;
    private double gpsy;

    public Information(int id, int run, int time, double speed, double inclination, double energy, double power, double gpsx, double gpsy){
        this.id = id;
        this.run = run;
        this.time = time;
        this.speed = speed;
        this.inclination = inclination;
        this.energy = energy;
        this.power = power;
        this.gpsx = gpsx;
        this.gpsy = gpsy;
    }

    public int getId() {
        return id;
    }

    public double getEnergy() {
        return energy;
    }

    public double getPower() {
        return power;
    }

    public int getTime() {
        return time;
    }

    public double getInclination() {
        return inclination;
    }

    public double getSpeed() {
        return speed;
    }

    public int getRun() {
        return run;
    }

    public double getGpsx() {
        return gpsx;
    }

    public double getGpsy() {
        return gpsy;
    }
}
