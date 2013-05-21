package com.github.fabmax.lightgl;

import android.opengl.Matrix;

/**
 * Base class for arbitrary camera implementations.
 * 
 * @author fabmax
 * 
 */
public abstract class Camera {

    /** Camera position */
    protected float mEyeX = 0, mEyeY = 0, mEyeZ = 10;

    /** Camera look at position */
    protected float mLookAtX = 0, mLookAtY = 0, mLookAtZ = 0;

    /** Camera up direction */
    protected float mUpX = 0, mUpY = 1, mUpZ = 0;
    
    /** Viewport dimensions */
    protected float mViewportW, mViewportH, aspect;

    /**
     * Sets the camera position.
     */
    public void setPosition(float x, float y, float z) {
        mEyeX = x;
        mEyeY = y;
        mEyeZ = z;
    }

    /**
     * Sets the position the camera looks at.
     */
    public void setLookAt(float x, float y, float z) {
        mLookAtX = x;
        mLookAtY = y;
        mLookAtZ = z;
    }

    /**
     * Sets the camera up direction.
     */
    public void setUpDirection(float x, float y, float z) {
        mUpX = x;
        mUpY = y;
        mUpZ = z;
    }
    
    /**
     * Sets the viewport dimensions.
     */
    protected void setViewport(float width, float height) {
        mViewportW = width;
        mViewportH = height;
        if (height > 0) {
            aspect = width / height;
        } else {
            aspect = 1;
        }
    }

    /**
     * Computes the view matrix for this camera. This method is called by GfxEngine for the active
     * camera every time before a frame is rendered. The default implementation calls
     * {@link Matrix#setLookAtM(float[], int, float, float, float, float, float, float, float, float, float)}
     * with the parameters for this camera.
     * 
     * @param viewMBuf
     *            16 element array where the view matrix is stored in
     */
    public void getViewMatrix(float[] viewMBuf) {
        // compute standard view matrix
        Matrix.setLookAtM(viewMBuf, 0, mEyeX, mEyeY, mEyeZ, mLookAtX, mLookAtY, mLookAtZ, mUpX, mUpY, mUpZ);
    }
    
    /**
     * Computes the projection matrix for this camera. This method is called by GfxEngine for the
     * active camera every time before a frame is rendered.
     * 
     * @param projMBuf
     *            16 element array where the projection matrix is stored in
     */
    public abstract void getProjectionMatrix(float[] projMBuf);
}
