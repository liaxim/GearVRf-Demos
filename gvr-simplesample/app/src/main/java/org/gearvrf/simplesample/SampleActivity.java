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
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.IAssetEvents;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import java.io.IOException;
import java.util.EnumSet;

import static org.gearvrf.GVRImportSettings.CALCULATE_NORMALS;
import static org.gearvrf.GVRImportSettings.CALCULATE_SMOOTH_NORMALS;
import static org.gearvrf.GVRImportSettings.TRIANGULATE;

public class SampleActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new SampleMain());
    }

    private static class SampleMain extends GVRMain {
        @Override
        public void onInit(GVRContext gvrContext) throws IOException {
            GVRScene scene = gvrContext.getMainScene();
            scene.setBackgroundColor(1, 1, 1, 1);

            GVRDirectLight sunLight = new GVRDirectLight(gvrContext);
            sunLight.setAmbientIntensity(0.8f, 0.8f, 0.8f, 1.0f);
            sunLight.setDiffuseIntensity(0.9f, 0.9f, 0.9f, 1.0f);
            GVRSceneObject so = new GVRSceneObject(gvrContext);
            so.attachComponent(sunLight);
            so.getTransform().setPosition(0, 4, -35);
            scene.addSceneObject(so);

//            EnumSet<GVRImportSettings> importSettings = GVRImportSettings.getRecommendedSettingsWith(EnumSet.of(CALCULATE_NORMALS));
            EnumSet<GVRImportSettings> importSettings = EnumSet.of(CALCULATE_NORMALS, TRIANGULATE);
            gvrContext.getAssetLoader().loadModel("CUPIC_SUBMARINE.obj", importSettings, true, null);

            gvrContext.getEventReceiver().addListener(new GVREventListeners.AssetEvents() {
                @Override
                public void onAssetLoaded(GVRContext context, GVRSceneObject model, String filePath, String errors) {
                    model.getTransform()
                            .setRotationByAxis(45, 0, 1, 0)
                            .setScale(0.1f, 0.1f, 0.1f)
                            .setPositionZ(-40f);
                    getGVRContext().getMainScene().addSceneObject(model);
                }
            });
        }
    }
}
