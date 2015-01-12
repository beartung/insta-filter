package org.insta;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.insta.R;

public class IFHefeFilter extends InstaFilter {

    public static final String SHADER = "precision lowp float;\n" +

    " varying highp vec2 textureCoordinate;\n" +
    " \n" +
    " uniform sampler2D inputImageTexture;\n" +
    " uniform sampler2D inputImageTexture2;  //edgeBurn\n" +
    " uniform sampler2D inputImageTexture3;  //hefeMap\n" +
    " uniform sampler2D inputImageTexture4;  //hefeGradientMap\n" +
    " uniform sampler2D inputImageTexture5;  //hefeSoftLight\n" +
    " uniform sampler2D inputImageTexture6;  //hefeMetal\n" +
    " \n" +
    " void main()\n" +
    "{   \n" +
    "    vec3 texel = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
    "    vec3 edge = texture2D(inputImageTexture2, textureCoordinate).rgb;\n" +
    "    texel = texel * edge;\n" +
    "    \n" +
    "    texel = vec3(\n" +
    "                 texture2D(inputImageTexture3, vec2(texel.r, .16666)).r,\n" +
    "                 texture2D(inputImageTexture3, vec2(texel.g, .5)).g,\n" +
    "                 texture2D(inputImageTexture3, vec2(texel.b, .83333)).b);\n" +
    "    \n" +
    "    vec3 luma = vec3(.30, .59, .11);\n" +
    "    vec3 gradSample = texture2D(inputImageTexture4, vec2(dot(luma, texel), .5)).rgb;\n" +
    "    vec3 final = vec3(\n" +
    "                      texture2D(inputImageTexture5, vec2(gradSample.r, texel.r)).r,\n" +
    "                      texture2D(inputImageTexture5, vec2(gradSample.g, texel.g)).g,\n" +
    "                      texture2D(inputImageTexture5, vec2(gradSample.b, texel.b)).b\n" +
    "                      );\n" +
    "    \n" +
    "    vec3 metal = texture2D(inputImageTexture6, textureCoordinate).rgb;\n" +
    "    vec3 metaled = vec3(\n" +
    "                        texture2D(inputImageTexture5, vec2(metal.r, texel.r)).r,\n" +
    "                        texture2D(inputImageTexture5, vec2(metal.g, texel.g)).g,\n" +
    "                        texture2D(inputImageTexture5, vec2(metal.b, texel.b)).b\n" +
    "                        );\n" +
    "    \n" +
    "    gl_FragColor = vec4(metaled, 1.0);\n" +
    "}";

    public IFHefeFilter(Context context) {
        super(SHADER, 5);
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.edge_burn);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.hefe_map);
        bitmaps[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.hefe_gradient_map);
        bitmaps[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.hefe_soft_light);
        bitmaps[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.hefe_metal);
    }

}
