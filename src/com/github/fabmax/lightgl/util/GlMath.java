package com.github.fabmax.lightgl.util;

import android.opengl.Matrix;

/**
 * Helper class with several GL related math functions.
 * 
 * @author fabmax
 * 
 */
public class GlMath {

    /**
     * PI = 3.14159265f
     */
    public static float PI = 3.14159265f;

    /**
     * Converts an angle from degrees to radians.
     * 
     * @param deg
     *            angle in degrees
     * @return specified angle as radians
     */
    public static float toRadians(float deg) {
        return deg / 180.0f * PI;
    }

    /**
     * Converts an angle from radians to degrees.
     * 
     * @param rad
     *            angle in radians
     * @return specified angle in degrees
     */
    public static float toDegrees(float rad) {
        return rad * 180.0f / PI;
    }

    /**
     * Sets a perspective projection matrix with the specified parameters. The projection matrix is
     * 4x4 hence m must have a size of at least 16 elements.
     * 
     * @param m
     *            Target matrix
     * @param fovy
     *            field of view along the y-axis
     * @param aspect
     *            aspect ratio
     * @param zNear
     *            near clipping distance
     * @param zFar
     *            far clipping distance
     */
    public static void perspectiveM(float[] m, float fovy, float aspect, float zNear, float zFar) {
        Matrix.setIdentityM(m, 0);

        float yScale = (float) (1.0 / Math.tan(Math.toRadians(fovy / 2.0f)));
        float xScale = yScale / aspect;
        float frustumLen = zFar - zNear;

        m[0] = xScale;
        m[5] = yScale;
        m[10] = -((zFar + zNear) / frustumLen);
        m[11] = -1;
        m[14] = -((2 * zNear * zFar) / frustumLen);
        m[15] = 0;
    }
    
    /**
     * Returns the maximum value of the specified values.
     * 
     * @param a
     * @param b
     * @param c
     * @return maximum value of a, b and c
     */
    public static int max3(int a, int b, int c) {
        if (a > b && a > c) {
            return a;
        } else if(b > a && b > c) {
            return b;
        } else {
            return c;
        }
    }
}