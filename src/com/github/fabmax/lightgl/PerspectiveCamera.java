package com.github.fabmax.lightgl;

import com.github.fabmax.lightgl.util.GlMath;

public class PerspectiveCamera extends Camera {

    private float fovy = 60;

    /**
     * Returns the field of view for this camera in Y direction.
     * 
     * @return the field of view in Y direction
     */
    public float getFovy() {
        return fovy;
    }

    /**
     * Sets the field of view for this camera in Y direction.
     * 
     * @param fovy
     *            the field of view to set
     */
    public void setFovy(float fovy) {
        this.fovy = fovy;
    }

    /**
     * @see Camera#getProjectionMatrix(float[])
     */
    @Override
    public void getProjectionMatrix(float[] projMBuf) {
        GlMath.perspectiveM(projMBuf, fovy, aspect, 0.1f, 100.0f);
    }

}
