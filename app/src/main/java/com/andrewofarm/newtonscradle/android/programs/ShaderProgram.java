package com.andrewofarm.newtonscradle.android.programs;

import android.content.Context;

import com.andrewofarm.newtonscradle.android.util.ShaderHelper;
import com.andrewofarm.newtonscradle.android.util.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

/**
 * Created by Andrew on 12/29/16.
 */

public class ShaderProgram {

    //Shader program
    public final int programID;

    protected ShaderProgram(Context context, int vertexShaderResourceID,
                            int fragmentShaderResourceID) {
        //compile the shaders and link the program
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(
                context, vertexShaderResourceID);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(
                context, fragmentShaderResourceID);
        programID = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);
    }

    public void useProgram() {
        //set the current OpenGL shader program to this program
        glUseProgram(programID);
    }
}
