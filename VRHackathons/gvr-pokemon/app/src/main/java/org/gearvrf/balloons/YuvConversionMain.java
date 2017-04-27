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
import org.gearvrf.GVRNioBufferTexture;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRCameraSceneObject;
import org.gearvrf.utility.YuvNv21ToRgbShader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class YuvConversionMain extends GVRMain {

    public void onPreviewFrame(byte[] image) {
        final YuvConversionActivity ba = (YuvConversionActivity)getGVRContext().getActivity();
        yBuffer.put(image, 0, ba.width*ba.height);
        yBuffer.position(0);
        uvBuffer.put(ba.previewCallbackBuffer, ba.width*ba.height, ba.width*ba.height/2);
        uvBuffer.position(0);

        //for simplicity's sake not trying to guarantee both are updated on time for the next frame; one
        //way would be to run both for a gl runnable (GVRContext.runOnGlThread)
        mYBufferTexture.postBuffer(GLES30.GL_LUMINANCE, ba.width, ba.height, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, yBuffer);
        mUVBufferTexture.postBuffer(GLES30.GL_LUMINANCE_ALPHA, ba.width / 2, ba.height / 2, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, uvBuffer);
    }

    private GVRScene mScene;
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

        mYBufferTexture = new GVRNioBufferTexture(context);
        mUVBufferTexture = new GVRNioBufferTexture(context);
        material.setTexture("y_texture", mYBufferTexture);
        material.setTexture("uv_texture", mUVBufferTexture);

        quad.getRenderData().setShaderTemplate(YuvNv21ToRgbShader.class);
        quad.getTransform().setPosition(0,0,-2);
        mScene.getMainCameraRig().addChildObject(quad);
        mScene.bindShaders();
    }

    private GVRNioBufferTexture mYBufferTexture;
    private GVRNioBufferTexture mUVBufferTexture;
    private ByteBuffer yBuffer;
    private ByteBuffer uvBuffer;

}
