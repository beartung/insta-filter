package org.insta;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.insta.R;

public class IFLomofiFilter extends InstaFilter {

    public static final String SHADER = "precision lowp float;\n" +
            " varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " uniform sampler2D inputImageTexture3;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     \n" +
            "     vec3 texel = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
            "     \n" +
            "     vec2 red = vec2(texel.r, 0.16666);\n" +
            "     vec2 green = vec2(texel.g, 0.5);\n" +
            "     vec2 blue = vec2(texel.b, 0.83333);\n" +
            "     \n" +
            "     texel.rgb = vec3(\n" +
            "                      texture2D(inputImageTexture2, red).r,\n" +
            "                      texture2D(inputImageTexture2, green).g,\n" +
            "                      texture2D(inputImageTexture2, blue).b);\n" +
            "     \n" +
            "     vec2 tc = (2.0 * textureCoordinate) - 1.0;\n" +
            "     float d = dot(tc, tc);\n" +
            "     vec2 lookup = vec2(d, texel.r);\n" +
            "     texel.r = texture2D(inputImageTexture3, lookup).r;\n" +
            "     lookup.y = texel.g;\n" +
            "     texel.g = texture2D(inputImageTexture3, lookup).g;\n" +
            "     lookup.y = texel.b;\n" +
            "     texel.b    = texture2D(inputImageTexture3, lookup).b;\n" +
            "     \n" +
            "     gl_FragColor = vec4(texel,1.0);\n" +
            " }";

   public IFLomofiFilter(Context context) {
        super(SHADER, 2);
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.lomo_map);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.vignette_map);
    }

}
