package org.gearvrf.gvrmeshanimation;

import org.gearvrf.GVRActivity;

import android.os.Bundle;

public class MeshAnimationActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMain(new MeshAnimationMain(this), "gvr.xml");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
