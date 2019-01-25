package com.andrewofarm.newtonscradle.android.util;

/**
 * Created by Andrew on 1/13/17.
 */

public class Ray {
    public final Vector3f point;
    public final Vector3f vector;

    public Ray(Vector3f point, Vector3f vector) {
        this.point = point;
        this.vector = vector;
    }
}
