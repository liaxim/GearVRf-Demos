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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;

public class YUV2RGBShader extends GVRShaderTemplate
{
    private static final String VERTEX_SHADER =
            "in vec4 a_position;\n" +
            "in vec2 a_texcoord;\n" +
            "out vec2 v_texCoord;\n" +
            "uniform mat4 u_mvp;\n" +
            "void main() {\n" +
            "  gl_Position = u_mvp * a_position;\n" +
            "  v_texCoord = a_texcoord;\n" +
            "}\n";

    //Our fragment shader code; takes Y,U,V values for each pixel and calculates R,G,B colors,
    //Effectively making YUV to RGB conversion
    private static String FRAGMENT_SHADER =
            "precision highp float;                             \n" +
            "in vec2 v_texCoord;                           \n" +
            "uniform sampler2D y_texture;                       \n" +
            "uniform sampler2D uv_texture;                      \n" +
            "out vec4 fragColor;\n" +

                    "void main (void){                                  \n" +
                    "   float r, g, b, y, u, v;                         \n" +

                    //We had put the Y values of each pixel to the R,G,B components by GL_LUMINANCE,
                    //that's why we're pulling it from the R component, we could also use G or B
                    "   y = texture(y_texture, v_texCoord).r;         \n" +

                    //We had put the U and V values of each pixel to the A and R,G,B components of the
                    //texture respectively using GL_LUMINANCE_ALPHA. Since U,V bytes are interspread
                    //in the texture, this is probably the fastest way to use them in the shader
                    "   u = texture(uv_texture, v_texCoord).a - 0.5;  \n" +
                    "   v = texture(uv_texture, v_texCoord).r - 0.5;  \n" +


                    //The numbers are just YUV to RGB conversion constants
                    "   r = y + 1.13983*v;                              \n" +
                    "   g = y - 0.39465*u - 0.58060*v;                  \n" +
                    "   b = y + 2.03211*u;                              \n" +

                    //We finally set the RGB color of our pixel
                    "   fragColor = vec4(r, g, b, 1.0);              \n" +
                    "}                                                  \n";

    public YUV2RGBShader(GVRContext gvrContext)
    {
        super("sampler2D y_texture, sampler2D uv_texture", 300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

}
