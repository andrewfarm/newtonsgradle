package com.andrewofarm.newtonscradle;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

import com.andrewofarm.newtonscradle.android.objects.Ball;
import com.andrewofarm.newtonscradle.android.objects.Cradle;
import com.andrewofarm.newtonscradle.android.objects.Skybox;
import com.andrewofarm.newtonscradle.android.programs.CradleShaderProgram;
import com.andrewofarm.newtonscradle.android.programs.MetalShaderProgram;
import com.andrewofarm.newtonscradle.android.programs.SimpleShaderProgram;
import com.andrewofarm.newtonscradle.android.programs.SkyboxShaderProgram;
import com.andrewofarm.newtonscradle.android.util.MatrixHelper;
import com.andrewofarm.newtonscradle.android.util.Ray;
import com.andrewofarm.newtonscradle.android.util.TextureHelper;
import com.andrewofarm.newtonscradle.android.util.Vector3f;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static com.andrewofarm.newtonscradle.android.Constants.HALF_PI;

/**
 * Created by Andrew on 1/11/17.
 */

public class CradleRenderer implements Renderer {

    private static final int BALL_COUNT = 5;
    private static final float BALL_DIAMETER = 1f;
    private static final float BALL_RADIUS = BALL_DIAMETER / 2;

    private final Context context;

    private SimpleShaderProgram simpleShader;
    private CradleShaderProgram shaderProgram;
    private MetalShaderProgram metalShader;
    private SkyboxShaderProgram skyboxShader;

    private Skybox skybox;
    private int cubeMapTexture;

    private final float[] modelRotationMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    private final float[] vpRotationMatrix = new float[16];
    private final float[] inverseViewProjectionMatrix = new float[16];

    private static final float CAM_TOUCH_SENSITIVITY = 100.0f;
    private float camAngle = 0, camElevation = 0; //degrees
    private Vector3f camPos = new Vector3f(0f, 0f, 12f);
    private Vector3f lookAtPos = new Vector3f(0f, 0f, 0f);

    private Cradle cradle;

    private final float[] ballXCoords;
    private final Vector3f[] ballPositions;

    private static final float SPEED = 0.135f;
    private boolean[] active = new boolean[BALL_COUNT];
    private int activeBalls = 0;
    private double periodPosition;
    private double ballAngle;
    private double previousBallAngle;
    private double maxSwingAngle;

    private boolean dragging = false, swinging = false;
    private int draggedBall;

    private float previousX, previousY;

    private MediaPlayer player;

    public CradleRenderer(Context context) {
        this.context = context;

        skybox = new Skybox();
        cradle = new Cradle(new Vector3f(6f, 6f, 5f), BALL_DIAMETER);

        ballXCoords = new float[] {-2f, -1f, 0f, 1f, 2f};
        ballPositions = new Vector3f[ballXCoords.length];
        for (int i = 0; i < ballXCoords.length; i++) {
            ballPositions[i] = new Vector3f(ballXCoords[i], cradle.BALL_Y_POSITION, 0f);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0f, 0f, 0f, 0f);

        simpleShader = new SimpleShaderProgram(context);
        shaderProgram = new CradleShaderProgram(context);
        metalShader = new MetalShaderProgram(context);
        skyboxShader = new SkyboxShaderProgram(context);

        cubeMapTexture = TextureHelper.loadCubeMap(context,
                new int[] {
                        R.drawable.room_left, R.drawable.room_right,
                        R.drawable.room_bottom, R.drawable.room_top,
                        R.drawable.room_front, R.drawable.room_back});
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        Matrix.setIdentityM(modelMatrix, 0);
        updateViewMatrix();
        MatrixHelper.perspectiveM(projectionMatrix, 0,
                60, (float) width / (float) height,
                1f, 50f);
        updateMVPMatrix();
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        step();
        render();
    }

