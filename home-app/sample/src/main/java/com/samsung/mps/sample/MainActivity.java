package com.samsung.mps.sample;

import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;

public class MainActivity extends GVRActivity {
    private SampleMain main;
    private long lastDownTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = new SampleMain();
        setMain(main, "gvr.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        main.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
