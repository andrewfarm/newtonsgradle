package com.andrewofarm.newtonscradle.android.programs;

import android.content.Context;

import com.andrewofarm.newtonscradle.R;
import com.andrewofarm.newtonscradle.android.util.Vector3f;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Andrew on 1/12/17.
 */

public class MetalShaderProgram extends ShaderProgram {

    private static final String A_POSITION = "a_Position";
    private static final String A_NORMAL = "a_Normal";
    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_MODEL_ROTATION_MATRIX = "u_ModelRotationMatrix";
    private static final String U_COLOR = "u_Color";
    private static final String U_CAMERA_POSITION = "u_CameraPosition";
    private static final String U_CUBE_MAP_UNIT = "u_CubeMapUnit";

    public final int aPositionLocation;
    public final int aNormalLocation;
    public final int uMvpMatrixLocation;
    public final int uModelRotationMatrixLocation;
    public final int uColorLocation;
    public final int uCameraPositionLocation;
    public final int uCubeMapUnitLocation;

    public static final Metal STEEL = new Metal(1f, 1f, 1f);
    public static final Metal GOLD = new Metal(1f, 0.9f, 0.6f);
    public static final Metal GUN_STEEL = new Metal(0.5f, 0.5f, 0.5f);


    public MetalShaderProgram(Context context) {
        super(context, R.raw.vertex_shader, R.raw.metal_fragment_shader);

        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aNormalLocation = glGetAttribLocation(programID, A_NORMAL);
        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uModelRotationMatrixLocation = glGetUniformLocation(programID, U_MODEL_ROTATION_MATRIX);
        uColorLocation = glGetUniformLocation(programID, U_COLOR);
        uCameraPositionLocation = glGetUniformLocation(programID, U_CAMERA_POSITION);
        uCubeMapUnitLocation = glGetUniformLocation(programID, U_CUBE_MAP_UNIT);
    }

    public void setMvpMatrix(float[] matrix) {
        glUniformMatrix4fv(uMvpMatrixLocation, 1, false, matrix, 0);
    }

    public void setModelRotationMatrix(float[] matrix) {
        glUniformMatrix4fv(uModelRotationMatrixLocation, 1, false, matrix, 0);
    }

    public void setColor(float r, float g, float b) {
        glUniform3f(uColorLocation, r, g, b);
    }

    public void setColor(Metal metal) {
        setColor(metal.r, metal.g, metal.b);
    }

    public void setCameraPosition(Vector3f pos) {
        glUniform3f(uCameraPositionLocation, pos.x, pos.y, pos.z);
    }

    public void setCubeMapUnit(int textureID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        glUniform1i(uCubeMapUnitLocation, 0);
    }

    private static class Metal {
        private final float r, g, b;
        Metal(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
}
