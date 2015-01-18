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

public abstract class InstaFilter extends GPUImageFilter {

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

    protected int [] GL_TEXTURES = { GLES20.GL_TEXTURE3, GLES20.GL_TEXTURE4, GLES20.GL_TEXTURE5,
                                    GLES20.GL_TEXTURE6, GLES20.GL_TEXTURE7, GLES20.GL_TEXTURE8 };

    protected int textureNum; //MAX 6
    protected int [] coordinateAttributes;
    protected int [] inputTextureUniforms;
    protected int [] sourceTextures;
    protected ByteBuffer[] coordinatesBuffers;
    protected Bitmap[] bitmaps;

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
