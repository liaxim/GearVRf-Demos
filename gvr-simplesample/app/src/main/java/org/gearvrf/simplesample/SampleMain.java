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

package org.gearvrf.simplesample;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.view.Surface;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRSharedTexture;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Log;

import static android.opengl.GLES20.glGenTextures;

public class SampleMain extends GVRMain {

    private GVRContext mGVRContext;

    @Override
    public void onInit(final GVRContext gvrContext) {

        // save context for possible use in onStep(), even though that'surface empty
        // in this sample
        mGVRContext = gvrContext;

        GVRScene scene = gvrContext.getMainScene();

        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera()
                .setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera()
                .setBackgroundColor(Color.WHITE);

        // load texture
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gearvr_logo));

        // create a scene object (this constructor creates a rectangular scene
        // object that uses the standard 'unlit' shader)
        final GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, 4.0f, 2.0f,
                texture);

        // set the scene object position
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        // add the scene object to the scene graph
        scene.addSceneObject(sceneObject);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                textureId = new int[1];
                GLES30.glGenTextures(1, textureId, 0);
                int err;
                if (GLES31.GL_NO_ERROR != (err = GLES31.glGetError())) {
                    Log.i("mmarinov", "glGenTextures error " + err);
                }

                surfaceTexture = new SurfaceTexture(textureId[0]);
                surfaceTexture.setDefaultBufferSize(1024, 1024);
                surface = new Surface(surfaceTexture);
                surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        Log.i("mrm_server", "onFrameAvailable ");
                    }
                });

//            final SurfaceHolder holder = view.getHolder();
                //final Surface surface = holder.getSurface();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setComponent(new ComponentName("com.example.test.mrm_client", "com.example.test.mrm_client.MyIntentService"));
                intent.putExtra("surface", surface);
                gvrContext.getContext().startService(intent);

                GVRSharedTexture texture = new GVRSharedTexture(gvrContext, textureId[0]);
                sceneObject.getRenderData().getMaterial().setMainTexture(texture);
                sceneObject.getRenderData().getMaterial().setTexture("diffuseTexture", texture);
                sceneObject.getRenderData().getMaterial().setShaderType(GVRMaterial.GVRShaderType.OES.ID);
                if (GLES31.GL_NO_ERROR != (err = GLES31.glGetError())) {
                    Log.i("mmarinov", "run error " + err);
                }

                GVRDrawFrameListener listener = new GVRDrawFrameListener() {
                    @Override
                    public void onDrawFrame(float v) {
                        int err;
                        if (GLES31.GL_NO_ERROR != (err = GLES31.glGetError())) {
                            Log.i("mmarinov", "run error1 " + err);
                        }
                        surfaceTexture.updateTexImage();
                        if (GLES31.GL_NO_ERROR != (err = GLES31.glGetError())) {
                            Log.i("mmarinov", "run error2 " + err);
                        }
                    }
                };
                gvrContext.registerDrawFrameListener(listener);
            }
        };
        gvrContext.runOnGlThread(r);
    }
    int[] textureId;
    SurfaceTexture surfaceTexture;
    Surface surface;

    @Override
    public void onStep() {
    }

}
