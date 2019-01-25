package com.andrewofarm.newtonscradle.android.objects;

import android.util.Log;

import com.andrewofarm.newtonscradle.android.util.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.andrewofarm.newtonscradle.android.Constants.HALF_PI;

/**
 * Created by Andrew on 1/11/17.
 */

public abstract class ObjectBuilder {

    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;

    public static void buildSphere(FloatBuffer vertexBuffer, int floatStride, IntBuffer indexBuffer,
                                   Vector3f center, float radius, int meridians, int parallels) {

        //construct vertices

        final int vertexOffset = vertexBuffer.position() / floatStride;

        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();

        //north pole
        position.set(0f, radius, 0f);
        position.add(center);
        normal.set(0f, 1f, 0f);
        putVertex(vertexBuffer, floatStride, position, normal);

        double meridianAngleInterval = 2 * Math.PI / meridians;
        double parallelAngleInterval = Math.PI / (parallels + 1);
        double azimuth, longitude;
        float x, y, z;
        for (int i = 0; i < meridians; i++) {
            longitude = i * meridianAngleInterval;
            float equatorX = radius * (float) Math.cos(longitude);
            float equatorZ = radius * (float) Math.sin(longitude);

            for (int j = 1; j <= parallels; j++) {
                azimuth = (j * parallelAngleInterval);
                float cos = (float) Math.cos(azimuth);
                float sin = (float) Math.sin(azimuth);
                x = equatorX * sin;
                z = equatorZ * sin;
                y = radius * cos;

                //calculate position
                position.set(x, y, z);
                position.add(center);
                //calculate normal
                normal.set(x, y, z);
                normal.normalize();
                //add to vertex buffer
                putVertex(vertexBuffer, floatStride, position, normal);
            }
        }

        //south pole
        position.set(0f, -radius, 0f);
        position.add(center);
        normal.set(0f, -1f, 0f);
        putVertex(vertexBuffer, floatStride, position, normal);

        //order indices

        final int northPoleIndex = vertexOffset;
        final int southPoleIndex = vertexOffset + meridians * parallels + 1;

        int col2;
        int colStartIndex, col2StartIndex;
        for (int col = 0; col < meridians; col++) {
            col2 = (col + 1) % meridians; //the column to the right of the current column

            colStartIndex = 1 + (col * parallels) + vertexOffset;
            col2StartIndex = 1 + (col2 * parallels) + vertexOffset;

            indexBuffer.put(northPoleIndex);
            for (int row = 0; row < parallels; row++) {
                indexBuffer.put(colStartIndex + row);
                indexBuffer.put(col2StartIndex + row);
            }
            indexBuffer.put(southPoleIndex);

            //degenerate vertices
            indexBuffer.put(southPoleIndex);
            indexBuffer.put(northPoleIndex);
        }
    }

    public static void buildTube(FloatBuffer vertexBuffer, int floatStride, IntBuffer indexBuffer,
                                  int axis, Vector3f center, float radius, float length,
                                  int faces) {

        //construct vertices

        final int vertexOffset = vertexBuffer.position() / floatStride;

        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();

        float dist = length / 2;
        double angleInterval = 2 * Math.PI / faces;
        double angle;
        float tubeX, tubeY;
        float x1, y1, z1, x2, y2, z2;
        float normalX, normalY, normalZ;
        for (int i = 0; i < faces; i++) {
            angle = i * angleInterval;
            tubeX = radius * (float) Math.cos(angle);
            tubeY = radius * (float) Math.sin(angle);

            switch (axis) {
                case X_AXIS:
                    x1 = dist;
                    x2 = -dist;
                    normalX = 0;
                    y1 = y2 = normalY = tubeX;
                    z1 = z2 = normalZ = tubeY;
                    break;
                case Y_AXIS:
                    x1 = x2 = normalX = tubeY;
                    y1 = dist;
                    y2 = -dist;
                    normalY = 0;
                    z1 = z2 = normalZ = tubeX;
                    break;
                case Z_AXIS:
                    x1 = x2 = normalX = tubeX;
                    y1 = y2 = normalY = tubeY;
                    z1 = dist;
                    z2 = -dist;
                    normalZ = 0;
                    break;
                default:
                    throw new IllegalArgumentException(axis + " is not a valid axis");
            }

            normal.set(normalX, normalY, normalZ);
            normal.normalize();

            position.set(x1, y1, z1);
            position.add(center);
            putVertex(vertexBuffer, floatStride, position, normal);

            position.set(x2, y2, z2);
            position.add(center);
            putVertex(vertexBuffer, floatStride, position, normal);
        }

        //order indices
        for (int i = 0; i < 2 * faces; i++) {
            indexBuffer.put(vertexOffset + i);
        }
        indexBuffer.put(vertexOffset);
        indexBuffer.put(vertexOffset + 1);
    }

