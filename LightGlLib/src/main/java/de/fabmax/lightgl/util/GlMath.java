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
    public static final float PI = 3.14159265f;

    /**
     * Scales the specified vector to unit length.
     * 
     * @param vec3  The vector to scale
     * @param off   Array offset of the vector
     */
    public static void normalize(float[] vec3, int off) {
        float s = 1 / (float) Math.sqrt(vec3[off] * vec3[off] + vec3[off+1] * vec3[off+1] + vec3[off+2] * vec3[off+2]);
        vec3[off]   *= s;
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
        gluUnProject(x, yInv, 0.0f, viewMatrix, 0, projMatrix, 0, viewport, 0, result.origin, 0);
        gluUnProject(x, yInv, 1.0f, viewMatrix, 0, projMatrix, 0, viewport, 0, result.direction, 0);
        
        // only took me a hour to figure out that the Android gluUnProject version does not divide
        // the resulting coordinates by w...
        float s = 1.0f / result.origin[3];
        result.origin[0] *= s;
        result.origin[1] *= s;
        result.origin[2] *= s;
        
        s = 1.0f / result.direction[3];
        result.direction[0] *= s;
        result.direction[1] *= s;
        result.direction[2] *= s;

        result.direction[0] -= result.origin[0];
        result.direction[1] -= result.origin[1];
        result.direction[2] -= result.origin[2];

        float len = (float) Math.sqrt(
                result.direction[0] * result.direction[0] +
                result.direction[1] * result.direction[1] +
                result.direction[2] * result.direction[2]);
        result.direction[0] /= len;
        result.direction[1] /= len;
        result.direction[2] /= len;
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

    /**
     * Map window coordinates to object coordinates. gluUnProject maps the
     * specified window coordinates into object coordinates using model, proj,
     * and view. The result is stored in obj.
     * <p>
     * Note that you can use the OES_matrix_get extension, if present, to get
     * the current modelView and projection matrices.
     * <p>
     * This method is taken from the Android framework as it uses float arrays
     * instead of FloatBuffers
     *
     * @param winX window coordinates X
     * @param winY window coordinates Y
     * @param winZ window coordinates Z
     * @param model the current modelview matrix
     * @param modelOffset the offset into the model array where the modelview
     *        maxtrix data starts.
     * @param project the current projection matrix
     * @param projectOffset the offset into the project array where the project
     *        matrix data starts.
     * @param view the current view, {x, y, width, height}
     * @param viewOffset the offset into the view array where the view vector
     *        data starts.
     * @param obj the output vector {objX, objY, objZ}, that returns the
     *        computed object coordinates.
     * @param objOffset the offset into the obj array where the obj vector data
     *        starts.
     * @return A return value of true indicates success, a return value
     *         of false indicates failure.
     */
    public static boolean gluUnProject(float winX, float winY, float winZ,
                                       float[] model, int modelOffset, float[] project, int projectOffset,
                                       int[] view, int viewOffset, float[] obj, int objOffset) {
        float[] scratch = sScratch;
        synchronized(sScratch) {
            final int PM_OFFSET = 0; // 0..15
            final int INVPM_OFFSET = 16; // 16..31
            final int V_OFFSET = 0; // 0..3 Reuses PM_OFFSET space
            Matrix.multiplyMM(scratch, PM_OFFSET, project, projectOffset,
                    model, modelOffset);

            if (!Matrix.invertM(scratch, INVPM_OFFSET, scratch, PM_OFFSET)) {
                return false;
            }

            scratch[V_OFFSET + 0] =
                    2.0f * (winX - view[viewOffset + 0]) / view[viewOffset + 2]
                            - 1.0f;
            scratch[V_OFFSET + 1] =
                    2.0f * (winY - view[viewOffset + 1]) / view[viewOffset + 3]
                            - 1.0f;
            scratch[V_OFFSET + 2] = 2.0f * winZ - 1.0f;
            scratch[V_OFFSET + 3] = 1.0f;

            Matrix.multiplyMV(obj, objOffset, scratch, INVPM_OFFSET, scratch, V_OFFSET);
        }

        return true;
    }

    private static final float[] sScratch = new float[32];

    public static void multiplyMV(float[] lhsMat, int lhsMatOffset,
                                  float[] rhsVec, int rhsVecOffset) {

        float x = lhsMat[lhsMatOffset] * rhsVec[rhsVecOffset] +
                lhsMat[lhsMatOffset +  4] * rhsVec[rhsVecOffset + 1] +
                lhsMat[lhsMatOffset +  8] * rhsVec[rhsVecOffset + 2] +
                lhsMat[lhsMatOffset + 12] * rhsVec[rhsVecOffset + 3];
        float y = lhsMat[lhsMatOffset +  1] * rhsVec[rhsVecOffset] +
                lhsMat[lhsMatOffset +  5] * rhsVec[rhsVecOffset + 1] +
                lhsMat[lhsMatOffset +  9] * rhsVec[rhsVecOffset + 2] +
                lhsMat[lhsMatOffset + 13] * rhsVec[rhsVecOffset + 3];
        float z = lhsMat[lhsMatOffset +  2] * rhsVec[rhsVecOffset] +
                lhsMat[lhsMatOffset +  6] * rhsVec[rhsVecOffset + 1] +
                lhsMat[lhsMatOffset + 10] * rhsVec[rhsVecOffset + 2] +
                lhsMat[lhsMatOffset + 14] * rhsVec[rhsVecOffset + 3];
        float w = lhsMat[lhsMatOffset +  3] * rhsVec[rhsVecOffset] +
                lhsMat[lhsMatOffset +  7] * rhsVec[rhsVecOffset + 1] +
                lhsMat[lhsMatOffset + 11] * rhsVec[rhsVecOffset + 2] +
                lhsMat[lhsMatOffset + 15] * rhsVec[rhsVecOffset + 3];

        rhsVec[rhsVecOffset]     = x;
        rhsVec[rhsVecOffset + 1] = y;
        rhsVec[rhsVecOffset + 2] = z;
        rhsVec[rhsVecOffset + 3] = w;
    }
}