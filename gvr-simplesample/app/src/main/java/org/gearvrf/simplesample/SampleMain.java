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
import android.opengl.GLES30;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import java.io.IOException;

public class SampleMain extends GVRMain {
    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        GVRScene scene = gvrContext.getNextMainScene();

        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.WHITE);

        GVRSceneObject shadowBox1 = makeShadowBox();
        shadowBox1.getTransform().setPosition(3, 0, -6);
        scene.addSceneObject(shadowBox1);

        GVRSceneObject shadowBox2 = makeShadowBox();
        shadowBox2.getTransform().setPosition(-3, 0, -6);
        scene.addSceneObject(shadowBox2);

        GVRSceneObject shadowBox3 = makeShadowBox();
        shadowBox3.getTransform().setPosition(0, 3, -6);
        scene.addSceneObject(shadowBox3);

        GVRSceneObject shadowBox4 = makeShadowBox();
        shadowBox4.getTransform().setPosition(0, -3, -6);
        scene.addSceneObject(shadowBox4);
    }

    GVRSceneObject makeShadowBox() throws IOException {
        final GVRContext gvrContext = getGVRContext();
        GVRSceneObject parent = new GVRSceneObject(gvrContext, 1.2f, 0.7f);

//        final GVRSceneObject stencilMask = gvrContext.getAssetLoader().loadModel("stencil_mask.fbx");
//        //stencilMask.attachComponent(new GVRStencilMask());
//        final GVRSceneObject.BoundingVolume boundingVolume = stencilMask.getBoundingVolume();
//        //use boundingvolume to determine size of the logo
//        stencilMask.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
//        stencilMask.getRenderData().setAlphaBlend(true);
//        parent.addChildObject(stencilMask);
//

        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.white_texture));
//        final GVRModelSceneObject stencil = gvrContext.getAssetLoader().loadModel("Stencil_Mask_Shadowbox.fbx");

//        GVRSceneObject stencil = new GVRSceneObject(gvrContext, 0.9f, 0.35f, texture);

        GVRSceneObject stencil = new GVRSphereSceneObject(gvrContext, true);
        stencil.getRenderData().getMaterial().setMainTexture(texture);

        stencil.getRenderData()
//                .setRenderingOrder(GVRRenderData.GVRRenderingOrder.STENCIL)
//                .setStencilTest(true)
                .setStencilFunc(GLES30.GL_ALWAYS, 1, 0xFF)
                .setStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE)
                .setStencilMask(0xFF);

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
        background.getTransform().setPositionZ(-2).setScale(4,4,4);

        background.getRenderData()
//                .setStencilTest(true)
                .setStencilFunc(GLES30.GL_EQUAL, 1, 0xFF)
                .setStencilMask(0x00);
        parent.addChildObject(background);

        return parent;
    }
}
