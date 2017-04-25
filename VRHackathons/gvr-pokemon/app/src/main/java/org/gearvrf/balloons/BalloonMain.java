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

package org.gearvrf.balloons;

import android.media.MediaPlayer;
import android.opengl.GLES30;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSharedTexture;
import org.gearvrf.scene_objects.GVRCameraSceneObject;
import org.gearvrf.utility.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BalloonMain extends GVRMain {

    final int[] textureNames = new int[2];
    public void onPreviewFrame(byte[] image) {
        final BalloonActivity ba = (BalloonActivity)getGVRContext().getActivity();
        Log.i("mmarinov", "onPreviewFrame " + ba.previewCallbackBuffer.length);
        yBuffer.put(image, 0, ba.width*ba.height);
        yBuffer.position(0);

        //Copy the UV channels of the image into their buffer, the following (width*height/2) bytes are the UV channel; the U and V bytes are interspread
        uvBuffer.put(ba.previewCallbackBuffer, ba.width*ba.height, ba.width*ba.height/2);
        uvBuffer.position(0);

        getGVRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                Log.i("mmarinov", "sending data");
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureNames[0]);
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, ba.width, ba.height, 0,
                        GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, yBuffer);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
                Log.i("mmarinov", "sending data1 " + Integer.toHexString(GLES30.glGetError()));

                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureNames[1]);
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE_ALPHA, ba.width/2, ba.height/2,
                        0, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, uvBuffer);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
                Log.i("mmarinov", "sending data2 " + Integer.toHexString(GLES30.glGetError()));

                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            }
        });
    }

    private GVRScene mScene = null;
    public static MediaPlayer sMediaPlayer;


    private BalloonActivity mActivity;
    private GVRCameraSceneObject cameraObject;

    BalloonMain(BalloonActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(final GVRContext context)
    {
       /*
         * Set the background color
         */
        mScene = context.getMainScene();

        /*
         * Set the camera passthrough
         */
        cameraObject = new GVRCameraSceneObject(
                context, 18f, 10f, mActivity.getCamera());
        cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
//        cameraObject.getTransform().setPosition(0.0f, -1.8f, -10.0f);
//        mScene.getMainCameraRig().addChildObject(cameraObject);

        final BalloonActivity ba = (BalloonActivity)getGVRContext().getActivity();
        yBuffer = ByteBuffer.allocateDirect(ba.width*ba.height);
        uvBuffer = ByteBuffer.allocateDirect(ba.width*ba.height/2); //We have (width/2*height/2) pixels, each pixel is 2 bytes
        yBuffer.order(ByteOrder.nativeOrder());
        uvBuffer.order(ByteOrder.nativeOrder());

        final GVRSceneObject quad = new GVRSceneObject(context, 4, 2);
        GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.BeingGenerated.ID);
        quad.getRenderData().setMaterial(material);
        quad.getRenderData().setShaderTemplate(YUV2RGBShader.class);
        quad.getTransform().setPosition(0,0,-2);
        mScene.addSceneObject(quad);
        mScene.bindShaders();

        context.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                Log.i("mmarinov", "run1");
                GLES30.glGenTextures(2, textureNames, 0);

                GVRSharedTexture sharedTextureY = new GVRSharedTexture(context, textureNames[0]);
                GVRSharedTexture sharedTextureUV = new GVRSharedTexture(context, textureNames[1]);
                quad.getRenderData().getMaterial().setTexture("y_texture", sharedTextureY);
                quad.getRenderData().getMaterial().setTexture("uv_texture", sharedTextureUV);
                Log.i("mmarinov", "run11 " + Integer.toHexString(GLES30.glGetError()) + ", " + textureNames[0] + ", " + textureNames[1]);
            }
        });
    }
    private ByteBuffer yBuffer;
    private ByteBuffer uvBuffer;

}
