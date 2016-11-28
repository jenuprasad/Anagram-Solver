package com.bmpak.anagramsolver.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by charbgr on 10/1/14.
 */
public class Bebas extends TextView {
    public Bebas(Context context) {
        super(context);
        Typeface tf = Typeface.createFromAsset(getResources().getAssets(), "font/bebas.ttf");
        setTypeface(tf);
    }

    public Bebas(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface tf = Typeface.createFromAsset(getResources().getAssets(), "font/bebas.ttf");
        setTypeface(tf);
    }

    public Bebas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface tf = Typeface.createFromAsset(getResources().getAssets(), "font/bebas.ttf");
        setTypeface(tf);
    }
}
