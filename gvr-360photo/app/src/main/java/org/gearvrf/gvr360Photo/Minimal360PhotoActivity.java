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

import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;

import java.lang.reflect.Method;

public class Minimal360PhotoActivity extends GVRActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Minimal360PhotoMain main = new Minimal360PhotoMain();
        setMain(main);

        mDetector = new VRTouchPadGestureDetector(new VRTouchPadGestureDetector.OnTouchPadGestureListener() {

            @Override
            public boolean onSingleTap(MotionEvent e) {
                main.onTap();
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
            }

            @Override
            public boolean onSwipe(MotionEvent e, VRTouchPadGestureDetector.SwipeDirection swipeDirection, float velocityX, float velocityY) {
                try {
                    final GVRContext ctx = main.getGVRContext();

                    if (swipeDirection == VRTouchPadGestureDetector.SwipeDirection.Up) {
                        final Method m = ctx.getClass().getMethod("changeIPDMeters", float.class);
                        m.invoke(ctx, 0.001f);
                    } else if (swipeDirection == VRTouchPadGestureDetector.SwipeDirection.Down) {
                        final Method m = ctx.getClass().getMethod("changeIPDMeters", float.class);
                        m.invoke(ctx, -0.001f);
                    } else if (swipeDirection == VRTouchPadGestureDetector.SwipeDirection.Backward) {
                        final Method m = ctx.getClass().getMethod("changeShiftScreenCenterMeters", float.class);
                        m.invoke(ctx, 0.001f);
                    } else if (swipeDirection == VRTouchPadGestureDetector.SwipeDirection.Forward) {
                        final Method m = ctx.getClass().getMethod("changeShiftScreenCenterMeters", float.class);
                        m.invoke(ctx, -0.001f);
                    }
                } catch (final Exception exc) {
                    exc.printStackTrace();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private VRTouchPadGestureDetector mDetector;
}
