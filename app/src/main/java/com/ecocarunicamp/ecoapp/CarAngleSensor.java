package com.ecocarunicamp.ecoapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;


/**
 * Created by lucas on 7/25/16.
 */
public class CarAngleSensor implements SensorEventListener {

    private MovingAveregeBuffer buffer;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private TextView display;
    private boolean isRunning;
    private double inclination;
    private double aceleration;

    public CarAngleSensor(SensorManager sensorManager, TextView display, int bufferSize){

        this.buffer = new MovingAveregeBuffer(bufferSize);
        this.sensorManager = sensorManager;
        this.accelerometerSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.display = display;
    }


    public void pause(){
        this.isRunning = false;
        this.sensorManager.unregisterListener(this);
    }

    public void start(){
        this.isRunning = true;
        this.sensorManager.registerListener(this, this.accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void reset(int bufferSize){
        this.isRunning = false;
        this.buffer = new MovingAveregeBuffer(bufferSize);

    }

    @Override
    public void onSensorChanged(SensorEvent event){
        double deviceAngle;

        float carAngle;

        deviceAngle = Math.acos(event.values[1]/9.8);
        carAngle = (float)(180*deviceAngle/Math.PI);
        carAngle = this.buffer.add(90 - carAngle);
        inclination = carAngle;
        this.display.setText(Float.toString(carAngle));

        aceleration = Math.sqrt(Math.pow(event.values[0], 2)+Math.pow(event.values[1], 2)+Math.pow(event.values[2], 2));

        aceleration = this.buffer.add((float)aceleration);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    public double getInclination() {
        return inclination;
    }

    public double getAceleration() { return aceleration; };
}
