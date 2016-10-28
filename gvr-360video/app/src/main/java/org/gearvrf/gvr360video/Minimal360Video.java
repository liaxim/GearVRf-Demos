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

package org.gearvrf.gvr360video;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;

import com.google.android.exoplayer.ExoPlayer;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRMaterial;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

public class Minimal360Video extends GVRMain
{
    /** Called when the activity is first created. */
    @Override
    public synchronized void onInit(GVRContext gvrContext) {
        if (null != mPlayer) {
            proceed();
        }
    }

    private void proceed() {
        GVRScene scene = getGVRContext().getNextMainScene();

        // create sphere / mesh
        GVRSphereSceneObject sphere = new GVRSphereSceneObject(getGVRContext(), 72, 144, false);
        GVRMesh mesh = sphere.getRenderData().getMesh();

        // create video scene
        GVRVideoSceneObject video = new GVRVideoSceneObject( getGVRContext(), mesh, mPlayer, GVRVideoType.MONO );
        video.setName( "video" );

        // apply video to scene
        scene.addSceneObject( video );
    }

    @Override
    public void onStep() {
    }

    private GVRVideoSceneObjectPlayer<?> mPlayer;

    public synchronized void setPlayer(GVRVideoSceneObjectPlayer<?> player) throws InterruptedException {
        mPlayer = player;
        if (null != getGVRContext()) {
            proceed();
        }
    }
}
