package com.github.fabmax.lightgl;

import com.github.fabmax.lightgl.util.GlMath;

/**
 * A standard perspective camera. This is basically just a wrapper around
 * {@link GlMath#perspectiveM(float[], float, float, float, float)}
 * 
 * @author fabmax
 * 
 */
public class PerspectiveCamera extends Camera {

    private float mFovy = 60.0f;
    private float mNear = 0.1f;
    private float mFar = 100.0f;
    private float mAspect = 1.0f;
    
    /**
     * @see com.github.fabmax.lightgl.Camera#setup(com.github.fabmax.lightgl.GfxState)
     */
    @Override
    public void setup(GfxState state) {
        mAspect = state.getAspectRatio();
        super.setup(state);
    }

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
        // recompute camera matrices on next setup()
        setDirty();
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
        // recompute camera matrices on next setup()
        setDirty();
    }

    /**
     * @see Camera#computeProjectionMatrix(float[])
     */
    @Override
    public void computeProjectionMatrix(float[] projMBuf) {
        GlMath.perspectiveM(projMBuf, mFovy, mAspect, mNear, mFar);
    }

}
