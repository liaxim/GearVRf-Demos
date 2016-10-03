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

package org.gearvrf.opacityanigallery;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPostEffect;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;

import android.media.MediaPlayer;

public class GalleryMain extends GVRMain {

    private static final float ANIMATION_DURATION = 0.3f;
    private static final float SELECTED_SCALE = 2.0f;

    private List<GVRSceneObject> mBoards = new ArrayList<GVRSceneObject>();
    private GVRSceneObject mBoardParent;
    private int mSelected = 0;
    private GVRAnimationEngine mAnimationEngine;

    private static final int LOOK_UP = 1;
    private static final int LOOK_FRONT = 0;
    private static final int LOOK_DOWN = -1;
    private static final float LOOK_AT_THRESHOLD = 0.2f;
    private int mLookAtMode = LOOK_FRONT;
    private GVRAnimation mRotationAnimation;

    @Override
    public void onInit(GVRContext gvrContext) {
        mAnimationEngine = gvrContext.getAnimationEngine();

        final GVRScene mainScene = gvrContext.getNextMainScene();
        final GVRCameraRig mainCameraRig = mainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mainCameraRig.getRightCamera().setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);

        final GVRMesh sphereMesh = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.sphere_mesh));
        final GVRSceneObject background = new GVRSceneObject(gvrContext, sphereMesh,
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.background)));
        background.getTransform().setScale(10.0f, 10.0f, 10.0f);
        mainScene.addSceneObject(background);

        final List<GVRTexture> numberTextures = new ArrayList<GVRTexture>();
        final int[] resourceIds = new int[] { R.drawable.photo_1,
                R.drawable.photo_2, R.drawable.photo_3,
                R.drawable.photo_4, R.drawable.photo_5,
                R.drawable.photo_6, R.drawable.photo_7,
                R.drawable.photo_8, R.drawable.photo_9 };
        for (final int id : resourceIds) {
            numberTextures.add(gvrContext.loadTexture(new GVRAndroidResource(gvrContext, id)));
        }

        for (int i = 0, size = numberTextures.size(); i < size; ++i) {
            final GVRSceneObject number = new GVRSceneObject(gvrContext, 2.0f, 1.0f, numberTextures.get(i));
            final GVRTransform transform = number.getTransform();
            transform.setPosition(0.0f, 0.0f, -5.0f);

            final float degree = 360.0f * i / (size + 1);
            transform.rotateByAxisWithPivot(degree, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
            number.getRenderData().getMaterial().setOpacity(0.0f);
            number.setEnable(false);

            mBoards.add(number);
        }

        final MediaPlayer mediaPlayer = MediaPlayer.create(gvrContext.getContext(), R.raw.tron);
        final GVRVideoSceneObject video = new GVRVideoSceneObject(gvrContext, 2.0f, 1.0f, mediaPlayer, GVRVideoType.MONO);
        video.setName("video");
        video.getRenderData().getMaterial().setOpacity(0.0f);
        video.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        final float degree = 360.0f * (numberTextures.size()) / (numberTextures.size() + 1);
        video.getTransform().rotateByAxisWithPivot(degree, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        video.setEnable(false);
        mBoards.add(video);

        mBoardParent = new GVRSceneObject(gvrContext);
        for (final GVRSceneObject board : mBoards) {
            mBoardParent.addChildObject(board);
        }

        mainScene.addSceneObject(mBoardParent);

        final GVRSceneObject selected = mBoards.get(mSelected);
        selected.getTransform().setScale(SELECTED_SCALE, SELECTED_SCALE, 0.0f);
        selected.setEnable(true);
        selected.getRenderData().getMaterial().setOpacity(1.0f);

        final CustomPostEffectShaderManager shaderManager = new CustomPostEffectShaderManager(gvrContext);
        final GVRPostEffect postEffect = new GVRPostEffect(gvrContext, shaderManager.getShaderId());
        postEffect.setVec3("ratio_r", 0.393f, 0.769f, 0.189f);
        postEffect.setVec3("ratio_g", 0.349f, 0.686f, 0.168f);
        postEffect.setVec3("ratio_b", 0.272f, 0.534f, 0.131f);
        mainCameraRig.getLeftCamera().addPostEffect(postEffect);
        mainCameraRig.getRightCamera().addPostEffect(postEffect);
    }

    @Override
    public void onStep() {
        if (mRotationAnimation != null && mRotationAnimation.isFinished()) {
            mRotationAnimation = null;
        }

        final float lookAtY = getGVRContext().getMainScene().getMainCameraRig().getLookAt()[1];
        if (mRotationAnimation == null) {
            if (mLookAtMode == LOOK_FRONT) {
                if (lookAtY > LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_UP;
                    rotateCounterClockwise();
                } else if (lookAtY < -LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_DOWN;
                    rotateClockwise();
                }
            }
            if (mLookAtMode == LOOK_UP) {
                if (lookAtY < -LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_DOWN;
                    rotateClockwise();
                } else if (lookAtY < LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_FRONT;
                }
            }
            if (mLookAtMode == LOOK_DOWN) {
                if (lookAtY > LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_UP;
                    rotateCounterClockwise();
                } else if (lookAtY > -LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_FRONT;
                }
            }
        }
    }

    private void rotateCounterClockwise() {
        rotateImpl(360, -1);
    }

    private void rotateClockwise() {
        rotateImpl(-360, 1);
    }

    private void rotateImpl(final float rotationAngle, final int increment) {
        final int boardsSize = mBoards.size();

        final GVRSceneObject previouslySelected = mBoards.get(mSelected);
        mRotationAnimation = new GVRRotationByAxisWithPivotAnimation(
                mBoardParent, ANIMATION_DURATION, rotationAngle / boardsSize,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f).start(mAnimationEngine);

        new GVRScaleAnimation(previouslySelected, ANIMATION_DURATION,
                1.0f / SELECTED_SCALE, 1.0f / SELECTED_SCALE, 1.0f)
                .start(mAnimationEngine);

        new GVROpacityAnimation(previouslySelected, ANIMATION_DURATION, 0f)
                .start(mAnimationEngine).setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation animation) {
                previouslySelected.setEnable(false);
            }
        });

        if (previouslySelected instanceof GVRVideoSceneObject) {
            final GVRVideoSceneObject video = (GVRVideoSceneObject) mBoards.get(mSelected);
            video.getMediaPlayer().pause();
        }

        mSelected += increment;
        mSelected = (((mSelected % boardsSize) + boardsSize) % boardsSize);

        final GVRSceneObject selected = mBoards.get(mSelected);
        selected.setEnable(true);

        new GVRScaleAnimation(selected, ANIMATION_DURATION,
                SELECTED_SCALE, SELECTED_SCALE, 1.0f).start(mAnimationEngine);

        new GVROpacityAnimation(selected, ANIMATION_DURATION, 1f)
                .start(mAnimationEngine);

        if (selected instanceof GVRVideoSceneObject) {
            final GVRVideoSceneObject video = (GVRVideoSceneObject) mBoards.get(mSelected);
            video.getMediaPlayer().start();
        }
    }

}
