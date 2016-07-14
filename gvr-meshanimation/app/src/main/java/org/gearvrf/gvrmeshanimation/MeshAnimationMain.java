package org.gearvrf.gvrmeshanimation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTransform;
import org.gearvrf.IActivityEvents;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.periodic.GVRPeriodicEngine;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;

public class MeshAnimationMain extends GVRMain {

    private static final String TAG = "MeshAnimationMain";
    private static final String mModelPath = "TRex_NoGround.fbx";

    private GVRContext mGVRContext;
    private GVRScene mMainScene;
    private GVRActivity mActivity;
    private GVRAnimationEngine mAnimationEngine;

    private float mAccelerometerX, mAccelerometerY, mAccelerometerZ;
    private Sensor mAccelerometer;

    private final Random mRandom = new Random();
    private int mMaxCharacters = 5;

    private final Vector3f mCharacterPosition = new Vector3f();
    private final Vector3f mTargetPosition = new Vector3f();
    private final Vector3f mTmp = new Vector3f();
    private final AxisAngle4f mRotation = new AxisAngle4f();

    private static final int SKIP_STEPS = 250;
    private int mSkipSteps;

    public MeshAnimationMain(GVRActivity activity) {
        mActivity = activity;
        mActivity.getEventReceiver().addListener(new IActivityEvents() {
            @Override
            public void onPause() {
                stopListening();
            }

            @Override
            public void onResume() {
                startListening();
            }

            @Override
            public void onDestroy() {
            }

            @Override
            public void onSetScript(GVRScript script) {
            }

            @Override
            public void onWindowFocusChanged(boolean hasFocus) {
            }

            @Override
            public void onConfigurationChanged(Configuration config) {
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
            }

            @Override
            public void onTouchEvent(MotionEvent event) {
            }

            @Override
            public void dispatchTouchEvent(MotionEvent event) {
            }
        });
    }

    private final SensorEventListener mAccelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mAccelerometerX = event.values[0];
                mAccelerometerY = event.values[1];
                mAccelerometerZ = event.values[2];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void startListening() {
        if (null == mGVRContext || null != mAccelerometer) {
            return;
        }

        final SensorManager sensorManager = (SensorManager) mGVRContext.getContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(mAccelerometerListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void stopListening() {
        if (null == mGVRContext || null == mAccelerometer) {
            return;
        }

        final SensorManager sensorManager = (SensorManager) mGVRContext.getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(mAccelerometerListener, mAccelerometer);
    }

    private final static class State {
        boolean passed;
        float[] startPosition;
        float speed;

        State(float x, float y, float z, float speed) {
            startPosition = new float[]{x, y, z};
            this.speed = speed;
        }
    }

    @Override
    public void onInit(final GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mAnimationEngine = gvrContext.getAnimationEngine();
        mMainScene = gvrContext.getNextMainScene();

        startListening();
        GVRPeriodicEngine.getInstance(gvrContext).runAfter(new Runnable() {
            @Override
            public void run() {
                gvrContext.showToast("Look for the dinos; dodge by tilting your head.", 6f);
            }
        }, 2f);
    }

    @Override
    public void onStep() {
        if (mMainScene.getRoot().getChildrenCount() <= mMaxCharacters) {
            --mSkipSteps;
            if (0 == mRandom.nextInt()%71 && 0 >= mSkipSteps) {
                createCharacter();
                mSkipSteps = SKIP_STEPS;
            }
        }

        for (final GVRSceneObject character : mMainScene.getRoot().getChildren()) {
            moveObject(character);
        }

//        final GVRTransform transform = mGVRContext.getMainScene().getMainCameraRig().getTransform();
//        transform.translate(Math.min(0.15f, 0.05f * mAccelerometerY), 0, 0);

        final float[] lookAt = mGVRContext.getMainScene().getMainCameraRig().getLookAt();
        if (mAccelerometerY > 0) {
            mTmp.set(lookAt[0], lookAt[1], lookAt[2]);
            mRotation.set((float)(Math.PI/2), 0, 1, 0);
            Quaternionf q = new Quaternionf();
            mTmp.rotate(mRotation.get(q));
        }

        final GVRTransform transform = mGVRContext.getMainScene().getMainCameraRig().getTransform();
        transform.translate(Math.min(0.15f, 0.05f * mAccelerometerY), 0, 0);

    }

    private GVRSceneObject createCharacter() {
        try {
            final GVRModelSceneObject character = mGVRContext.loadModel(mModelPath);

            int startX = randInt(50, 100);
            if (0 == startX%3) {
                startX *= -1;
            }
            final State state = new State(startX, -10, -randInt(50, 100), randInt(100, 150)*0.001f);
            Log.i(TAG, "createCharacter " + Arrays.toString(state.startPosition));
            character.setTag(state);

            character.getTransform().setPosition(state.startPosition[0], state.startPosition[1], state.startPosition[2]);
            character.getTransform().setRotationByAxis(90.0f, 1.0f, 0.0f, 0.0f);
            character.getTransform().setRotationByAxis(40.0f, 0.0f, 1.0f, 0.0f);
            character.getTransform().setScale(1.5f, 1.5f, 1.5f);

            mMainScene.addSceneObject(character);

            final GVRAnimation animation = character.getAnimations().get(0);
            animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
            animation.start(mAnimationEngine);

            return character;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    private void moveObject(final GVRSceneObject object) {
        if (null == object.getTag()) {
            return;
        }

        GVRTransform t = object.getTransform();
        mCharacterPosition.set(t.getPositionX(), t.getPositionY(), t.getPositionZ());

        t = mGVRContext.getMainScene().getMainCameraRig().getTransform();
        mTargetPosition.set(t.getPositionX(), t.getPositionY(), t.getPositionZ());

        final State state = (State) object.getTag();
        if (!state.passed && mTargetPosition.distance(mCharacterPosition) > 1f) {
            mTargetPosition.sub(mCharacterPosition);
            mTargetPosition.normalize();

            object.getTransform().translate(mTargetPosition.x * state.speed, mTargetPosition.y * state.speed, mTargetPosition.z * state.speed);
        } else {
            state.passed = true;

            mTargetPosition.set(-state.startPosition[0], state.startPosition[1], -state.startPosition[2]);
            if (mTargetPosition.distance(mCharacterPosition) < 1f) {
                mMainScene.removeSceneObject(object);
                Log.i(TAG, "removing character");
            }

            mTargetPosition.sub(mCharacterPosition);
            mTargetPosition.normalize();
            object.getTransform().translate(mTargetPosition.x * state.speed, mTargetPosition.y * state.speed, mTargetPosition.z * state.speed);
        }


//        float positionZ = object.getTransform().getPositionZ();
//        positionZ += increment;
//        //object.getTransform().setPositionZ(positionZ);
//        object.getTransform().translate(0, 0, increment);
//        final float positionX1 = object.getTransform().getPositionX();
//        final float positionY1 = object.getTransform().getPositionY();
//        final float positionZ1 = object.getTransform().getPositionZ();
//
//        Log.i("mmarinov", "moveObject " + positionX1 + ", " + positionY1 + ", " + positionZ1);
//        if (positionZ1 > 10) {
//            object.getTransform().translate(0, 0, -100f);
//        }
//
//        float positionX = object.getTransform().getPositionX();
//        if (positionX < 0) {
//            positionX += increment;
//        } else {
//            positionX -= increment;
//        }
//        object.getTransform().setPositionX(positionX);
    }

    private int randInt(int min, int max) {
        return mRandom.nextInt((max - min) + 1) + min;
    }
}
