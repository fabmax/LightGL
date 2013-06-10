package de.fabmax.lightgl;

import android.opengl.Matrix;

/**
 * A standard orthographic camera. This is basically just a wrapper around
 * {@link Matrix#orthoM(float[], int, float, float, float, float, float, float)}
 * 
 * @author fabmax
 * 
 */
public class OrthograpicCamera extends Camera {

    private float mLeft = 0f;
    private float mRight = 0f;
    private float mBottom = 0f;
    private float mTop = 0f;
    private float mNear = 0f;
    private float mFar = 100f;
    
    /**
     * Sets the camera clip dimensions.
     */
    public void setClipSize(float left, float right, float bottom, float top, float near, float far) {
        mLeft = left;
        mRight = right;
        mBottom = bottom;
        mTop = top;
        mNear = near;
        mFar = far;
        // recompute camera matrices on next setup()
        setDirty();
    }

    /**
     * Computes an orthographic projection matrix.
     * 
     * @see Camera#computeProjectionMatrix(float[])
     */
    @Override
    public void computeProjectionMatrix(float[] projMBuf) {
        Matrix.orthoM(projMBuf, 0, mLeft, mRight, mBottom, mTop, mNear, mFar);
    }

}
