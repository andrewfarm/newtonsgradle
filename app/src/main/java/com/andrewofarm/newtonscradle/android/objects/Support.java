package com.andrewofarm.newtonscradle.android.objects;

import com.andrewofarm.newtonscradle.android.programs.SimpleShaderProgram;
import com.andrewofarm.newtonscradle.android.util.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.andrewofarm.newtonscradle.android.Constants.BYTES_PER_FLOAT;

/**
 * Created by Andrew on 1/13/17.
 */

public class Support {

    private static final int POSITION_COMPONENT_COUNT = 3;

    private static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT;
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

    private static final int VERTEX_COUNT = 3;

    final FloatBuffer vertexBuffer;

    public Support(Vector3f end1, Vector3f center, Vector3f end2) {
        vertexBuffer = ByteBuffer.allocateDirect(
                VERTEX_COUNT * STRIDE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(new float[] {
                        end1.x, end1.y, end1.z,
                        center.x, center.y, center.z,
                        end2.x, end2.y, end2.z});
    }

    public void bindData(SimpleShaderProgram shaderProgram) {
        vertexBuffer.position(0);
        glVertexAttribPointer(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(shaderProgram.aPositionLocation);
    }

    public void draw() {
        glDrawArrays(GL_LINE_STRIP, 0, VERTEX_COUNT);
    }
}