    private void step() {
        if (swinging) {
            periodPosition += SPEED;
            periodPosition %= 2 * Math.PI;
            ballAngle = maxSwingAngle * Math.cos(periodPosition);
            maxSwingAngle *= 0.999;
            for (int i = 0; i < BALL_COUNT; i++) {
                if (active[i]) {
                    ballPositions[i].set(
                            (float) (cradle.SUPPORT_LENGTH * Math.sin(ballAngle)),
                            (float) (cradle.SUPPORT_LENGTH * -Math.cos(ballAngle)),
                            0f);
                    ballPositions[i].add(ballXCoords[i], cradle.FRAME_TOP, 0f);
                }
            }

            //collide balls
            if (Math.signum(ballAngle) != Math.signum(previousBallAngle)) {
                for (int i = 0; i < BALL_COUNT / 2; i++) {
                    //swap active states
                    int swapIndex = BALL_COUNT - 1 - i;
                    boolean temp = active[i];
                    active[i] = active[swapIndex];
                    active[swapIndex] = temp;
                }

                if (activeBalls < BALL_COUNT) {
                    //lose kinetic energy
                    maxSwingAngle *= 0.95;
                    //play sound effect
                    playSound(R.raw.click, (float) Math.abs(maxSwingAngle / HALF_PI));
                }
            }

            //correct numerical imprecision by resetting the non-active balls
            for (int i = 0; i < BALL_COUNT; i++) {
                if (!active[i]) {
                    ballPositions[i].set(ballXCoords[i], cradle.BALL_Y_POSITION, 0f);
                }
            }

            previousBallAngle = ballAngle;
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        float[] rotationMatrix = new float[16];
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, camAngle, 0, 1, 0);
        Matrix.rotateM(rotationMatrix, 0, camElevation, 1, 0, 0);
        float[] rotatedCamCoords = new float[4];
        Matrix.multiplyMV(rotatedCamCoords, 0, rotationMatrix, 0,
                new float[] {0f, 0f, 12f, 0f}, 0);
        camPos.set(rotatedCamCoords);
        updateViewMatrix();

        Matrix.setIdentityM(modelMatrix, 0);

        drawSkybox();
        drawCradle();
    }

    private void drawSkybox() {
        updateVPRotationMatrix();
        skyboxShader.useProgram();
        skyboxShader.setUniforms(vpRotationMatrix, cubeMapTexture);
        skybox.bindData(skyboxShader);
        skybox.draw();
    }

    private void drawCradle() {
        metalShader.useProgram();

        metalShader.setColor(MetalShaderProgram.STEEL);
        metalShader.setCameraPosition(camPos);
        metalShader.setCubeMapUnit(cubeMapTexture);
        updateMVPMatrix();
        metalShader.setMvpMatrix(mvpMatrix);
        updateModelRotationMatrix();
        metalShader.setModelRotationMatrix(modelRotationMatrix);

        //draw frame
        cradle.frame.bindData(shaderProgram);
        cradle.frame.draw();

        //draw balls
        cradle.ball.bindData(metalShader);
//        float translateX = -(BALL_COUNT + 1) * BALL_DIAMETER / 2f;
//        Matrix.translateM(modelMatrix, 0, translateX, cradle.BALL_Y_POSITION, 0f);
//        for (int i = 0; i < BALL_COUNT; i++) {
//            Matrix.translateM(modelMatrix, 0, BALL_DIAMETER, 0f, 0f);
//            updateMVPMatrix();
//            metalShader.setMvpMatrix(mvpMatrix);
//            cradle.ball.draw();
//        }
        for (int i = 0; i < BALL_COUNT; i++) {
            Vector3f ballPos = ballPositions[i];
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, ballPos.x, ballPos.y, ballPos.z);
            updateMVPMatrix();
            metalShader.setMvpMatrix(mvpMatrix);
            cradle.ball.draw();
        }

