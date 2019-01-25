package com.andrewofarm.newtonscradle.android.util;

/**
 * Created by Andrew on 12/28/16.
 */

public class MatrixHelper {

    public static void perspectiveM(float[] m, int offset, float yFovInDegrees, float aspect,
        float n, float f) {

        final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);
        final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));

        m[offset + 0] = a / aspect;
        m[offset + 1] = 0;
        m[offset + 2] = 0;
        m[offset + 3] = 0;

        m[offset + 4] = 0;
        m[offset + 5] = a;
        m[offset + 6] = 0;
        m[offset + 7] = 0;

        m[offset + 8] = 0;
        m[offset + 9] = 0;
        m[offset + 10] = -((f + n) / (f - n));
        m[offset + 11] = -1f;

        m[offset + 12] = 0;
        m[offset + 13] = 0;
        m[offset + 14] = -((2f * f * n) / (f - n));
        m[offset + 15] = 0;
    }
}
