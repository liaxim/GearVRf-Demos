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

package org.gearvrf.gui;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRGUISceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRFrameLayout;


public class GUIMain extends GVRMain {
    private static final String TAG = GUIMain.class.getSimpleName();
    private static final int KEY_EVENT = 1;
    private static final int MOTION_EVENT = 2;

    private GVRViewSceneObject layoutSceneObject;
    private GVRContext context;

    private static final float DEPTH = -1.5f;

    private final GVRFrameLayout frameLayout;

    private GVRSphereSceneObject sphere;
    private GVRCubeSceneObject cube;
    private GVRCylinderSceneObject cylinder;
    private GVRMaterial mat;
    private GVRSceneObject currentObject;
    private GVRSceneObject sphere2;


    private GVRScene mainScene;
    private GVRSceneObject cursor;

    public GUIMain(GUIActivity activity,
                       final GVRFrameLayout frameLayout) {
        this.frameLayout = frameLayout;
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        context = gvrContext;

        sphere = new GVRSphereSceneObject(gvrContext, true);
        cube = new GVRCubeSceneObject(gvrContext, true);
        cylinder = new GVRCylinderSceneObject(gvrContext, true);
        mat = new GVRMaterial(gvrContext);
        mat.setDiffuseColor(1, 0, 0, 1);
        mat.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        sphere.getRenderData().setShaderTemplate(GVRPhongShader.class);
        sphere.getRenderData().setMaterial(mat);
        sphere.getRenderData().enableLight();
        cube.getRenderData().setShaderTemplate(GVRPhongShader.class);
        cube.getRenderData().setMaterial(mat);
        cube.getRenderData().enableLight();
        cylinder.getRenderData().setShaderTemplate(GVRPhongShader.class);
        cylinder.getRenderData().setMaterial(mat);
        cylinder.getRenderData().enableLight();

        currentObject = sphere;
        GVRLight mLight = new GVRLight(gvrContext);

        mLight.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1.0f);
        mLight.setDiffuseIntensity(0.8f, 0.8f, 0.8f, 1.0f);
        mLight.setSpecularIntensity(0.1f, 0.1f, 0.1f, 1.0f);

        GVRSceneObject lightObject = new GVRSceneObject(gvrContext);
        lightObject.getTransform().setPosition(25.0f, 50.0f, 25.0f);
        lightObject.attachLight(mLight);

        layoutSceneObject = new GVRGUISceneObject(gvrContext, frameLayout, 3f, 60f);
        mainScene = gvrContext.getMainScene();
        mainScene.addSceneObject(layoutSceneObject);
        mainScene.addSceneObject(currentObject);
        mainScene.addSceneObject(lightObject);

        layoutSceneObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        layoutSceneObject.getTransform().setPosition(-1.5f*DEPTH, 0.0f, 1.5f*DEPTH);
        currentObject.getTransform().setPosition(3*DEPTH, 0.0f, 5*DEPTH);
        layoutSceneObject.getTransform().setRotationByAxis(-45.0f, 0.0f, 1.0f, 0.0f);
        layoutSceneObject.setName("GUIObject");

        // set up the input manager for the main scene
        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.addCursorControllerListener(listener);
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            listener.onCursorControllerAdded(cursor);
        }
    }

    public void setCurrentObject(String objname) {
        switch (objname){
            case "Sphere": {
                if (this.currentObject == this.sphere) break;
                mainScene.removeSceneObject(this.currentObject);
                float x = this.currentObject.getTransform().getPositionX();
                float y = this.currentObject.getTransform().getPositionY();
                float z = this.currentObject.getTransform().getPositionZ();
                sphere.getTransform().setPosition(x,y,z);
                x = this.currentObject.getTransform().getScaleX();
                y = this.currentObject.getTransform().getScaleY();
                z = this.currentObject.getTransform().getScaleZ();
                sphere.getTransform().setScale(x,y,z);
                this.currentObject = sphere;
                mainScene.addSceneObject(this.currentObject);
                break;
            }
            case "Cube": {
                if (this.currentObject == this.cube) break;
                mainScene.removeSceneObject(this.currentObject);
                float x = this.currentObject.getTransform().getPositionX();
                float y = this.currentObject.getTransform().getPositionY();
                float z = this.currentObject.getTransform().getPositionZ();
                cube.getTransform().setPosition(x,y,z);
                x = this.currentObject.getTransform().getScaleX();
                y = this.currentObject.getTransform().getScaleY();
                z = this.currentObject.getTransform().getScaleZ();
                cube.getTransform().setScale(x,y,z);
                this.currentObject = cube;
                mainScene.addSceneObject(this.currentObject);
                break;
            }
            case "Cylinder": {
                if (this.currentObject == this.cylinder) break;
                mainScene.removeSceneObject(this.currentObject);
                float x = this.currentObject.getTransform().getPositionX();
                float y = this.currentObject.getTransform().getPositionY();
                float z = this.currentObject.getTransform().getPositionZ();
                cylinder.getTransform().setPosition(x,y,z);
                x = this.currentObject.getTransform().getScaleX();
                y = this.currentObject.getTransform().getScaleY();
                z = this.currentObject.getTransform().getScaleZ();
                cylinder.getTransform().setScale(x,y,z);
                this.currentObject = cylinder;
                mainScene.addSceneObject(this.currentObject);
                break;
            }
        }
    }

    public  void changeSize(int size){
        this.cube.getTransform().setScale(size, size, size);
        this.sphere.getTransform().setScale(size, size, size);
        this.cylinder.getTransform().setScale(size, size, size);
    }

    public  void changeColor(String color){
        switch (color) {
            case "Red": {
                mat.setDiffuseColor(1,0,0, 1);
                break;
            }
            case "Blue": {
                mat.setDiffuseColor(0,0,1, 1);
                break;
            }
            case "Green": {
                mat.setDiffuseColor(0,1,0, 1);
            }
        }
    }

    private CursorControllerListener listener = new CursorControllerListener() {

        @Override
        public void onCursorControllerRemoved(GVRCursorController controller) {
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                if (cursor != null) {
                    mainScene.getMainCameraRig().removeChildObject(cursor);
                }
                controller.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerAdded(GVRCursorController controller) {
            // Only allow only gaze
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                cursor = new GVRSceneObject(context,
                        new FutureWrapper<GVRMesh>(context.createQuad(0.1f, 0.1f)),
                        context.getAssetLoader().loadFutureTexture(new GVRAndroidResource(context, R.raw.cursor)));
                cursor.getTransform().setPosition(0.0f, 0.0f, DEPTH);
                mainScene.getMainCameraRig().addChildObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);
                controller.setPosition(0.0f, 0.0f, DEPTH);
                controller.setNearDepth(DEPTH);
                controller.setFarDepth(DEPTH);
            }
            else {
                    // disable all other types
                controller.setEnable(false);
            }
        }
    };




    @Override
    public void onStep() {
        // unused
    }
}
