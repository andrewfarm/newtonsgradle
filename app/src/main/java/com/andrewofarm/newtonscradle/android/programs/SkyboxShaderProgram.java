package com.andrewofarm.newtonscradle.android.programs;

import android.content.Context;

import com.andrewofarm.newtonscradle.R;

import static android.opengl.GLES20.*;

/**
 * Created by Andrew on 1/2/17.
 */

public class SkyboxShaderProgram extends ShaderProgram {

    private static final String U_MATRIX = "u_Matrix";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String A_POSITION = "a_Position";

    public final int uMatrixLocation;
    public final int uTextureUnitLocation;
    public final int aPositionLocation;


    public SkyboxShaderProgram(Context context) {
        super(context, R.raw.skybox_vertex_shader, R.raw.skybox_fragment_shader);

        uMatrixLocation = glGetUniformLocation(programID, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
    }

    public void setUniforms(float[] matrix, int textureID) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        glUniform1i(uTextureUnitLocation, 0);
    }
}
