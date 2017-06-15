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

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class CubemapActivity extends GVRActivity {

    private VRTouchPadGestureDetector mDetector;
    CubemapMain main = new CubemapMain();

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(main, "gvr.xml");
        mDetector = new VRTouchPadGestureDetector(new VRTouchPadGestureDetector.OnTouchPadGestureListener() {
            @Override
            public boolean onSingleTap(MotionEvent e) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onSwipe(MotionEvent e, VRTouchPadGestureDetector.SwipeDirection swipeDirection, float velocityX, float velocityY) {
                if (VRTouchPadGestureDetector.SwipeDirection.Up == swipeDirection) {
                    main.onSwipeUp();
                } else if (VRTouchPadGestureDetector.SwipeDirection.Down == swipeDirection) {
                    main.onSwipeDown();
                } else if (VRTouchPadGestureDetector.SwipeDirection.Forward == swipeDirection) {
                    main.onSwipeForward();
                } else if (VRTouchPadGestureDetector.SwipeDirection.Backward == swipeDirection) {
                    main.onSwipeBackward();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //mDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            main.onTouch();
            return true;
        }

        return super.onTouchEvent(event);
    }
}
