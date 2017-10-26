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

import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import java.io.IOException;

public class SampleActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new SampleMain());
    }

    private static class SampleMain extends GVRMain {
        @Override
        public void onInit(GVRContext gvrContext) {
            GVRScene scene = gvrContext.getMainScene();
            scene.setBackgroundColor(1, 1, 1, 1);

            try {
                GVRModelSceneObject button = gvrContext.getAssetLoader().loadModel("HoverButton_v3.fbx");
                button.getTransform().setPosition(0, 0, -7);
                button.getTransform().rotateByAxis(90, 1, 0, 0);
                scene.addSceneObject(button);

                button.getAnimations().get(3).setRepeatMode(GVRRepeatMode.REPEATED);
                button.getAnimations().get(3).setRepeatCount(-1);
                button.getAnimations().get(3).setOnFinish(new GVROnFinish() {
                    @Override
                    public void finished(GVRAnimation animation) {

                    }
                }).start(gvrContext.getAnimationEngine());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