    public static void buildCurvedTube(FloatBuffer vertexBuffer, int floatStride,
                                       IntBuffer indexBuffer,
                                       int axis, Vector3f center, float curveRadius,
                                       double startAngle, double arcLengthRadians,
                                       float tubeRadius, int tubeFaces, int ringFaces) {

        //construct vertices

        final int vertexOffset = vertexBuffer.position() / floatStride;

        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();

        double tubeAngleInterval = 2 * Math.PI / tubeFaces;
        double curveAngleInterval = arcLengthRadians / ringFaces;
        double tubeAngle, curveAngle;
        float x0, y0, z0; //untransformed coordinates
        float x, y, z; //transformed coordinates
        float curveX0, curveY0, curveZ0; //untransformed circle coordinates
        float curveX, curveY, curveZ; //transformed circle coordinates
        for (int i = 0; i < tubeFaces; i++) {
            tubeAngle = i * tubeAngleInterval;
            float tubeX = tubeRadius * (float) Math.cos(tubeAngle) + curveRadius;
            float tubeY = tubeRadius * (float) Math.sin(tubeAngle);
            for (int j = 0; j < ringFaces + 1; j++) {
                curveAngle = j * curveAngleInterval + startAngle;
                float cos = (float) Math.cos(curveAngle);
                float sin = (float) Math.sin(curveAngle);
                x0 = tubeX * cos;
                y0 = tubeY;
                z0 = tubeX * sin;
                curveX0 = curveRadius * cos;
                curveY0 = 0;
                curveZ0 = curveRadius * sin;

                switch (axis) {
                    case X_AXIS:
                        x = y0;
                        y = z0;
                        z = x0;
                        curveX = curveY0;
                        curveY = curveZ0;
                        curveZ = curveX0;
                        break;
                    case Y_AXIS:
                        x = x0;
                        y = y0;
                        z = z0;
                        curveX = curveX0;
                        curveY = curveY0;
                        curveZ = curveZ0;
                        break;
                    case Z_AXIS:
                        x = z0;
                        y = x0;
                        z = y0;
                        curveX = curveZ0;
                        curveY = curveX0;
                        curveZ = curveY0;
                        break;
                    default:
                        throw new IllegalArgumentException(axis + " is not a valid axis");
                }

                position.set(x, y, z);
                position.add(center);

                normal.set(x, y, z);
                normal.sub(curveX, curveY, curveZ);
                normal.normalize();

                putVertex(vertexBuffer, floatStride, position, normal);
            }
        }

        //order indices

        int col2;
        int colStartIndex, col2StartIndex;
        int rings = ringFaces + 1;
        for (int col = 0; col < tubeFaces; col++) {
            col2 = (col + 1) % tubeFaces; //the column next to the current column

            colStartIndex = col * rings + vertexOffset;
            col2StartIndex = col2 * rings + vertexOffset;

            for (int row = 0; row < rings; row++) {
                indexBuffer.put(colStartIndex + row);
                indexBuffer.put(col2StartIndex + row);
            }

            //degenerate vertices
            indexBuffer.put(col2StartIndex + ringFaces);
            indexBuffer.put(col2StartIndex);
        }
    }

