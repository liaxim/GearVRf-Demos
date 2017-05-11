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

package org.gearvrf.gvr360Photo;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class Minimal360PhotoMain extends GVRMain {
    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        // get a handle to the scene
        GVRScene scene = gvrContext.getMainScene();

        final Field[] fields = R.raw.class.getFields();
        for (final Field f : fields) {
            if (f.getName().startsWith("photosphere")) {
                mFields.add(f.getInt(null));
            }
        }

        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        mSphereObject = new GVRSphereSceneObject(gvrContext, 72, 144, false);
        next();

        // add the scene object to the scene graph
        scene.addSceneObject(mSphereObject);
//        scene.getMainCameraRig().getLeftCamera().setBackgroundColor(1, 0, 0, 0);
//        scene.getMainCameraRig().getRightCamera().setBackgroundColor(0, 1, 0, 0);
    }

    private void next() {
        final int id = mFields.get(mIdx);
        GVRTexture t = getGVRContext().getAssetLoader().loadTexture(new GVRAndroidResource(getGVRContext(), id));
        mSphereObject.getRenderData().getMaterial().setMainTexture(t);

        ++mIdx;
        mIdx %= mFields.size();
        Log.i("mmarinov", "next " + mIdx);
    }

    public void onTap() {
        if (null != mFields) {
            next();
        }
    }

    private GVRSphereSceneObject mSphereObject;
    private int mIdx;
    private final ArrayList<Integer> mFields = new ArrayList<>();
}
