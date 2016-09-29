/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.zoom360Photo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRShaders;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;
import org.gearvrf.zoom360Photo.FileBrowserUtils.FileBrowserListener;

import android.view.KeyEvent;
import android.view.MotionEvent;

public class Zoom360PhotosMain extends GVRMain implements FileBrowserListener {
    private static final String TAG = Zoom360PhotosMain.class.getSimpleName();
    private GVRCameraRig cameraRig;
    private int step = 0;
    private FileBrowserUtils fileBrowser;
    private CursorUtils cursor;
    private boolean isBrowserShowing = false;
    private GVRContext gvrContext;
    private boolean turnOnBrowser;
    private GVRSphereSceneObject sphereObject;
    private GVRCubeSceneObject cubeObject;

    @Override
    public void onInit(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        // get a handle to the scene
        GVRScene scene = gvrContext.getNextMainScene();
        cameraRig = scene.getMainCameraRig();

        cubeObject = new GVRCubeSceneObject(gvrContext, false);
        cubeObject.getTransform().setScale(20, 20, 20);
        cubeObject.setEnable(false);
        cubeObject.getRenderData().getMaterial().setShaderType(GVRMaterial.GVRShaderType.Cubemap.ID);
        scene.addSceneObject(cubeObject);

        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new GVRSphereSceneObject(gvrContext, 72, 144, false);
        sphereObject.setName("sphere");
        sphereObject.getTransform().setScale(20.0f, 20.0f, 20.0f);
        sphereObject.setEnable(false);

        // add the scene object to the scene graph
        scene.addSceneObject(sphereObject);

        fileBrowser = new FileBrowserUtils(gvrContext, this);
        cursor = new CursorUtils(gvrContext);
        turnOnBrowser = true;
    }

    @Override
    public void onStep() {
        if(!isBrowserShowing && turnOnBrowser) {
            fileBrowser.show();
            cursor.show();
            isBrowserShowing = true;
            turnOnBrowser = false;
        } 
    }

    public void onTap() {
        if(!isBrowserShowing && !turnOnBrowser) {
            turnOnBrowser = true;
        } 
    }

    private void updateFovY() {
        if(step > 70) {
            step = 70;
        } else if(step < 0) {
            step = 0;
        } 

        GVRPerspectiveCamera left = (GVRPerspectiveCamera) cameraRig.getLeftCamera();
        GVRPerspectiveCamera right = (GVRPerspectiveCamera) cameraRig.getRightCamera();

        left.setFovY(90 - step);
        right.setFovY(90 - step);
    }

    private float lastX = 0, lastY = 0;
    private boolean isOnClick = false;
    private static final float MOVE_SCALE_FACTOR = 0.01f;
    private static final float MOVE_THRESHOLD = 80f;

    public void onTouchEvent(MotionEvent event) {
        if(isBrowserShowing) {
            return;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            lastX = event.getX();
            lastY = event.getY();
            isOnClick = true;
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if (isOnClick) {
                onTap();
            }
         break;
        case MotionEvent.ACTION_MOVE:
//            float currentX = event.getX();
//            float currentY = event.getY();
//            float dx = currentX - lastX;
//            float dy = currentY - lastY;
//            float distance = dx * dx + dy * dy;
//            if (Math.abs(distance) > MOVE_THRESHOLD) {
//                lastX = currentX;
//                lastY = currentY;
//                distance *= MOVE_SCALE_FACTOR;
//                if (dx > 0) {
//                    distance = -distance;
//                }
//
//                step += distance;
//                updateFovY();
//
//                isOnClick = false;
//            }
            break;
        default:
            break;
        }
    }

    @Override
    public void onFileSelected(String filePath) {
        try {
            if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
                Future<GVRTexture> texture = gvrContext.loadFutureTexture(new GVRAndroidResource(filePath));
                sphereObject.getRenderData().getMaterial().setMainTexture(texture);
                cubeObject.setEnable(false);
                sphereObject.setEnable(true);
            } else if (filePath.toLowerCase().endsWith(".zip")) {
                final Future<GVRTexture> texture = gvrContext.loadFutureCubemapTexture(new GVRAndroidResource(filePath));
                cubeObject.getRenderData().getMaterial().setMainTexture(texture);
                sphereObject.setEnable(false);
                cubeObject.setEnable(true);
            }

            fileBrowser.hide();
            cursor.hide();
            isBrowserShowing = false;
            turnOnBrowser = false;
        } catch(IOException e) {
            Log.e(TAG, "Could not open file: %s, Error:%s", filePath, e.getMessage());
        }
    }

    boolean processKeyEvent(int keyCode) {
        switch (keyCode) {
            case android.view.KeyEvent.KEYCODE_BUTTON_L1:
            case android.view.KeyEvent.KEYCODE_VOLUME_UP: {
                applyGrid0();
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                applyGrid1();
                return true;
            }
        }
        return false;
    }

    void applyGrid0() {
        final GVRContext gvr = getGVRContext();
        try {
            Method method = gvr.getClass().getMethod("changeRenderDiameter", float.class);
            method.invoke(gvr, 0.062f);

            method = gvr.getClass().getMethod("changeFreeParam1", float.class);
            method.invoke(gvr, 0f);

            method = gvr.getClass().getMethod("setFovY", float.class);
            method.invoke(gvr, 86f);

            gvr.showToast("D62 G0 FOV86");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void applyGrid1() {
        final GVRContext gvr = getGVRContext();
        try {
            Method method = gvr.getClass().getMethod("changeRenderDiameter", float.class);
            method.invoke(gvr, 0.0626f);

            method = gvr.getClass().getMethod("changeFreeParam1", float.class);
            method.invoke(gvr, 1f);

            method = gvr.getClass().getMethod("setFovY", float.class);
            method.invoke(gvr, 87f);

            gvr.showToast("D626 G1 FOV87");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

