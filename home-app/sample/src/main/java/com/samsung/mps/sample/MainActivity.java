package com.samsung.mps.sample;

import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;


public class MainActivity extends GVRActivity  {
    private static final String TAG  = "MainActivity";
    private SampleMain main;
    private VRTouchPadGestureDetector mDetector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = new SampleMain();
        mDetector = new VRTouchPadGestureDetector(main);
        setMain(main);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return false;
    }

}
