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

package org.gearvrf.sample.sceneobjects;

import android.view.View;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRGUISceneObject;

import java.io.IOException;

public class SampleMain extends GVRMain {
    private SceneObjectActivity mActivity;
    private GVRSceneObject mWebViewObject;

    SampleMain(SceneObjectActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        GVRScene scene = gvrContext.getMainScene();
        mWebViewObject = createWebViewObject(gvrContext);
        scene.addSceneObject(mWebViewObject);

        // set up the input manager for the main scene
        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.addCursorControllerListener(cursorControllerListener);
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            cursorControllerListener.onCursorControllerAdded(cursor);
        }
    }

    private GVRSceneObject createWebViewObject(GVRContext gvrContext) {
        View webView = mActivity.getWebView();
        GVRSceneObject webObject = new GVRGUISceneObject(gvrContext, webView, 3, 45);
        webObject.setName("web view object");
        webObject.getRenderData().getMaterial().setOpacity(1.0f);
        webObject.getTransform().setPosition(0.0f, 0.0f, -1.5f*1.5f);

        return webObject;
    }

    GVRSceneObject cursor;
    private CursorControllerListener cursorControllerListener = new CursorControllerListener() {
        @Override
        public void onCursorControllerAdded(GVRCursorController gvrCursorController) {
            final GVRContext context = getGVRContext();
            if (gvrCursorController.getControllerType() == GVRControllerType.GAZE) {
                cursor = new GVRSceneObject(context,
                        context.createQuad(0.1f, 0.1f),
                        context.getAssetLoader().loadTexture(new GVRAndroidResource(context, R.raw.cursor)));
                cursor.getTransform().setPosition(0.0f, 0.0f, -1.5f*1.5f);
                context.getMainScene().getMainCameraRig().addChildObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
                gvrCursorController.setPosition(0.0f, 0.0f, -1.5f*1.5f);
                gvrCursorController.setNearDepth(DEPTH);
                gvrCursorController.setFarDepth(DEPTH);
            }
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController gvrCursorController) {
        }
    };

    private static final float DEPTH = -1.5f;
}
