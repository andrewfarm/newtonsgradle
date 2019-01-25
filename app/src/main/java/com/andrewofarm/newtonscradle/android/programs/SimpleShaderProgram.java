package com.andrewofarm.newtonscradle.android.programs;

import android.content.Context;

import com.andrewofarm.newtonscradle.R;

import static android.opengl.GLES20.*;

/**
 * Created by Andrew on 1/12/17.
 */

public class SimpleShaderProgram extends ShaderProgram {

    private static final String A_POSITION = "a_Position";
    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_COLOR = "u_Color";

    public final int aPositionLocation;
    public final int uMvpMatrixLocation;
    public final int uColorLocation;


    public SimpleShaderProgram(Context context) {
        super(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader);

        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uColorLocation = glGetUniformLocation(programID, U_COLOR);
    }

    public void setMvpMatrix(float[] matrix) {
        glUniformMatrix4fv(uMvpMatrixLocation, 1, false, matrix, 0);
    }

    public void setColor(float red, float green, float blue) {
        glUniform3f(uColorLocation, red, green, blue);
    }
}
