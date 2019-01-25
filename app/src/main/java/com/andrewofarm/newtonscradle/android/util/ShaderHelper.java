package com.andrewofarm.newtonscradle.android.util;

import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * Created by Andrew on 12/27/16.
 */

public class ShaderHelper {

    private static final String TAG = "ShaderHelper";

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectID = glCreateShader(type);

        //check for errors
        if (shaderObjectID == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new shader.");
            }
            return 0;
        }

        //upload and compile source
        glShaderSource(shaderObjectID, shaderCode);
        glCompileShader(shaderObjectID);

        //check that the shader was successfully compiled
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectID, GL_COMPILE_STATUS, compileStatus, 0);
        if (LoggerConfig.ON) {
            //print results
            Log.v(TAG, "Results of compiling source:\n" + compileStatus[0]);
        }
        //if it failed, delete the shader object
        if (compileStatus[0] == 0) {
            glDeleteShader(shaderObjectID);
            if (LoggerConfig.ON) {
                Log.w(TAG, "Compilation of shader failed.");
            }
            return 0;
        }

        return shaderObjectID;
    }

    public static int linkProgram(int vertexShaderID, int fragmentShaderID) {
        //create program
        final int programObjectID = glCreateProgram();

        //check for errors
        if (programObjectID == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new program.");
            }
            return 0;
        }

        //attach shaders
        glAttachShader(programObjectID, vertexShaderID);
        glAttachShader(programObjectID, fragmentShaderID);

        //link shaders together
        glLinkProgram(programObjectID);

        //check that the program was linked successfully
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectID, GL_LINK_STATUS, linkStatus, 0);
//        if (LoggerConfig.ON) {
//            //print results
//            Log.v(TAG, "Results of linking program:\n"
//                + glGetProgramInfoLog(programObjectID));
//        }
        //if it failed, delete the program object
        if (linkStatus[0] == 0) {
            glDeleteProgram(programObjectID);
            if (LoggerConfig.ON) {
                Log.w(TAG, "Linking of program failed.");
            }
            return 0;
        }

        return programObjectID;
    }

    public static boolean validateProgram(int programObjectID) {
        glValidateProgram(programObjectID);

        //check that the program is valid
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectID, GL_VALIDATE_STATUS, validateStatus, 0);
        //print results
        Log.v(TAG, "Results of validating program: " + validateStatus[0]
            + "\nLog: " + glGetProgramInfoLog(programObjectID));

        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program;

        //compile shaders
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        //link them into a shader program
        program = linkProgram(vertexShader, fragmentShader);

        //validate the program
        if (LoggerConfig.ON) {
            validateProgram(program);
        }

        return program;
    }
}
