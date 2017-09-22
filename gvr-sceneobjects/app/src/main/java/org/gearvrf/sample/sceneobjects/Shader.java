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


package org.gearvrf.sample.sceneobjects;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;

public class Shader extends GVRShaderTemplate
{
    private static final String VERTEX_SHADER =
        "in vec4 a_position;\n" +
        "uniform mat4 u_mvp;\n" +
        "in vec2 a_texcoord;\n" +
        "out vec2 v_texcoord;\n" +

        "uniform highp float texelWidth;\n" +
        "uniform highp float texelHeight;\n" +
        "out vec2 textureCoordinate;\n" +
        "out vec2 leftTextureCoordinate;\n" +
        "out vec2 rightTextureCoordinate;\n" +
        "out vec2 topTextureCoordinate;\n" +
        "out vec2 topLeftTextureCoordinate;\n" +
        "out vec2 topRightTextureCoordinate;\n" +
        "out vec2 bottomTextureCoordinate;\n" +
        "out vec2 bottomLeftTextureCoordinate;\n" +
        "out vec2 bottomRightTextureCoordinate;\n" +

        "void main() {\n" +
        "  v_texcoord = a_texcoord.xy;\n" +
        "  gl_Position = u_mvp * a_position;\n" +
        "  vec2 widthStep = vec2(texelWidth, 0.0);\n" +
        "  vec2 heightStep = vec2(0.0, texelHeight);\n" +
        "  vec2 widthHeightStep = vec2(texelWidth, texelHeight);\n" +
        "  vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);\n" +
        "  textureCoordinate = a_texcoord.xy;\n" +
        "  leftTextureCoordinate = a_texcoord.xy - widthStep;\n" +
        "  rightTextureCoordinate = a_texcoord.xy + widthStep;\n" +
        "  topTextureCoordinate = a_texcoord.xy - heightStep;\n" +
        "  topLeftTextureCoordinate = a_texcoord.xy - widthHeightStep;\n" +
        "  topRightTextureCoordinate = a_texcoord.xy + widthNegativeHeightStep;\n" +
        "  bottomTextureCoordinate = a_texcoord.xy + heightStep;\n" +
        "  bottomLeftTextureCoordinate = a_texcoord.xy - widthNegativeHeightStep;\n" +
        "  bottomRightTextureCoordinate = a_texcoord.xy + widthHeightStep;\n"+
        "}\n";

    private static final String FRAGMENT_SHADER =
        "#extension GL_OES_EGL_image_external : enable\n" +
        "#extension GL_OES_EGL_image_external_essl3 : enable\n" +
        "precision mediump float;\n" +
        "uniform samplerExternalOES u_texture;\n" +
        "in vec2 v_texcoord;\n" +
        "out vec4 fragColor;\n" +

        "uniform mediump mat4 convolutionMatrix;\n" +
        "in vec2 textureCoordinate;\n" +
        "in vec2 leftTextureCoordinate;\n" +
        "in vec2 rightTextureCoordinate;\n" +
        "in vec2 topTextureCoordinate;\n" +
        "in vec2 topLeftTextureCoordinate;\n" +
        "in vec2 topRightTextureCoordinate;\n" +
        "in vec2 bottomTextureCoordinate;\n" +
        "in vec2 bottomLeftTextureCoordinate;\n" +
        "in vec2 bottomRightTextureCoordinate;\n" +
        "mediump mat3 matrix = mat3(\n" +
        "            3.25f, 3.5f, 3.25f,\n" +
        "            3.5f,  5.0f, 3.5f,\n" +
        "            3.25f, 3.5f, 3.25f);\n" +
        "void main() {\n" +
        "  mediump vec4 bottomColor = texture(u_texture, bottomTextureCoordinate);\n" +
        "  mediump vec4 bottomLeftColor = texture(u_texture, bottomLeftTextureCoordinate);\n" +
        "  mediump vec4 bottomRightColor = texture(u_texture, bottomRightTextureCoordinate);\n" +
        "  mediump vec4 centerColor = texture(u_texture, textureCoordinate);\n" +
        "  mediump vec4 leftColor = texture(u_texture, leftTextureCoordinate);\n" +
        "  mediump vec4 rightColor = texture(u_texture, rightTextureCoordinate);\n" +
        "  mediump vec4 topColor = texture(u_texture, topTextureCoordinate);\n" +
        "  mediump vec4 topRightColor = texture(u_texture, topRightTextureCoordinate);\n" +
        "  mediump vec4 topLeftColor = texture(u_texture, topLeftTextureCoordinate);\n" +
        "  mediump vec4 resultColor = topLeftColor * matrix[0][0] + topColor * matrix[0][1] + topRightColor * matrix[0][2];\n" +
        "  resultColor += leftColor * matrix[1][0] + centerColor * matrix[1][1] + rightColor * matrix[1][2];\n" +
        "  resultColor += bottomLeftColor * matrix[2][0] + bottomColor * matrix[2][1] + bottomRightColor * matrix[2][2];\n" +

//        "  if (resultColor.r < 0.01 && resultColor.g < 0.01 && resultColor.b < 0.01) resultColor.a = 0.0;" +
        "  fragColor = resultColor;\n" +
        "}\n";

    /**
     * A simple shader that lets you choose a solid color for your scene object.
     * 1. set the shader for your scene object via getRenderData().setShaderTemplate(ColorShader.class).
     * 2. specify the color to be used via getRenderData().getMaterial().setVec4("u_color", R, G, B, A).
     */
    public Shader(GVRContext gvrcontext)
    {
        super("float texelWidth; float texelHeight; mat4 convolutionMatrix; mat4 u_mvp;", 300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

}
