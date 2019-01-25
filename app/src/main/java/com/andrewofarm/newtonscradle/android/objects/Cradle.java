package com.andrewofarm.newtonscradle.android.objects;

import com.andrewofarm.newtonscradle.android.util.Vector3f;

/**
 * Created by Andrew on 1/12/17.
 */

public class Cradle {

    public final float FRAME_TOP;
    public final float BALL_Y_POSITION;
    public final float SUPPORT_LENGTH;

    // The ball geometry to be drawn for each ball
    public final Ball ball;

    // The frame geometry
    public final Frame frame;

    // The support geometry
    public final Support support;

    public Cradle(Vector3f frameDimen, float ballDiam) {
        ball = new Ball(ballDiam / 2);
        frame = new Frame(frameDimen.x, frameDimen.y, frameDimen.z);
        FRAME_TOP = frameDimen.y / 2;
        BALL_Y_POSITION = -frameDimen.y / 4;
        support = new Support(
                new Vector3f(0f, frameDimen.y / 2, frameDimen.z / 2),
                new Vector3f(0f, BALL_Y_POSITION, 0f),
                new Vector3f(0f, frameDimen.y / 2, -frameDimen.z / 2));
        SUPPORT_LENGTH = FRAME_TOP - BALL_Y_POSITION;
    }
}
