package com.ecocarunicamp.ecoapp;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by root on 7/26/16.
 */
public class EcoTextView extends TextView {

    public EcoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(),"HunDIN1451.ttf"));
    }
}
