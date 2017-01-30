package com.ecocarunicamp.ecoapp;

/**
 * Created by lucas on 7/22/16.
 */
public class MovingAveregeBuffer {
    float [] values;
    int indexToAdd;
    int bufferSize;


    public MovingAveregeBuffer(int bufferSize){
        this.values = new float[bufferSize];
        this.indexToAdd = 0;
        this.bufferSize = bufferSize;

    }

    public float add(float value){
        float avarege = 0f;
        this.values[this.indexToAdd] = value;
        this.indexToAdd++;
        if(this.indexToAdd == this.bufferSize){
            this.indexToAdd = 0;
        }
        for(int i = 0; i < this.bufferSize; i++){
            avarege += this.values[i];

        }

        return avarege/this.bufferSize;
    }
}

