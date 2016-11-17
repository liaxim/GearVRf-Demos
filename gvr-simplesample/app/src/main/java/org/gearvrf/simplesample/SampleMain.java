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

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLES30;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;

import java.io.IOException;

public class SampleMain extends GVRScript {

    private GVRContext mGVRContext;

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;

        GVRScene scene = gvrContext.getNextMainScene();

        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.WHITE);

        // load texture
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gearvr_logo));

        // create a scene object (this constructor creates a rectangular scene
        // object that uses the standard 'unlit' shader)
        GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, 4.0f, 2.0f,
                texture);

        // set the scene object position
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        // add the scene object to the scene graph
//        scene.addSceneObject(sceneObject);


        GVRSceneObject parent = new GVRSceneObject(gvrContext, 1.2f, 0.7f);
        parent.getTransform().setPositionZ(-1);

//        final GVRSceneObject stencilMask = gvrContext.getAssetLoader().loadModel("stencil_mask.fbx");
//        //stencilMask.attachComponent(new GVRStencilMask());
//        final GVRSceneObject.BoundingVolume boundingVolume = stencilMask.getBoundingVolume();
//        //use boundingvolume to determine size of the logo
//        stencilMask.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
//        stencilMask.getRenderData().setAlphaBlend(true);
//        parent.addChildObject(stencilMask);
//

        texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.white_texture));
//        final GVRModelSceneObject stencil = gvrContext.getAssetLoader().loadModel("Stencil_Mask_Shadowbox.fbx");

        GVRSceneObject stencil = new GVRSceneObject(gvrContext, 0.9f, 0.35f, texture);
//        stencil.getRenderData().getMaterial().setMainTexture(texture);

        stencil.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.STENCIL);
        stencil.getRenderData().setStencilTest(true);
        stencil.getRenderData().setStencilFunc(GLES30.GL_ALWAYS, 1, 0xFF);
        stencil.getRenderData().setStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE);
        stencil.getRenderData().setStencilMask(0xFF);
        stencil.getRenderData().setDepthMask(false);

        parent.addChildObject(stencil);

        //scales in X and Y independently
        //??get from app the logo texture
        //??get the stencil mask mesh from duncan's model
//        GVRTexture logoTexture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.logo));
//        GVRSceneObject logo = new GVRSceneObject(gvrContext, 1.2f, 0.7f, logoTexture);
//        logo.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
//        logo.getRenderData().setAlphaBlend(true);
//        parent.addChildObject(logo);

        GVRTexture backgroundTexture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, "GearVR.jpg"));
        GVRSceneObject background = new GVRSceneObject(gvrContext, 1.2f, 0.7f, backgroundTexture);
        background.getTransform().setPositionZ(-2);
        background.getTransform().setScale(4,4,4);

        background.getRenderData().setStencilTest(true);
        background.getRenderData().setStencilFunc(GLES30.GL_EQUAL, 1, 0xFF);
        background.getRenderData().setStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE);
        background.getRenderData().setStencilMask(0x00);
        parent.addChildObject(background);

//        GVRTexture graphics1Texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, "donald.png"));
//        GVRSceneObject graphics1 = new GVRSceneObject(gvrContext, 1.2f, 0.7f, graphics1Texture);
//        graphics1.getTransform().setPositionZ(-1.8f);
//        graphics1.getTransform().setScale(2,2,2);
//        graphics1.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
//        graphics1.getRenderData().setAlphaBlend(true);
//        parent.addChildObject(graphics1);
//
//        GVRTexture graphics2Texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, "circles.png"));
//        GVRSceneObject graphics2 = new GVRSceneObject(gvrContext, 1.2f, 0.7f, graphics2Texture);
//        graphics2.getTransform().setPositionZ(-1.9f);
//        graphics2.getTransform().setScale(2,2,2);
//        graphics2.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
//        graphics2.getRenderData().setAlphaBlend(true);
//        parent.addChildObject(graphics2);

        scene.addSceneObject(parent);
    }

    @Override
    public void onStep() {
    }

}
