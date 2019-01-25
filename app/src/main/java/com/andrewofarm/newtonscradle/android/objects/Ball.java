package com.andrewofarm.newtonscradle.android.objects;

import com.andrewofarm.newtonscradle.android.programs.CradleShaderProgram;
import com.andrewofarm.newtonscradle.android.programs.MetalShaderProgram;
import com.andrewofarm.newtonscradle.android.programs.ShaderProgram;
import com.andrewofarm.newtonscradle.android.util.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static android.opengl.GLES20.*;
import static com.andrewofarm.newtonscradle.android.Constants.*;

/**
 * Created by Andrew on 1/11/17.
 */

public class Ball {

    public static final int POSITION_COMPONENT_COUNT = 3;
    public static final int NORMAL_COMPONENT_COUNT = 3;

    public static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT;
    public static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

    private final int MERIDIANS = 32;
    private final int PARALLELS = (MERIDIANS / 2) - 1;

    public final int VERTEX_COUNT = MERIDIANS * PARALLELS + 2;
    public final int INDEX_COUNT = MERIDIANS * (2 * PARALLELS + 4);

    final FloatBuffer vertexBuffer;
    final IntBuffer indexBuffer;

    public Ball(float radius) {
        //allocate buffers
        vertexBuffer = ByteBuffer.allocateDirect(
                VERTEX_COUNT * STRIDE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        indexBuffer = ByteBuffer.allocateDirect(
                INDEX_COUNT * BYTES_PER_INT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        //build geometry
        vertexBuffer.position(0);
        indexBuffer.position(0);
        ObjectBuilder.buildSphere(vertexBuffer, TOTAL_COMPONENT_COUNT, indexBuffer,
                new Vector3f(), radius, MERIDIANS, PARALLELS);
    }

    public void bindData(MetalShaderProgram shaderProgram) {
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
        glDrawElements(GL_TRIANGLE_STRIP, INDEX_COUNT, GL_UNSIGNED_INT, indexBuffer);
//        glDrawArrays(GL_POINTS, 0, VERTEX_COUNT);
    }
}
