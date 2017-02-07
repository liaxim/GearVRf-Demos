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

package org.gearvrf.immersivepedia.scene;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.immersivepedia.GazeController;
import org.gearvrf.immersivepedia.R;

import java.io.IOException;

public class GridScene extends GVRScene {

    private GVRContext gvrContext;

    public GridScene(GVRContext gvrContext) throws IOException {
        super(gvrContext);
        this.gvrContext = gvrContext;

        setBackgroundColor(1, 1, 1, 1);

        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.grid));
        GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, 18.0f, 18.0f, texture);
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -12.0f);
        addSceneObject(sceneObject);

        hide();
    }

    public void show() {
        getGVRContext().setMainScene(this);
        GazeController.disableGaze();
        for (GVRSceneObject object : getWholeSceneObjects()) {
            if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                new GVROpacityAnimation(object, 1f, 1f).start(getGVRContext().getAnimationEngine());
            }
        }
    }

    public void hide() {
        for (GVRSceneObject object : getWholeSceneObjects()) {
            if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                object.getRenderData().getMaterial().setOpacity(0f);
            }
        }
    }
}
