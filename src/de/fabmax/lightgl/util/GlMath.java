package de.fabmax.lightgl.util;

import de.fabmax.lightgl.Ray;
import android.opengl.GLU;
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
     * Scales the specified vector to unit length.
     * 
     * @param vec3 the vector to scale
     */
    public static void normalize(float[] vec3, int off) {
        float s = 1 / (float) Math.sqrt(vec3[off+0] * vec3[off+0] + vec3[off+1] * vec3[off+1] + vec3[off+2] * vec3[off+2]);
        vec3[off+0] *= s;
        vec3[off+1] *= s;
        vec3[off+2] *= s;
    }
    
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
     * @param mAspect
     *            mAspect ratio
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
    
    public static void computePickRay(int[] viewport, float[] viewMatrix, int viewMatrixOff,
            float[] projMatrix, int projMatrixOff, float x, float y, Ray result) {
        float yInv = viewport[3] - y;
        GLU.gluUnProject(x, yInv, 0.0f, viewMatrix, viewMatrixOff, projMatrix, projMatrixOff, viewport, 0, result.origin, 0);
        GLU.gluUnProject(x, yInv, 1.0f, viewMatrix, viewMatrixOff, projMatrix, projMatrixOff, viewport, 0, result.direction, 0);
        
        // only took me a hour to figure out that the Android gluUnProject version does not divide
        // the resulting coordinates by w...
        float s = 1.0f / result.origin[3];
        result.origin[0] *= s;
        result.origin[1] *= s;
        result.origin[2] *= s;
        result.origin[3] = 1.0f;
        
        s = 1.0f / result.direction[3];
        result.direction[0] *= s;
        result.direction[1] *= s;
        result.direction[2] *= s;
        result.direction[3] = 0.0f;

        result.direction[0] -= result.origin[0];
        result.direction[1] -= result.origin[1];
        result.direction[2] -= result.origin[2];
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
        } else if (b > a && b > c) {
            return b;
        } else {
            return c;
        }
    }

    /**
     * Computes a packed int color from HSV color model values.
     * 
     * @param h
     *            hue [0 .. 360]
     * @param s
     *            saturation [0 .. 1]
     * @param v
     *            value [0 .. 1]
     * @param a
     *            alpha [0 .. 1]
     * @return packed int color
     */
    public static int packedHsvColor(float h, float s, float v, float a) {
        int hi = (int) (h / 60.0f);
        float f = h / 60.0f - hi;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
    
        switch (hi) {
        case 1:
            return packedColor(q, v, p, a);
        case 2:
            return packedColor(p, v, t, a);
        case 3:
            return packedColor(p, q, v, a);
        case 4:
            return packedColor(t, p, v, a);
        case 5:
            return packedColor(v, p, q, a); 
        default:
            return packedColor(v, t, p, a);
        }
    }

    /**
     * Computes a packed int color with the specified color intensities. The packed format is ABGR.
     * The specified values should be in the range from 0 to 1 and are clamped.
     * 
     * @param r
     *            red [0 .. 1]
     * @param g
     *            green [0 .. 1]
     * @param b
     *            blue [0 .. 1]
     * @param a
     *            alpha [0 .. 1]
     * @return packed ABGR int
     */
    public static int packedColor(float r, float g, float b, float a) {
        int ir = (int) (r * 255.0f + 0.5f);
        int ig = (int) (g * 255.0f + 0.5f);
        int ib = (int) (b * 255.0f + 0.5f);
        int ia = (int) (a * 255.0f + 0.5f);

        if (ir > 255) {
            ir = 255;
        } else if (ir < 0) {
            ir = 0;
        }
        if (ig > 255) {
            ig = 255;
        } else if (ig < 0) {
            ig = 0;
        }
        if (ib > 255) {
            ib = 255;
        } else if (ib < 0) {
            ib = 0;
        }
        if (ia > 255) {
            ia = 255;
        } else if (ia < 0) {
            ia = 0;
        }

        return (ia << 24) | (ib << 16) | (ig << 8) | ir;
    }
}