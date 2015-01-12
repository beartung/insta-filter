package org.insta;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

import android.opengl.GLES20;

import org.insta.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

public class InstaFilter extends GPUImageFilter {

    private static final int FILTER_NUM = 18;
    private static InstaFilter[] filters;
    private static Bitmap overlayBitmap;

    public static Bitmap getOverlayBitmap(Context context) {
        if (overlayBitmap == null || overlayBitmap.isRecycled()) {
            overlayBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_map);
        }
        return overlayBitmap;
    }

    private static Bitmap vignetteBitmap;

    public static Bitmap getVignetteBitmap(Context context) {
        if (vignetteBitmap == null || vignetteBitmap.isRecycled()) {
            vignetteBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.vignette_map);
        }
        return vignetteBitmap;
    }

    public static InstaFilter getFilter(Context context, int index) {
        if (filters == null) {
            filters = new InstaFilter[FILTER_NUM];
        }
        try {
            /*
            if (filters[index] != null) {
                return filters[index];
            }
            */

            switch (index){
                case 0:
                    filters[index] = new IFNormalFilter(context);
                    break;
                case 1:
                    filters[index] = new IFAmaroFilter(context);
                    break;
                case 2:
                    filters[index] = new IFRiseFilter(context);
                    break;
                case 3:
                    filters[index] = new IFHudsonFilter(context);
                    break;
                case 4:
                    filters[index] = new IFXproIIFilter(context);
                    break;
                case 5:
                    filters[index] = new IFSierraFilter(context);
                    break;
                case 6:
                    filters[index] = new IFLomofiFilter(context);
                    break;
                case 7:
                    filters[index] = new IFEarlybirdFilter(context);
                    break;
                case 8:
                    filters[index] = new IFSutroFilter(context);
                    break;
                case 9:
                    filters[index] = new IFToasterFilter(context);
                    break;
                case 10:
                    filters[index] = new IFBrannanFilter(context);
                    break;
                case 11:
                    filters[index] = new IFInkwellFilter(context);
                    break;
                case 12:
                    filters[index] = new IFWaldenFilter(context);
                    break;
                case 13:
                    filters[index] = new IFHefeFilter(context);
                    break;
                case 14:
                    filters[index] = new IFValenciaFilter(context);
                    break;
                case 15:
                    filters[index] = new IFNashvilleFilter(context);
                    break;
                case 16:
                    filters[index] = new IF1977Filter(context);
                    break;
                case 17:
                    filters[index] = new IFLordKelvinFilter(context);
                    break;
            }
        } catch (Throwable e) {
        }
        return filters[index];
    }

    public static void destroyFilters() {
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                try {
                    if (filters[i] != null) {
                        filters[i].destroy();
                        filters[i] = null;
                    }
                } catch (Throwable e) {
                }
            }
        }
    }

    protected static final String VERTEX_SHADER = "attribute vec4 position;\n" +
            " attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            " varying vec2 textureCoordinate;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            " }\n";

    private int [] GL_TEXTURES = { GLES20.GL_TEXTURE3, GLES20.GL_TEXTURE4, GLES20.GL_TEXTURE5,
                                    GLES20.GL_TEXTURE6, GLES20.GL_TEXTURE7, GLES20.GL_TEXTURE8 };

    protected int textureNum; //MAX 6
    protected int [] coordinateAttributes;
    int [] inputTextureUniforms;
    int [] sourceTextures;
    ByteBuffer[] coordinatesBuffers;
    Bitmap[] bitmaps;

    public InstaFilter(String fragmentShader, int textures) {
        this(VERTEX_SHADER, fragmentShader, textures);
    }

    public InstaFilter(String vertexShader, String fragmentShader, int textures) {
        super(vertexShader, fragmentShader);
        textureNum = textures;
        coordinateAttributes = new int[textureNum];
        inputTextureUniforms = new int[textureNum];
        sourceTextures = new int[textureNum];
        for (int i = 0; i < textureNum; i++) {
            sourceTextures[i] = OpenGlUtils.NO_TEXTURE;
        }
        coordinatesBuffers = new ByteBuffer[textureNum];
        bitmaps = new Bitmap[textureNum];
        setRotation(Rotation.NORMAL, false, false);
    }

    public void setRotation(final Rotation rotation, final boolean flipHorizontal, final boolean flipVertical) {
        float[] buffer = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical);

        ByteBuffer bBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder());
        FloatBuffer fBuffer = bBuffer.asFloatBuffer();
        fBuffer.put(buffer);
        fBuffer.flip();

        for (int i = 0; i < textureNum; i++) {
            coordinatesBuffers[i] = bBuffer;
        }
    }

    @Override
    public void onInit() {
        super.onInit();
        int k;
        for (int i = 0; i < textureNum; i++) {
            k = i + 2;
            coordinateAttributes[i] = GLES20.glGetAttribLocation(getProgram(), String.format("inputTextureCoordinate%d", k));
            inputTextureUniforms[i] = GLES20.glGetUniformLocation(getProgram(), String.format("inputImageTexture%d", k));
            GLES20.glEnableVertexAttribArray(coordinateAttributes[i]);
            if (bitmaps[i] != null && !bitmaps[i].isRecycled()) {
                loadBitmap(i, bitmaps[i]);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (textureNum > 0) {
            try{
                GLES20.glDeleteTextures(1, sourceTextures, 0);

                for (int i = 0; i < textureNum; i++) {
                    sourceTextures[i] = OpenGlUtils.NO_TEXTURE;
                    if (bitmaps[i] != null && !bitmaps[i].isRecycled()) {
                        bitmaps[i].recycle();
                        bitmaps[i] = null;
                    }
                }
            } catch (Exception e) {
            }
        }
    }


    public void setBitmap(final int index, final Bitmap bitmap) {

        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        if (bitmap == null) {
            return;
        }
        bitmaps[index] = bitmap;
    }

    private void loadBitmap(final int index, final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        if (bitmap == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (bitmap == null || bitmap.isRecycled()) {
                    return;
                }
                if (sourceTextures[index] == OpenGlUtils.NO_TEXTURE) {
                    GLES20.glActiveTexture(GL_TEXTURES[index]);
                    sourceTextures[index] = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, false);
                }
            }
        });
    }

    @Override
    protected void onDrawArraysPre() {
        for (int i = 0; i < textureNum; i++) {
            GLES20.glEnableVertexAttribArray(coordinateAttributes[i]);
            GLES20.glActiveTexture(GL_TEXTURES[i]);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sourceTextures[i]);
            GLES20.glUniform1i(inputTextureUniforms[i], i + 3);

            coordinatesBuffers[i].position(0);
            GLES20.glVertexAttribPointer(coordinateAttributes[i], 2, GLES20.GL_FLOAT, false, 0, coordinatesBuffers[i]);
        }
    }

}
