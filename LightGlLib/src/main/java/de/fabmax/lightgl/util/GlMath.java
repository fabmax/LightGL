package de.fabmax.lightgl.util;

import android.opengl.GLU;
import android.opengl.Matrix;

import de.fabmax.lightgl.Ray;

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
     * @param vec3  The vector to scale
     * @param off   Array offset of the vector
     */
    public static void normalize(float[] vec3, int off) {
        float s = 1 / (float) Math.sqrt(vec3[off+0] * vec3[off+0] + vec3[off+1] * vec3[off+1] + vec3[off+2] * vec3[off+2]);
        vec3[off+0] *= s;
        vec3[off+1] *= s;
        vec3[off+2] *= s;
    }

    /**
     * Transform the specified vector in place according to the given 4x4 transform matrix.
     *
     * @param vec3    Vector to transform, result will override initial value
     * @param vOff    Vector array offset
     * @param w       Vector w-component, 0 to only consider rotation, 1 for translation as well
     * @param m44     4x4 transform matrix
     * @param mOff    Matrix array offset
     */
    public static void transformVector(float[] vec3, int vOff, float w, float[] m44, int mOff) {
        float x = m44[mOff] * vec3[vOff] + m44[mOff + 4] * vec3[vOff + 1] + m44[mOff + 8] * vec3[vOff + 2] + m44[mOff + 12] * w;
        float y = m44[mOff + 1] * vec3[vOff] + m44[mOff + 5] * vec3[vOff + 1] + m44[mOff + 9] * vec3[vOff + 2] + m44[mOff + 13] * w;
        float z = m44[mOff + 2] * vec3[vOff] + m44[mOff + 6] * vec3[vOff + 1] + m44[mOff + 10] * vec3[vOff + 2] + m44[mOff + 14] * w;
        vec3[vOff] = x;
        vec3[vOff + 1] = y;
        vec3[vOff + 2] = z;
    }

    /**
     * Subtracts two vectors and stores the result as a third vector: result = lhs - rhs. All
     * vectors can bestored in the same array with different offsets.
     */
    public static void subtractVector(float[] result, int resOff, float[] lhs, int lhsOff, float[] rhs, int rhsOff) {
        result[resOff] = lhs[lhsOff] - rhs[rhsOff];
        result[resOff + 1] = lhs[lhsOff + 1] - rhs[rhsOff + 1];
        result[resOff + 2] = lhs[lhsOff + 2] - rhs[rhsOff + 2];
    }

    /**
     * Computes the cross product of two vectors ans stores the result as a third vector:
     * result = lhs x rhs. All vectors can bestored in the same array with different offsets.
     */
    public static void crossVector(float[] result, int resOff, float[] lhs, int lhsOff, float[] rhs, int rhsOff) {
        result[resOff] = lhs[lhsOff + 1] * rhs[rhsOff + 2] - lhs[lhsOff + 2] * rhs[rhsOff + 1];
        result[resOff + 1] = lhs[lhsOff + 2] * rhs[rhsOff] - lhs[lhsOff] * rhs[rhsOff + 2];
        result[resOff + 2] = lhs[lhsOff] * rhs[rhsOff + 1] - lhs[lhsOff + 1] * rhs[rhsOff];
    }

    /**
     * Computes the dot product of two vectors.
     */
    public static float dotVector(float[] lhs, int lhsOff, float[] rhs, int rhsOff) {
        return lhs[lhsOff] * rhs[rhsOff] + lhs[lhsOff + 1] * rhs[rhsOff + 1] + lhs[lhsOff + 2] * rhs[rhsOff + 2];
    }

    /**
     * Returns the squared distance between the two 3-component vectors.
     */
    public static float distanceSqr(float[] lhs, int lhsOff, float[] rhs, int rhsOff) {
        float dx = lhs[lhsOff]     - rhs[rhsOff];
        float dy = lhs[lhsOff + 1] - rhs[rhsOff + 1];
        float dz = lhs[lhsOff + 2] - rhs[rhsOff + 2];
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Returns the distance between the two 3-component vectors.
     */
    public static float distance(float[] lhs, int lhsOff, float[] rhs, int rhsOff) {
        return (float) Math.sqrt(distanceSqr(lhs, lhsOff, rhs, rhsOff));
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
     * @param aspect
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

    /**
     * Computes a {@link de.fabmax.lightgl.Ray} that corresponds to the camera's view direction at
     * the specified screen coordinate in pixels.
     *
     * @see de.fabmax.lightgl.Camera#getPickRay(int[], float, float, de.fabmax.lightgl.Ray)
     *
     * @param viewport         Viewport matrix as returned by
     *                         {@link de.fabmax.lightgl.GfxState#getViewport()}
     * @param viewMatrix       View matrix as returned by
     *                         {@link de.fabmax.lightgl.GfxState#getViewMatrix()}
     * @param projMatrix       Projection matrix as returned by
     *                         {@link de.fabmax.lightgl.GfxState#getProjectionMatrix()}
     * @param x                Screen x-coordinate
     * @param y                Screen y-coordinate
     * @param result           Ray used to store the result
     */
    public static void computePickRay(int[] viewport, float[] viewMatrix, float[] projMatrix,
                                      float x, float y, Ray result) {
        float yInv = viewport[3] - y;
        GLU.gluUnProject(x, yInv, 0.0f, viewMatrix, 0, projMatrix, 0, viewport, 0, result.origin, 0);
        GLU.gluUnProject(x, yInv, 1.0f, viewMatrix, 0, projMatrix, 0, viewport, 0, result.direction, 0);
        
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
     * Clamps the value of f to the range [min, max].
     *
     * @param f      value to clamp
     * @param min    minimum bound
     * @param max    maximum bound
     * @return clamped value
     */
    public static float clamp(float f, float min, float max) {
        if (f < min) {
            return min;
        } else if (f > max) {
            return max;
        } else {
            return f;
        }
    }

    /**
     * Returns the maximum value of the specified values.
     *
     * @return maximum value of a, b and c
     */
    public static float max3(float a, float b, float c) {
        if (a > b && a > c) {
            return a;
        } else if (b > a && b > c) {
            return b;
        } else {
            return c;
        }
    }

    /**
     * Returns the maximum value of the specified values.
     *
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
     * Returns the minimum value of the specified values.
     *
     * @return minimum value of a, b and c
     */
    public static float min3(float a, float b, float c) {
        if (a < b && a < c) {
            return a;
        } else if (b < a && b < c) {
            return b;
        } else {
            return c;
        }
    }

    /**
     * Returns the minimum value of the specified values.
     *
     * @return minimum value of a, b and c
     */
    public static int min3(int a, int b, int c) {
        if (a < b && a < c) {
            return a;
        } else if (b < a && b < c) {
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