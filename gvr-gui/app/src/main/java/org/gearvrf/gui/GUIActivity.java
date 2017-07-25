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

package org.gearvrf.gui;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.view.GVRFrameLayout;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class GUIActivity extends GVRActivity {
    private static final String TAG = GUIActivity.class.getSimpleName();
    private GUIMain main;
    private GVRFrameLayout frameLayout;

    private SeekBar seekBar;
    private ListView selection;
    private ListView colors;

    private static final List<String> shapes = new ArrayList<String>(3);
    private static final List<String> colorList = new ArrayList<String>(3);

    static {
        shapes.add("Sphere");
        shapes.add("Cube");
        shapes.add("Cylinder");
    }
    static {
        colorList.add("Red");
        colorList.add("Blue");
        colorList.add("Green");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        frameLayout = new GVRFrameLayout(this);
        frameLayout.setBackgroundColor(Color.WHITE);
        View.inflate(this, R.layout.interactive, frameLayout);
        seekBar = (SeekBar) frameLayout.findViewById(R.id.sizeSeekBar);
        selection = (ListView) frameLayout.findViewById(R.id.selection);
        colors = (ListView) frameLayout.findViewById(R.id.colors);
        colors.setBackgroundColor(Color.LTGRAY);
        selection.setBackgroundColor(Color.LTGRAY);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, shapes);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, colorList);
        selection.setAdapter(adapter2);
        colors.setAdapter(adapter3);
        main = new GUIMain(this, frameLayout);
        setMain(main, "gvr.xml");
        selection.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                main.setCurrentObject((String) adapterView.getItemAtPosition(i));
            }
        });
        colors.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                main.changeColor((String) adapterView.getItemAtPosition(i));
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                main.changeSize(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
