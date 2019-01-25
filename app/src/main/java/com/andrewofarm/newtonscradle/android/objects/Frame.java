package com.andrewofarm.newtonscradle.android.objects;

import com.andrewofarm.newtonscradle.android.programs.CradleShaderProgram;
import com.andrewofarm.newtonscradle.android.util.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static android.opengl.GLES20.*;
import static com.andrewofarm.newtonscradle.android.Constants.BYTES_PER_FLOAT;
import static com.andrewofarm.newtonscradle.android.Constants.BYTES_PER_INT;

/**
 * Created by Andrew on 1/12/17.
 */

public class Frame {

    public static final int POSITION_COMPONENT_COUNT = 3;
    public static final int NORMAL_COMPONENT_COUNT = 3;

    public static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT;
    public static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

    private static final float TUBE_RADIUS = 0.1f;
    private static final float CORNER_RADIUS = 0.3f;

    private static final int TUBE_FACES = 16;
    private static final int CURVE_FACES = 8;

    private static final int VERTEX_COUNT =
            (8 * 2 * TUBE_FACES) //8 cylindrical tubes
            + (8 * TUBE_FACES * (CURVE_FACES + 1)); //8 curved tubes
    private static final int INDEX_COUNT =
            (8 * 2 * (TUBE_FACES + 1)) //8 cylindrical tubes
            + (8 * (CURVE_FACES + 1) * (2 * (TUBE_FACES + 1) + 2)) //8 curved tubes
            + (16 * 2); //degenerate vertices

    final FloatBuffer vertexBuffer;
    final IntBuffer indexBuffer;

    int indexCount;

    public Frame(float xDimen, float yDimen, float zDimen) {
        //allocate buffers
        vertexBuffer = ByteBuffer.allocateDirect(
                VERTEX_COUNT * STRIDE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        indexBuffer = ByteBuffer.allocateDirect(
                INDEX_COUNT * BYTES_PER_INT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        ObjectBuilder.buildFrame(vertexBuffer, TOTAL_COMPONENT_COUNT, indexBuffer,
                new Vector3f(xDimen, yDimen, zDimen), TUBE_RADIUS, CORNER_RADIUS,
                TUBE_FACES, CURVE_FACES);
        indexCount = indexBuffer.position();
    }

    public void bindData(CradleShaderProgram shaderProgram) {
        int dataOffset = 0;

        vertexBuffer.position(dataOffset);
        glVertexAttribPointer(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(shaderProgram.aPositionLocation);
        dataOffset += POSITION_COMPONENT_COUNT;

        vertexBuffer.position(dataOffset);
        glVertexAttribPointer(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(shaderProgram.aNormalLocation);
    }

    public void draw() {
        indexBuffer.position(0);
        glDrawElements(GL_TRIANGLE_STRIP, indexCount, GL_UNSIGNED_INT, indexBuffer);
//         glDrawArrays(GL_POINTS, 0, VERTEX_COUNT);
    }
}
