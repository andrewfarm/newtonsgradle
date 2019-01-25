package com.andrewofarm.newtonscradle.android.programs;

import android.content.Context;

import com.andrewofarm.newtonscradle.R;

import static android.opengl.GLES20.*;

/**
 * Created by Andrew on 1/11/17.
 */

public class CradleShaderProgram extends ShaderProgram {

    private static final String A_POSITION = "a_Position";
    private static final String A_NORMAL = "a_Normal";
    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_MODEL_ROTATION_MATRIX = "u_ModelRotationMatrix";
    private static final String U_COLOR = "u_Color";
    private static final String U_CAMERA_POSITION = "u_CameraPosition";

    public final int aPositionLocation;
    public final int aNormalLocation;
    public final int uMvpMatrixLocation;
    public final int uModelRotationMatrixLocation;
    public final int uColorLocation;
    public final int uCameraPositionLocation;


    public CradleShaderProgram(Context context) {
        super(context, R.raw.vertex_shader, R.raw.fragment_shader);

        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aNormalLocation = glGetAttribLocation(programID, A_NORMAL);
        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uModelRotationMatrixLocation = glGetUniformLocation(programID, U_MODEL_ROTATION_MATRIX);
        uColorLocation = glGetUniformLocation(programID, U_COLOR);
        uCameraPositionLocation = glGetUniformLocation(programID, U_CAMERA_POSITION);
    }
}
