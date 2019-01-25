package com.andrewofarm.newtonscradle.android.objects;

import com.andrewofarm.newtonscradle.android.programs.SkyboxShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.andrewofarm.newtonscradle.android.Constants.*;

/**
 * Created by Andrew on 1/2/17.
 */

public class Skybox {

    private static final int POSITION_COMPONENT_COUNT = 3;

    private static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT;
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

    private static final int VERTEX_COUNT = 24;
    private static final int INDEX_COUNT = 36;

    private final FloatBuffer vertexArray;
    private final ByteBuffer indexArray;

    public Skybox() {
        //Create a unit cube
        vertexArray = ByteBuffer.allocateDirect(VERTEX_COUNT * STRIDE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(new float[] {
                -1,  1,  1,
                 1,  1,  1,
                -1, -1,  1,
                 1, -1,  1,
                -1,  1, -1,
                 1,  1, -1,
                -1, -1, -1,
                 1, -1, -1,});

        indexArray = ByteBuffer.allocateDirect(INDEX_COUNT * BYTES_PER_INT).put(new byte[] {
                //Front
                1, 3, 0,
                0, 3, 2,

                //Back
                4, 6, 5,
                5, 6, 7,

                //Left
                0, 2, 4,
                4, 2, 6,

                //Right
                5, 7, 1,
                1, 7, 3,

                //Top
                5, 1, 4,
                4, 1, 0,

                //Bottom
                6, 2, 7,
                7, 2, 3,
        });
    }

    public void bindData(SkyboxShaderProgram skyboxProgram) {
        vertexArray.position(0);
        glVertexAttribPointer(skyboxProgram.aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, vertexArray);
    }

    public void draw() {
        indexArray.position(0);
        glDrawElements(GL_TRIANGLES, INDEX_COUNT, GL_UNSIGNED_BYTE, indexArray);
    }
}
