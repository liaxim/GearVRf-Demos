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

package org.gearvrf.video.movie;

import android.media.MediaPlayer;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRVertexBuffer;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.video.shaders.AdditiveShader;
import org.gearvrf.video.shaders.RadiosityShader;

import java.io.IOException;

public class IMAXMovieTheater extends MovieTheater {

    GVRSceneObject background = null;
    GVRSceneObject additive = null;
    GVRSceneObject screen = null;

    private float mFadeWeight = 0.0f;
    private float mFadeTarget = 1.0f;

    public IMAXMovieTheater(GVRContext context, MediaPlayer player,
                            GVRExternalTexture screenTexture) {
        super(context);

        final GVRAssetLoader assetLoader = context.getAssetLoader();
        try {
            // background
            GVRMesh backgroundMesh = assetLoader.loadMesh(new GVRAndroidResource(context, "imax/cinema.obj"));
            GVRTexture backgroundLightOffTexture = assetLoader.loadTexture(new GVRAndroidResource(context, "imax/cinema_light_off.png"));
            GVRTexture backgroundLightOnTexture = assetLoader.loadTexture(new GVRAndroidResource(context, "imax/cinema_light_on.png"));

            background = new GVRSceneObject(context, backgroundMesh, backgroundLightOffTexture);
            final GVRRenderData backgroundRenderData = background.getRenderData();
            backgroundRenderData.setCullFace(GVRRenderPass.GVRCullFaceEnum.None);

            GVRMesh additiveMesh = assetLoader.loadMesh(new GVRAndroidResource(context, "imax/additive.obj"));
            GVRTexture additiveTexture = assetLoader.loadTexture(new GVRAndroidResource(context, "imax/additive.png"));

            additive = new GVRSceneObject(context, additiveMesh, additiveTexture);
            additive.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            additive.getRenderData().setRenderingOrder(2500);

            // radiosity
            backgroundRenderData.setMaterial(new GVRMaterial(context, new GVRShaderId(RadiosityShader.class)));
            final GVRMaterial backgroundMaterial = backgroundRenderData.getMaterial();
            backgroundMaterial.setTexture(RadiosityShader.TEXTURE_OFF_KEY, backgroundLightOffTexture);
            backgroundMaterial.setTexture(RadiosityShader.TEXTURE_ON_KEY, backgroundLightOnTexture);
            backgroundMaterial.setTexture(RadiosityShader.SCREEN_KEY, screenTexture);

            additive.getRenderData().setMaterial(new GVRMaterial(context, new GVRShaderId(AdditiveShader.class)));
            additive.getRenderData().getMaterial().setTexture(AdditiveShader.TEXTURE_KEY, additiveTexture);

            // screen
            GVRMesh screenMesh = assetLoader.loadMesh(new GVRAndroidResource(context, "imax/screen.obj"));
            screen = new GVRVideoSceneObject(context, screenMesh, player, screenTexture, GVRVideoSceneObject.GVRVideoType.MONO);
            screen.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);

            addChildObject(background);
            addChildObject(additive);
            addChildObject(screen);

            getTransform().setPosition(3.353f, -0.401f, 0.000003f);
            getTransform().rotateByAxisWithPivot(90.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideCinemaTheater() {
        background.getRenderData().setRenderMask(0);
        additive.getRenderData().setRenderMask(0);
        screen.getRenderData().setRenderMask(0);
    }

    @Override
    public void showCinemaTheater() {
        mFadeWeight = 0.0f;
        background.getRenderData().setRenderMask(GVRRenderData.GVRRenderMaskBit.Left
                | GVRRenderData.GVRRenderMaskBit.Right);
        additive.getRenderData().setRenderMask(GVRRenderData.GVRRenderMaskBit.Left
                | GVRRenderData.GVRRenderMaskBit.Right);
        screen.getRenderData().setRenderMask(GVRRenderData.GVRRenderMaskBit.Left
                | GVRRenderData.GVRRenderMaskBit.Right);
    }

    @Override
    public void switchOnLights() {
        background.getRenderData().getMaterial().setMainTexture(
                background.getRenderData().getMaterial().getTexture(RadiosityShader.TEXTURE_ON_KEY));
    }

    @Override
    public void switchOffLights() {
        background.getRenderData().getMaterial().setMainTexture(
                background.getRenderData().getMaterial().getTexture(RadiosityShader.TEXTURE_OFF_KEY));
    }

    @Override
    public void switchToImax() {
        // Nothing to do as Imax mode is not available
    }

    @Override
    public void setShaderValues() {
        mFadeWeight += 0.01f * (mFadeTarget - mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.WEIGHT_KEY, 0.1f);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.LIGHT_KEY, 1.0f);
        additive.getRenderData().getMaterial()
                .setFloat(AdditiveShader.WEIGHT_KEY, 0.1f);
        additive.getRenderData().getMaterial()
                .setFloat(AdditiveShader.FADE_KEY, mFadeWeight);
    }
}
