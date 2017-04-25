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

package org.gearvrf.balloons;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.opengl.GLES30;
import android.view.MotionEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSharedTexture;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.IPickEvents;
import org.gearvrf.scene_objects.GVRCameraSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Vector2f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

public class BalloonMain extends GVRMain {

    final int[] textureNames = new int[2];
    public void onPreviewFrame(byte[] image) {
        Log.i("mmarinov", "onPreviewFrame");
        final BalloonActivity ba = (BalloonActivity)getGVRContext().getActivity();
        yBuffer.put(image, 0, ba.width*ba.height);
        yBuffer.position(0);

        //Copy the UV channels of the image into their buffer, the following (width*height/2) bytes are the UV channel; the U and V bytes are interspread
        uvBuffer.put(image, ba.width*ba.height, ba.width*ba.height/2);
        uvBuffer.position(0);

        getGVRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                Log.i("mmarinov", "sending data");
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureNames[0]);
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, ba.width, ba.height, 0,
                        GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, yBuffer);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureNames[1]);
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE_ALPHA, ba.width/2, ba.height/2,
                        0, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, uvBuffer);
            }
        });
    }

    public class PickHandler implements IPickEvents
    {
        public GVRSceneObject   PickedObject = null;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onExit(GVRSceneObject sceneObj) { }
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onNoPick(GVRPicker picker)
        {
            PickedObject = null;
        }
        public void onPick(GVRPicker picker)
        {
            GVRPickedObject picked = picker.getPicked()[0];
            PickedObject = picked.hitObject;
        }
    }

    private GVRScene mScene = null;
    private PickHandler mPickHandler;
    private ParticleEmitter mParticleSystem;
    private ArrayList<GVRMaterial> mMaterials;
    private GVRMesh     mSphereMesh;
    private Random      mRandom = new Random();
    private SoundPool   mAudioEngine;
    private SoundEffect mPopSound;
    public static MediaPlayer sMediaPlayer;
    private GVRTextViewSceneObject mScoreBoard;
    private boolean     mGameOver = false;
    private Integer     mScore = 0;
	private Timer		mTimer;

    private BalloonActivity mActivity;
    private GVRCameraSceneObject cameraObject;

    private String [] pokemon_imgs = new String[] {
            "amphaos.png",
            "bulbashar.png",
            "Charizard.png",
            "charmander.png",
            "Coolfeatures.png",
            "cresselia.png",
            "evee2.png",
            "fly.png",
            "genesect.png",
            "Ivysaur.png",
            "Jigglypuff.png",
            "Landourous.png",
            "lurario.png",
            "meloetta.png",
            "meowth.png",
            "Ninetales_3d.png",
            "pikachu.png",
            "Seedot.png",
            "snorlax.png",
            "squirtle.png",
            "Tentacruel.png",
            "tornadus.png",
            "tyranitar.png",
            "unicorn.png",
            "Victini.png"
    };

    BalloonMain(BalloonActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(final GVRContext context)
    {
       /*
         * Set the background color
         */
        mScene = context.getMainScene();
        mScene.setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        final BalloonActivity ba = (BalloonActivity)getGVRContext().getActivity();
        yBuffer = ByteBuffer.allocateDirect(ba.width*ba.height);
        uvBuffer = ByteBuffer.allocateDirect(ba.width*ba.height/2); //We have (width/2*height/2) pixels, each pixel is 2 bytes
        yBuffer.order(ByteOrder.nativeOrder());
        uvBuffer.order(ByteOrder.nativeOrder());

        final GVRSceneObject quad = new GVRSceneObject(context, 2, 1);
        quad.getRenderData().setShaderTemplate(YUV2RGBShader.class);
        mScene.addSceneObject(quad);

        context.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                Log.i("mmarinov", "run1");
                GLES30.glGenTextures(2, textureNames, 0);

                GVRSharedTexture sharedTextureY = new GVRSharedTexture(context, textureNames[0]);
                GVRSharedTexture sharedTextureUV = new GVRSharedTexture(context, textureNames[1]);
                quad.getRenderData().getMaterial().setTexture("y_texture", sharedTextureY);
                quad.getRenderData().getMaterial().setTexture("uv_texture", sharedTextureUV);
            }
        });
    }
    private ByteBuffer yBuffer;
    private ByteBuffer uvBuffer;

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                if (mPickHandler.PickedObject != null)
                {
                    onHit(mPickHandler.PickedObject);
                }
                break;

            default:
                break;
        }
    }

    private void onHit(GVRSceneObject sceneObj)
    {
        Particle particle = (Particle) sceneObj.getComponent(Particle.getComponentType());
        if (!mGameOver && (particle != null))
        {
            mPopSound.play();
            mParticleSystem.stop(particle);
            ++mScore;
            mScoreBoard.setText("Score: " + mScore.toString());
        }
    }
}
