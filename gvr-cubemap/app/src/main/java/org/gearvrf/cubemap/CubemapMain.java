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

package org.gearvrf.cubemap;

import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;

import java.util.concurrent.Future;

public class CubemapMain extends GVRMain {

    private static final float CUBE_WIDTH = 20.0f;
    private static final String TAG = "CubemapMain";

    private GVRContext mGVRContext = null;

    private final static int CubemapGlashow = 0;
    private final static int FrozenEia = 1;
    private final static int FrozenGrid = 2;
    private final static int FrozenBlackDot = 3;
    private static final int MAX_ENVIRONMENTS = 4;

    private int mEnvironmentType = CubemapGlashow;

    private GVRMaterial mCompressedCubemapMaterial;
    private GVRSceneObject mSceneObject;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getMainScene();
        scene.setFrustumCulling(true);

        // Compressed cubemap texture
        final Future<GVRTexture> futureCompressedCubemapTexture = gvrContext.getAssetLoader().loadFutureCubemapTexture(new GVRAndroidResource(mGVRContext, R.raw.glasgow_university));
        mCompressedCubemapMaterial = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Cubemap.ID);
        mCompressedCubemapMaterial.setMainTexture(futureCompressedCubemapTexture);

        apply(scene);
    }

    private void apply(GVRScene scene) {
        switch (mEnvironmentType) {
            case CubemapGlashow:
                if (null != mSceneObject) {
                    scene.removeSceneObject(mSceneObject);
                    scene.getMainCameraRig().setCameraRigType(GVRCameraRig.GVRCameraRigType.Free.ID);
                }
                mSceneObject = new GVRCubeSceneObject(mGVRContext, false, mCompressedCubemapMaterial);
                mSceneObject.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH, CUBE_WIDTH);
                scene.addSceneObject(mSceneObject);
                break;
            case FrozenEia: {
                applyFrozen(R.raw.eia1956);
            } break;
            case FrozenBlackDot: {
                applyFrozen(R.raw.black_dot_array);
            } break;
            case FrozenGrid: {
                applyFrozen(R.raw.grid);
            } break;
        }
    }

    private void applyFrozen(int resource) {
        final GVRScene scene = getGVRContext().getMainScene();
        if (null != mSceneObject) {
            scene.removeSceneObject(mSceneObject);
            scene.getMainCameraRig().setCameraRigType(GVRCameraRig.GVRCameraRigType.Freeze.ID);
        }

        GVRTexture t = getGVRContext().getAssetLoader().loadTexture(new GVRAndroidResource(getGVRContext(), resource));
        mSceneObject = new GVRSceneObject(mGVRContext, 20,20, t);
        mSceneObject.getTransform().setPositionZ(-11);
        scene.addSceneObject(mSceneObject);
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouch() {
        if (null != mGVRContext) {
            mGVRContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    mGVRContext.getMainScene().clear();
                    ++mEnvironmentType;
                    mEnvironmentType %= MAX_ENVIRONMENTS;
                    Log.i(TAG, "mEnvironmentType: " + mEnvironmentType);
                    apply(mGVRContext.getMainScene());
                }
            });
        }
    }
}

