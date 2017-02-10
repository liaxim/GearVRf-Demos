package com.samsung.mps.sample;

import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPostEffectShaderId;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.IPickEvents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class SampleMain extends GVRMain implements VRTouchPadGestureDetector.OnTouchPadGestureListener{
    private static final String TAG = SampleMain.class.getSimpleName();
    private static final float CUBE_WIDTH = 100.0f;
    private static final String APP_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "HomeApp" + File.separator;
    private GVRScene mainScene;
    private GVRContext gvrContext;
    private static String[] files = new String[4];
    private float lastX = 0, lastY = 0;
    private boolean isOnClick = false;
    private static final float MOVE_SCALE_FACTOR = 0.05f;
    private static final float MOVE_THRESHOLD = 400f;
    private static final float MOVE_THRESHOLD_X = 5f;
    private static final float MIN_POSSIBLE_Z = -40.0f;
    private static final float MAX_POSSIBLE_Z = -10.0f;

    static {
        files[0] = new String("1.jpg");
        files[1] = new String("2.jpg");
        files[2] = new String("3.jpg");
        files[3] = new String("4.jpg");
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onSwipe(MotionEvent e, VRTouchPadGestureDetector.SwipeDirection
            swipeDirection, float velocityX, float velocityY) {
        if(swipeDirection == VRTouchPadGestureDetector.SwipeDirection.Forward){
            currentIndex = currentIndex + 1;
            currentIndex = currentIndex % skyboxes.size();
            changeSkyBox();
        }else if(swipeDirection == VRTouchPadGestureDetector.SwipeDirection.Backward){
            currentIndex = currentIndex - 1;
            if(currentIndex < 0){
                currentIndex = skyboxes.size() -1;
            }

            changeSkyBox();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        if(arg2 <MOVE_THRESHOLD_X) {
            mPickHandler.setDistance(arg3 * -MOVE_SCALE_FACTOR);
        }
        return true;
    }

    public class PickHandler implements IPickEvents {
        private GVRSceneObject attachedObject = null;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            attachedObject = sceneObj;
        }

        public void onExit(GVRSceneObject sceneObj) {
            attachedObject = null;
        }

        public void onNoPick(GVRPicker picker) {
        }

        public void onPick(GVRPicker picker) {
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
        }

        public void setDistance(float distance) {
            if (attachedObject == null) {
                return;
            }

            GVRTransform transform = attachedObject.getParent().getTransform();

            float x = transform.getPositionX();
            float z = transform.getPositionZ();

            if (x == 0.0f) {
                //front or back
                if (z < 0) {
                    //front
                    transform.translate(0.0f, 0.0f, distance);
                    if (transform.getPositionZ() < MIN_POSSIBLE_Z) {
                        transform.setPositionZ(MIN_POSSIBLE_Z);
                    }
                    if (transform.getPositionZ() > MAX_POSSIBLE_Z) {
                        transform.setPositionZ(MAX_POSSIBLE_Z);
                    }
                } else {
                    transform.translate(0.0f, 0.0f, -distance);
                    if (transform.getPositionZ() > -MIN_POSSIBLE_Z) {
                        transform.setPositionZ(-MIN_POSSIBLE_Z);
                    }
                    if (transform.getPositionZ() < -MAX_POSSIBLE_Z) {
                        transform.setPositionZ(-MAX_POSSIBLE_Z);
                    }
                }
            } else { // z== 0.0f
                if (x < 0) {
                    //front
                    transform.translate(distance, 0.0f, 0.0f);
                    if (transform.getPositionX() < MIN_POSSIBLE_Z) {
                        transform.setPositionX(MIN_POSSIBLE_Z);
                    }
                    if (transform.getPositionX() > MAX_POSSIBLE_Z) {
                        transform.setPositionX(MAX_POSSIBLE_Z);
                    }
                } else {
                    transform.translate(-distance, 0.0f, 0.0f);
                    if (transform.getPositionX() > -MIN_POSSIBLE_Z) {
                        transform.setPositionX(-MIN_POSSIBLE_Z);
                    }
                    if (transform.getPositionX() < -MAX_POSSIBLE_Z) {
                        transform.setPositionX(-MAX_POSSIBLE_Z);
                    }
                }
            }
        }
    }

    private PickHandler mPickHandler = new PickHandler();

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        mainScene = gvrContext.getMainScene();
        this.gvrContext = gvrContext;

        addSurroundings(gvrContext, mainScene);
        float angle = 0.0f;
        mainScene.getEventReceiver().addListener(mPickHandler);
        GVRPicker mPicker = new GVRPicker(gvrContext, mainScene);

        // front
        GVRSceneObject sceneObject = null;
        sceneObject = createQuad(files[0], 0.0f, -15.0f, 0, -0.1f, 0.0f);
        mainScene.addSceneObject(sceneObject);

        // back
        sceneObject = createQuad(files[1], 0.0f, 15.0f, 0, 0.1f, 180.0f);

        mainScene.addSceneObject(sceneObject);
        // left
        sceneObject = createQuad(files[2], -15.0f, 0.0f, -0.1f, 0.0f, 90.0f);
        mainScene.addSceneObject(sceneObject);

        // right
        sceneObject = createQuad(files[3], 15.0f, 0.0f, 0.1f, 0.0f, -90.0f);
        mainScene.addSceneObject(sceneObject);
    }

    GVRSceneObject createQuad(String filename, float x, float z, float borderX, float borderZ,
                              float angle) throws
            IOException {
        GVRSceneObject parent = new GVRSceneObject(gvrContext);
        parent.getTransform().setPosition(x, 0.0f, z);
        GVRSceneObject quad = getQuad(filename);

        quad.getTransform().rotateByAxis(angle, 0.0f, 1.0f, 0.0f);
        parent.addChildObject(quad);
        quad.attachComponent(new GVRMeshCollider(gvrContext, true));

        GVRSceneObject border = new GVRSceneObject(gvrContext, gvrContext.createQuad(16.2f,
                9.2f));
        GVRMaterial material = new GVRMaterial(gvrContext);
        Future<GVRTexture> futureTexture = gvrContext.loadFutureTexture(new GVRAndroidResource
                (gvrContext, "texture.png"));
        material.setMainTexture(futureTexture);
        border.getRenderData().setMaterial(material);
        border.getTransform().setPosition(borderX, 0.0f, borderZ);
        border.getTransform().rotateByAxis(angle, 0.0f, 1.0f, 0.0f);

        parent.addChildObject(border);

        return parent;
    }

    List<Future<GVRTexture>> skyboxes = new ArrayList<Future<GVRTexture>>();
    int currentIndex = 0;

    // The assets for the Cubemap are taken from the Samsung Developers website:
    // http://www.samsung.com/us/samsungdeveloperconnection/developer-resources/
    // gear-vr/apps-and-games/exercise-2-creating-the-splash-scene.html
    private void addSurroundings(GVRContext gvrContext, GVRScene scene) {
        FutureWrapper<GVRMesh> futureQuadMesh = new FutureWrapper<GVRMesh>(
                gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH));
        String fullPath = APP_PATH + "skybox";


        File dir = new File(fullPath);

        File[] list = dir.listFiles();

        for (File file : list) {
            Future<GVRTexture> futureCubemapTexture = null;
            try {
                futureCubemapTexture = gvrContext
                        .loadFutureCubemapTexture(
                                new GVRAndroidResource(fullPath + File.separator + file.getName()));
                skyboxes.add(futureCubemapTexture);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        Future<GVRTexture> futureCubemapTexture = null;

        futureCubemapTexture = skyboxes.get(0);
        GVRMaterial cubemapMaterial = new GVRMaterial(gvrContext,
                GVRMaterial.GVRShaderType.Cubemap.ID);
        cubemapMaterial.setMainTexture(futureCubemapTexture);

        // surrounding cube
        GVRSceneObject frontFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        frontFace.getRenderData().setMaterial(cubemapMaterial);
        frontFace.setName("front");
        frontFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        scene.addSceneObject(frontFace);
        frontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        backFace.getRenderData().setMaterial(cubemapMaterial);
        backFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        leftFace.getRenderData().setMaterial(cubemapMaterial);
        leftFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        rightFace.getRenderData().setMaterial(cubemapMaterial);
        rightFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        topFace.getRenderData().setMaterial(cubemapMaterial);
        topFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        bottomFace.getRenderData().setMaterial(cubemapMaterial);
        bottomFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder
                .BACKGROUND);
        bottomFace.setName("bottom");
        scene.addSceneObject(bottomFace);
        bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
        bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
    }

    private GVRSceneObject getQuad(String filename) {

        GVRSceneObject quad = new GVRSceneObject(gvrContext, gvrContext.createQuad(16.0f, 9.0f));
        GVRMaterial material = new GVRMaterial(gvrContext);
        try {
            String fullPath = APP_PATH + filename;
            Future<GVRTexture> futureTexture = gvrContext.loadFutureTexture(new GVRAndroidResource
                    (fullPath));
            material.setMainTexture(futureTexture);
            quad.getRenderData().setMaterial(material);

                } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return quad;
        }

    void changeSkyBox() {
        Future<GVRTexture> futureCubemapTexture = skyboxes.get(currentIndex);
        GVRSceneObject sceneObject = mainScene.getSceneObjectByName("front");
        sceneObject.getRenderData().getMaterial().setMainTexture(futureCubemapTexture);
    }

    @Override
    public void onStep() {

    }
}