        //draw supports
        simpleShader.useProgram();
        simpleShader.setColor(0f, 0f, 0f);
        cradle.support.bindData(simpleShader);
//        translateX = -BALL_COUNT * BALL_DIAMETER;
//        Matrix.translateM(modelMatrix, 0, translateX, -cradle.BALL_Y_POSITION, 0f);
//        for (int i = 0; i < BALL_COUNT; i++) {
//            Matrix.translateM(modelMatrix, 0, BALL_DIAMETER, 0f, 0f);
//            updateMVPMatrix();
//            simpleShader.setMvpMatrix(mvpMatrix);
//            cradle.support.draw();
//        }
        for (int i = 0; i < BALL_COUNT; i++) {
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, ballXCoords[i], 0f, 0f);
            if (active[i]) {
                Matrix.translateM(modelMatrix, 0, 0f, cradle.FRAME_TOP, 0f);
                Matrix.rotateM(modelMatrix, 0, (float) Math.toDegrees(ballAngle), 0, 0, 1);
                Matrix.translateM(modelMatrix, 0, 0f, -cradle.FRAME_TOP, 0f);
            }
            updateMVPMatrix();
            simpleShader.setMvpMatrix(mvpMatrix);
            cradle.support.draw();
        }
    }

    private void updateViewMatrix() {
        Matrix.setLookAtM(viewMatrix, 0,
                camPos.x, camPos.y, camPos.z,
                lookAtPos.x, lookAtPos.y, lookAtPos.z,
                0f, 1f, 0f);
    }

    private void updateMVPMatrix() {
        //also updates inverseViewProjectionMatrix.

        //calculate MVP matrix
        float[] viewProjectionMatrix = new float[16];
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);

        //calculate inverse VP matrix
        Matrix.invertM(inverseViewProjectionMatrix, 0, viewProjectionMatrix, 0);
    }

    private void updateModelRotationMatrix() {
        System.arraycopy(modelMatrix, 0, modelRotationMatrix, 0, modelMatrix.length);
        modelRotationMatrix[12] = 0;
        modelRotationMatrix[13] = 0;
        modelRotationMatrix[14] = 0;
    }

    private void updateVPRotationMatrix() {
        float[] viewRotationMatrix = new float[16];
        System.arraycopy(viewMatrix, 0, viewRotationMatrix, 0, viewMatrix.length);
        viewRotationMatrix[12] = 0;
        viewRotationMatrix[13] = 0;
        viewRotationMatrix[14] = 0;
        Matrix.multiplyMM(vpRotationMatrix, 0, projectionMatrix, 0, viewRotationMatrix, 0);

    }

    public void handleTouchDown(float normalizedX, float normalizedY) {
        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

        int intersectedBall = getIntersectedBallIndex(ray);
        if (intersectedBall >= 0) {
            draggedBall = intersectedBall;
            swinging = false;
            dragging = true;
        } else {
            previousX = normalizedX;
            previousY = normalizedY;
        }
    }

    public void handleTouchMove(float normalizedX, float normalizedY) {
        if (dragging) {
            Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            Ray plane = new Ray(new Vector3f(), new Vector3f(0f, 0f, 1f));
            Vector3f intersectionPoint = getIntersectionPoint(ray, plane);
            intersectionPoint.sub(ballXCoords[draggedBall], cradle.FRAME_TOP, intersectionPoint.z);
            ballAngle = Math.atan2(intersectionPoint.y, intersectionPoint.x) + HALF_PI;
            if (ballAngle > Math.PI) ballAngle -= 2 * Math.PI;
            //clamp ballAngle
            ballAngle = Math.min(Math.max(ballAngle, -HALF_PI), HALF_PI);
            intersectionPoint.set(
                    cradle.SUPPORT_LENGTH * (float) Math.sin(ballAngle),
                    cradle.SUPPORT_LENGTH * (float) -Math.cos(ballAngle),
                    0f);

            //reset ball statuses
            for (int i = 0; i < BALL_COUNT; i++) {
                active[i] = false;
                ballPositions[i].set(ballXCoords[i], cradle.BALL_Y_POSITION, 0f);
            }
            activeBalls = 0;

            //find affected balls
            int direction = ballAngle < 0 ? -1 : 1;
            for (int i = draggedBall; (i >= 0) && (i < BALL_COUNT); i += direction) {
                active[i] = true;
                activeBalls++;
                ballPositions[i].set(intersectionPoint);
                ballPositions[i].add(ballXCoords[i], cradle.FRAME_TOP, 0f);
            }
        } else {
            float dx = normalizedX - previousX;
            float dy = normalizedY - previousY;
            camAngle -= dx * CAM_TOUCH_SENSITIVITY;
            camAngle %= 360;
            camElevation += dy * CAM_TOUCH_SENSITIVITY;
            camElevation = Math.min(Math.max(camElevation, -89.99f), 89.99f);
            previousX = normalizedX;
            previousY = normalizedY;
        }
    }

    public void handleTouchUp() {
        if (dragging) {
            swinging = true;
            periodPosition = 0;
            maxSwingAngle = previousBallAngle = ballAngle;
        }
        dragging = false;
    }

    private int getIntersectedBallIndex(Ray ray) {
        int startIndex, direction;
        if (camPos.x < 0) {
            startIndex = 0;
            direction = 1;
        } else {
            startIndex = BALL_COUNT - 1;
            direction = -1;
        }

        for (int i = 0, ballIndex = startIndex; i < BALL_COUNT; i++, ballIndex += direction) {
            if (intersects(ray, ballPositions[ballIndex], BALL_RADIUS)) {
                return ballIndex;
            }
        }
        return -1;
    }

    private Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
        //pick points on near and far plane (NDC)
        final float[] nearPointNdc = {normalizedX, normalizedY, -1f, 1f};
        final float[] farPointNdc = {normalizedX, normalizedY, 1f, 1f};

        //convert to world space
        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];
        Matrix.multiplyMV(nearPointWorld, 0, inverseViewProjectionMatrix, 0, nearPointNdc, 0);
        Matrix.multiplyMV(farPointWorld, 0, inverseViewProjectionMatrix, 0, farPointNdc, 0);
        
        //undo perspective divide
        nearPointWorld[0] /= nearPointWorld[3];
        nearPointWorld[1] /= nearPointWorld[3];
        nearPointWorld[2] /= nearPointWorld[3];
        farPointWorld[0] /= farPointWorld[3];
        farPointWorld[1] /= farPointWorld[3];
        farPointWorld[2] /= farPointWorld[3];

        //construct the ray
        Vector3f rayPoint = new Vector3f(nearPointWorld);
        Vector3f rayVector = new Vector3f(farPointWorld);
        rayVector.sub(nearPointWorld);
        return new Ray(rayPoint, rayVector);
    }

    private boolean intersects(Ray ray, Vector3f sphereCenter, float sphereRadius) {
        return distanceBetween(sphereCenter, ray) < sphereRadius;
    }

    private float distanceBetween(Vector3f point, Ray ray) {
        Vector3f p1ToPoint = new Vector3f(point);
        p1ToPoint.sub(ray.point);

        Vector3f p2ToPoint = new Vector3f(p1ToPoint);
        p2ToPoint.sub(ray.vector);

        Vector3f crossProduct = new Vector3f(p1ToPoint);
        crossProduct.cross(p2ToPoint);
        float areaOfTriangleTimesTwo = crossProduct.length();
        float lengthOfBase = ray.vector.length();

        float distanceFromPointToRay = areaOfTriangleTimesTwo / lengthOfBase;
        return distanceFromPointToRay;
    }

    /**
     * Calculates the intersection point between a ray and a plane.
     * @param ray - the ray
     * @param plane - the plane, as represented by a {@code Ray} object:
     *              the normal is represented by the ray's {@code vector} attribute.
     * @return a {@code Vector3f} representing the intersection point.
     */
    private Vector3f getIntersectionPoint (Ray ray, Ray plane) {
        Vector3f rayToPlane = new Vector3f(plane.point);
        rayToPlane.sub(ray.point);

        float scaleFactor = rayToPlane.dot(plane.vector)
                / ray.vector.dot(plane.vector);

        Vector3f intersectionPoint = new Vector3f(ray.vector
        );
        intersectionPoint.scale(scaleFactor);
        intersectionPoint.add(ray.point);
        return intersectionPoint;
    }

    private void playSound(int id, float volume) {
        if (player != null) {
            player.stop();
            player.release();
        }
        player = MediaPlayer.create(context, id);
        if (player != null) {
            player.setVolume(volume, volume);
            player.start();
        }
    }
}
