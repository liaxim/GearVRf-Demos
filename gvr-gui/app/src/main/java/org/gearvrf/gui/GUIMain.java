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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.ISensorEvents;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRGUISceneObject;
import org.gearvrf.scene_objects.GVRGearControllerSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRFrameLayout;


public class GUIMain extends GVRMain {
    private static final String TAG = GUIMain.class.getSimpleName();

    private GVRViewSceneObject layoutSceneObject;
    private GVRContext context;

    private static final float DEPTH = -1.5f;

    private final GVRFrameLayout frameLayout;

    private GVRSphereSceneObject sphere;
    private GVRCubeSceneObject cube;
    private GVRCylinderSceneObject cylinder;
    private GVRMaterial mat;
    private GVRSceneObject currentObject;
    private GVRSceneObject plane;


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
        plane = new GVRSceneObject(gvrContext, gvrContext.createQuad(1.0f, 1.0f));

        currentObject = sphere;
        GVRLight mLight = new GVRLight(gvrContext);

        mLight.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1.0f);
        mLight.setDiffuseIntensity(0.8f, 0.8f, 0.8f, 1.0f);
        mLight.setSpecularIntensity(0.1f, 0.1f, 0.1f, 1.0f);

        GVRSceneObject lightObject = new GVRSceneObject(gvrContext);
        lightObject.getTransform().setPosition(25.0f, 50.0f, 25.0f);
        lightObject.attachLight(mLight);

        layoutSceneObject = new GVRGUISceneObject(gvrContext, frameLayout, 3f, 45f);
        mainScene = gvrContext.getMainScene();
        mainScene.addSceneObject(layoutSceneObject);
        mainScene.addSceneObject(currentObject);
        mainScene.addSceneObject(lightObject);

        layoutSceneObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        layoutSceneObject.getTransform().setPosition(-1.5f*DEPTH, 0.0f, 1.5f*DEPTH);
        currentObject.getTransform().setPosition(0*DEPTH, 0.0f, 5*DEPTH);
        plane.getTransform().setPosition(3*DEPTH, 0.0f, 3.25f*DEPTH);
        plane.getTransform().setScale(5,5,5);
        plane.getRenderData().setMaterial(mat);
        layoutSceneObject.getTransform().setRotationByAxis(-45.0f, 0.0f, 1.0f, 0.0f);
        layoutSceneObject.setName("GUIObject");

        // set up the input manager for the main scene
        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.addCursorControllerListener(cursorControllerListener);
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            cursorControllerListener.onCursorControllerAdded(cursor);
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

    private CursorControllerListener cursorControllerListener = new CursorControllerListener() {
        @Override
        public void onCursorControllerAdded(GVRCursorController gvrCursorController) {
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
                android.util.Log.d(TAG, "Got the orientation remote controller");

                GVRGearControllerSceneObject controller = new GVRGearControllerSceneObject(context);
                GVRTexture cursorTexture = context.getAssetLoader().loadTexture(new GVRAndroidResource(context, R.raw.cursor));
                GVRSceneObject cursor = new GVRSceneObject(context,
                        context.createQuad(0.25f, 0.25f),
                        cursorTexture);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(10000);
                cursor.setName("Cursor");
                controller.setRayDepth(2.5f);
                controller.setCursor(cursor);
                controller.setName("GEARController");
                //controller.disableRay();
                controller.setCursorController(gvrCursorController);
                controller.enableSurfaceProjection();
                //controller.enableFastCursorProjection();
                ISensorEvents projectionListener = controller.getProjectionListener();

                layoutSceneObject.getEventReceiver().addListener(projectionListener);

                cube.attachCollider(new GVRMeshCollider(context, cube.getRenderData().getMesh(), true));
                cube.setSensor(new GVRBaseSensor(context));
                cube.getEventReceiver().addListener(projectionListener);
                cube.setName("Cube");

                sphere.attachCollider(new GVRMeshCollider(context, sphere.getRenderData().getMesh(), true));
                sphere.setSensor(new GVRBaseSensor(context));
                sphere.getEventReceiver().addListener(projectionListener);
                sphere.setName("Sphere");

                cylinder.attachCollider(new GVRSphereCollider(getGVRContext()));
                cylinder.setSensor(new GVRBaseSensor(context));
                cylinder.getEventReceiver().addListener(projectionListener);

                gvrCursorController.setNearDepth(DEPTH);
                gvrCursorController.setFarDepth(DEPTH);
            }
            else if (gvrCursorController.getControllerType() == GVRControllerType.GAZE) {
                cursor = new GVRSceneObject(context,
                        context.createQuad(0.1f, 0.1f),
                        context.getAssetLoader().loadTexture(new GVRAndroidResource(context, R.raw.cursor)));
                cursor.getTransform().setPosition(0.0f, 0.0f, DEPTH);
                mainScene.getMainCameraRig().addChildObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);
                gvrCursorController.setPosition(0.0f, 0.0f, DEPTH);
                gvrCursorController.setNearDepth(DEPTH);
                gvrCursorController.setFarDepth(DEPTH);
            }
            else {
                //do nothing
            }
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController gvrCursorController) {
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
                android.util.Log.d(TAG, "Got the orientation remote controller");
            }
        }
    };

    @Override
    public void onStep() {
        // unused
    }
}
