package com.github.fabmax.lightgl;

import android.opengl.Matrix;

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
    }

    /**
     * Updates left and right clip dimensions to keep the mAspect ratio.
     * 
     * @see Camera#setViewport(float, float)
     */
    @Override
    public void setViewport(float width, float height) {
        super.setViewport(width, height);
        float xCenter = mLeft + (mRight - mLeft) / 2.0f;
        float newW = (mTop - mBottom) * mAspect / 2.0f;
        mLeft = xCenter - newW;
        mRight = xCenter + newW;
    }

    /**
     * Computes an orthographic projection matrix.
     * 
     * @see Camera#getProjectionMatrix(float[])
     */
    @Override
    public void getProjectionMatrix(float[] projMBuf) {
        Matrix.orthoM(projMBuf, 0, mLeft, mRight, mBottom, mTop, mNear, mFar);
    }

}
