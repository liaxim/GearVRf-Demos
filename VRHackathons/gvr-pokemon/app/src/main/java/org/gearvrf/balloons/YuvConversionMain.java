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

import android.opengl.GLES30;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSharedTexture;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCameraSceneObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Adapted from this excellent post: http://stackoverflow.com/a/22456885
 */
public class YuvConversionMain extends GVRMain {

    final int[] textureNames = new int[2];
    public void onPreviewFrame(byte[] image) {
        final YuvConversionActivity ba = (YuvConversionActivity)getGVRContext().getActivity();
        yBuffer.put(image, 0, ba.width*ba.height);
        yBuffer.position(0);
        uvBuffer.put(ba.previewCallbackBuffer, ba.width*ba.height, ba.width*ba.height/2);
        uvBuffer.position(0);

        getGVRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureNames[0]);
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, ba.width, ba.height, 0,
                        GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, yBuffer);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureNames[1]);
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE_ALPHA, ba.width/2, ba.height/2,
                        0, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, uvBuffer);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            }
        });
    }

    private GVRScene mScene = null;
    private YuvConversionActivity mActivity;
    private GVRCameraSceneObject cameraObject;

    YuvConversionMain(YuvConversionActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(final GVRContext context)
    {
        mScene = context.getMainScene();

        cameraObject = new GVRCameraSceneObject(context, 18f, 10f, mActivity.getCamera());
        cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.

        final YuvConversionActivity ba = (YuvConversionActivity)getGVRContext().getActivity();
        yBuffer = ByteBuffer.allocateDirect(ba.width*ba.height);
        uvBuffer = ByteBuffer.allocateDirect(ba.width*ba.height/2);
        yBuffer.order(ByteOrder.nativeOrder());
        uvBuffer.order(ByteOrder.nativeOrder());

        final GVRSceneObject quad = new GVRSceneObject(context, 3f, 1.5f);
        final GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.BeingGenerated.ID);
        quad.getRenderData().setMaterial(material);
        //add the textures ahead of time to have them picked up by the shader generation
        material.setTexture("y_texture", (GVRTexture)null);
        material.setTexture("uv_texture", (GVRTexture)null);
        quad.getRenderData().setShaderTemplate(YUV2RGBShader.class);
        quad.getTransform().setPosition(0,0,-2);
        mScene.getMainCameraRig().addChildObject(quad);
        mScene.bindShaders();

        context.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                GLES30.glGenTextures(2, textureNames, 0);

                GVRSharedTexture sharedTextureY = new GVRSharedTexture(context, textureNames[0]);
                GVRSharedTexture sharedTextureUV = new GVRSharedTexture(context, textureNames[1]);
                material.setTexture("y_texture", sharedTextureY);
                material.setTexture("uv_texture", sharedTextureUV);
            }
        });
    }
    private ByteBuffer yBuffer;
    private ByteBuffer uvBuffer;

}
