package com.github.fabmax.lightgl;

import com.github.fabmax.lightgl.util.GlMath;

public class PerspectiveCamera extends Camera {

    private float mFovy = 60.0f;
    private float mNear = 0.1f;
    private float mFar = 100.0f;

    /**
     * Returns the field of view for this camera in Y direction.
     * 
     * @return the field of view in Y direction
     */
    public float getFovy() {
        return mFovy;
    }

    /**
     * Sets the field of view for this camera in Y direction.
     * 
     * @param fovy
     *            the field of view to set
     */
    public void setFovy(float fovy) {
        mFovy = fovy;
    }

    /**
     * Sets the near and far clip distances.
     * 
     * @param near
     *            near clip distance
     * @param far
     *            far clip distance
     */
    public void setClipRange(float near, float far) {
        mNear = near;
        mFar = far;
    }

    /**
     * @see Camera#getProjectionMatrix(float[])
     */
    @Override
    public void getProjectionMatrix(float[] projMBuf) {
        GlMath.perspectiveM(projMBuf, mFovy, mAspect, mNear, mFar);
    }

}