    public static void buildFrame(FloatBuffer vertexBuffer, int floatStride, IntBuffer indexBuffer,
                                  Vector3f dimen, float tubeRadius, float cornerRadius,
                                  int tubeFaces, int curveFaces) {

        final float cylClip = 2 * cornerRadius; //the total amount to clip off each cylinder

        float x = dimen.x / 2;
        float y = dimen.y / 2;
        float z = dimen.z / 2;

        int vertexIndex;

        /* **************** EDGE 1 **************** */

        buildTube(vertexBuffer, floatStride, indexBuffer, Y_AXIS,
                new Vector3f(x, 0f, z), tubeRadius, dimen.y - cylClip, tubeFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        buildCurvedTube(vertexBuffer, floatStride, indexBuffer, X_AXIS,
                new Vector3f(x, -y + cornerRadius, z - cornerRadius), cornerRadius, 0d, -HALF_PI,
                tubeRadius, tubeFaces, curveFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        /* **************** EDGE 2 **************** */

        buildTube(vertexBuffer, floatStride, indexBuffer, Z_AXIS,
                new Vector3f(x, -y, 0f), tubeRadius, dimen.z - cylClip, tubeFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        buildCurvedTube(vertexBuffer, floatStride, indexBuffer, X_AXIS,
                new Vector3f(x, -y + cornerRadius, -z + cornerRadius), cornerRadius, 3 * Math.PI / 2, -HALF_PI,
                tubeRadius, tubeFaces, curveFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        /* **************** EDGE 3 **************** */

        buildTube(vertexBuffer, floatStride, indexBuffer, Y_AXIS,
                new Vector3f(x, 0f, -z), tubeRadius, dimen.y - cylClip, tubeFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        buildCurvedTube(vertexBuffer, floatStride, indexBuffer, Z_AXIS,
                new Vector3f(x - cornerRadius, y - cornerRadius, -z), cornerRadius, HALF_PI, -HALF_PI,
                tubeRadius, tubeFaces, curveFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        /* **************** EDGE 4 **************** */

        buildTube(vertexBuffer, floatStride, indexBuffer, X_AXIS,
                new Vector3f(0f, y, -z), tubeRadius, dimen.x - cylClip, tubeFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        buildCurvedTube(vertexBuffer, floatStride, indexBuffer, Z_AXIS,
                new Vector3f(-x + cornerRadius, y - cornerRadius, -z), cornerRadius, 0, -HALF_PI,
                tubeRadius, tubeFaces, curveFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        /* **************** EDGE 5 **************** */

        buildTube(vertexBuffer, floatStride, indexBuffer, Y_AXIS,
                new Vector3f(-x, 0f, -z), tubeRadius, dimen.y - cylClip, tubeFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        buildCurvedTube(vertexBuffer, floatStride, indexBuffer, X_AXIS,
                new Vector3f(-x, -y + cornerRadius, -z + cornerRadius), cornerRadius, Math.PI, HALF_PI,
                tubeRadius, tubeFaces, curveFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        /* **************** EDGE 6 **************** */

        buildTube(vertexBuffer, floatStride, indexBuffer, Z_AXIS,
                new Vector3f(-x, -y, 0f), tubeRadius, dimen.z - cylClip, tubeFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        buildCurvedTube(vertexBuffer, floatStride, indexBuffer, X_AXIS,
                new Vector3f(-x, -y + cornerRadius, z - cornerRadius), cornerRadius, 3 * Math.PI / 2, HALF_PI,
                tubeRadius, tubeFaces, curveFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        /* **************** EDGE 7 **************** */

        buildTube(vertexBuffer, floatStride, indexBuffer, Y_AXIS,
                new Vector3f(-x, 0f, z), tubeRadius, dimen.y - cylClip, tubeFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        buildCurvedTube(vertexBuffer, floatStride, indexBuffer, Z_AXIS,
                new Vector3f(-x + cornerRadius, y - cornerRadius, z), cornerRadius, 3 * Math.PI / 2, HALF_PI,
                tubeRadius, tubeFaces, curveFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        /* **************** EDGE 8 **************** */

        buildTube(vertexBuffer, floatStride, indexBuffer, X_AXIS,
                new Vector3f(0f, y, z), tubeRadius, dimen.x - cylClip, tubeFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        buildCurvedTube(vertexBuffer, floatStride, indexBuffer, Z_AXIS,
                new Vector3f(x - cornerRadius, y - cornerRadius, z), cornerRadius, 0, HALF_PI,
                tubeRadius, tubeFaces, curveFaces);

        addDegenerates(indexBuffer, vertexBuffer, floatStride);

        /* **************** EDGE 4 **************** */

        //build cylindrical tube
        //TODO insert degenerate vertices
        //TODO leave room for next degenerate vertices
        //build curved tube
        //TODO insert degenerate vertices
        //TODO leave room for next degenerate vertices
    }

    private static void addDegenerates(IntBuffer indexBuffer,
                                       FloatBuffer vertexBuffer,
                                       int floatStride) {
        indexBuffer.put(indexBuffer.get(indexBuffer.position() - 1));
        indexBuffer.put(vertexBuffer.position() / floatStride);
    }

    private static void putVertex(FloatBuffer vertexBuffer, int floatStride,
                                  Vector3f position, Vector3f normal) {
        int bufferPos = vertexBuffer.position();

        vertexBuffer.put(position.x);
        vertexBuffer.put(position.y);
        vertexBuffer.put(position.z);
        vertexBuffer.put(normal.x);
        vertexBuffer.put(normal.y);
        vertexBuffer.put(normal.z);

        vertexBuffer.position(bufferPos + floatStride);
    }

    private static void printIntBuffer(IntBuffer intBuffer, int start, int count) {
        for (int i = 0; i < count; i++) {
            Log.i("printIntBuffer", String.valueOf(intBuffer.get(start + i)));
        }
    }

    private static void printVertices(FloatBuffer vertexBuffer, int start, int count, int floatStride) {
        int memoryIndex;
        for (int i = 0; i < count; i++) {
            memoryIndex = (start + i) * floatStride;
            Vector3f vertex = new Vector3f(
                    vertexBuffer.get(memoryIndex),
                    vertexBuffer.get(memoryIndex + 1),
                    vertexBuffer.get(memoryIndex + 2));
            Log.i("printVertices", (start + i) + ": " + vertex);
        }
    }
}
