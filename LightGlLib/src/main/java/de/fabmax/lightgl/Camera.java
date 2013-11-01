package de.fabmax.lightgl;

import android.opengl.Matrix;
import android.view.MotionEvent;

import de.fabmax.lightgl.util.GlMath;

/**
 * Base class for arbitrary camera implementations.
 * 
 * @author fabmax
 * 
 */
public abstract class Camera {

    // Camera position
    private final AnimatedVector mEye = new AnimatedVector(0, 0, 10);
    // Camera look at position
    private final AnimatedVector mLookAt = new AnimatedVector(0, 0, 0);
    // Camera up direction
    private final AnimatedVector mUp = new AnimatedVector(0, 1, 0);
    
    /** Matrix recalculation flag */
    protected boolean mDirty = true;

    /** Camera transformation matrices */
    protected final float[] mViewMatrix = new float[16];
    protected final float[] mProjMatrix = new float[16];

    /**
     * Sets the camera position.
     */
    public void setPosition(float x, float y, float z) {
        mEye.set(x, y, z);
        mDirty = true;
    }

    /**
     * Sets the position the camera looks at. This is an alternative way to set the camera's llok
     * direction.
     */
    public void setLookAt(float x, float y, float z) {
        mLookAt.set(x, y, z);
        mDirty = true;
    }

    /**
     * Sets the camera up direction.
     */
    public void setUpDirection(float x, float y, float z) {
        mUp.set(x, y, z);
        mDirty = true;
    }

    /**
     * Smoothly animate the camera to the specified position.
     */
    public void animatePositionTo(float x, float y, float z) {
        mEye.animateTo(x, y, z);
    }

    /**
     * Smoothly animate the camera to the specified direction.
     */
    public void animateLookAtTo(float x, float y, float z) {
        mLookAt.animateTo(x, y, z);
    }

    /**
     * Smoothly animate the camera to the specified up direction.
     */
    public void animateUpTo(float x, float y, float z) {
        mUp.animateTo(x, y, z);
    }

    /**
     * Sets the camera animation speed for {@link #animatePositionTo(float, float, float)},
     * {@link #animateLookAtTo(float, float, float)} and {@link #animateUpTo(float, float, float)}.
     * Larger values will increase the animation speed. The default value is 10. However, large
     * values may result in erratic behavior.
     */
    public void setCameraAnimationSpeed(float speed) {
        mEye.setStiffness(speed);
        mLookAt.setStiffness(speed);
        mUp.setStiffness(speed);
    }

    /**
     * Called on every {@link de.fabmax.lightgl.GfxEngine#onDrawFrame(javax.microedition.khronos.opengles.GL10)}
     * in order to animate the camera position and direction. deltaT is filtered by GfxEngine to
     * achieve a smoother animation.
     *
     * @param deltaT    time since last frame.
     */
    protected void animate(float deltaT) {
        if (mEye.animate(deltaT) | mLookAt.animate(deltaT) | mUp.animate(deltaT)) {
            mDirty = true;
        }
    }
    
    /**
     * Sets the dirty flag so that the camera matrices are recomputed on next call of setup().
     */
    protected void setDirty() {
        mDirty = true;
    }
    
    /**
     * Sets the view and projection matrices of the {@link GfxState} according to the current camera
     * settings.
     * 
     * @param state the GfxState to set up
     */
    public void setup(GfxState state) {
        checkMatrices();
        System.arraycopy(mProjMatrix, 0, state.getProjectionMatrix(), 0, 16);
        System.arraycopy(mViewMatrix, 0, state.getViewMatrix(), 0, 16);
        state.matrixUpdate();
    }
    
    /**
     * Recomputes view and projection matrices if needed.
     */
    private void checkMatrices() {
        if (mDirty) {
            mDirty = false;
            computeProjectionMatrix(mProjMatrix);
            computeViewMatrix(mViewMatrix);
        }
    }
    
    /**
     * Computes a {@link Ray} for the given screen coordinates. The Ray has the same origin and
     * direction as the virtual camera ray at that pixel. E.g. (x, y) can come from a
     * {@link MotionEvent} and the computed Ray can be used to pick scene objects.
     * 
     * @see GfxState#getViewport()
     * 
     * @param viewport
     *            Viewport dimensions
     * @param x
     *            X screen coordinate in pixels
     * @param y
     *            Y screen coordinate in pixels
     * @param result
     *            Ray representing the camera Ray at the specified pixel
     */
    public void getPickRay(int[] viewport, float x, float y, Ray result) {
        GlMath.computePickRay(viewport, mViewMatrix, mProjMatrix, x, y, result);
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
    public void computeViewMatrix(float[] viewMBuf) {
        Matrix.setLookAtM(viewMBuf, 0, mEye.x, mEye.y, mEye.z, mLookAt.x, mLookAt.y, mLookAt.z,
                mUp.x, mUp.y, mUp.z);
    }

    /**
     * Computes the projection matrix for this camera. This method is called by GfxEngine for the
     * active camera every time before a frame is rendered.
     * 
     * @param projMBuf
     *            16 element array where the projection matrix is stored in
     */
    public abstract void computeProjectionMatrix(float[] projMBuf);

    /**
     * Helper class for camera position, direction and up vector. Allows for smooth animation.
     * Animation is done by a critically damped mass spring damper system. Thus the animation starts
     * with quickly increasing speed and then slows down until he destination value is reached.
     */
    private static class AnimatedVector {
        float x;
        float y;
        float z;

        float mDstX;
        float mDstY;
        float mDstZ;

        float mDX;
        float mDY;
        float mDZ;

        float mStiffness;
        float mDamping;
        boolean mAnimated = false;

        AnimatedVector(float x, float y, float z) {
            set(x, y, z);
            setStiffness(10.0f);
        }

        /**
         * Set vector value without animation.
         */
        void set(float x, float y, float z) {
            this.x = mDstX = x;
            this.y = mDstY = y;
            this.z = mDstZ = z;
            mDX = mDY = mDZ = 0;
            mAnimated = false;
        }

        /**
         * Set animation target vector value.
         */
        void animateTo(float x, float y, float z) {
            mDstX = x;
            mDstY = y;
            mDstZ = z;
            mAnimated = true;
        }

        /**
         * Animate vector value towards previously set target value.
         */
        boolean animate(float deltaT) {
            if (!mAnimated) {
                return false;
            }

            float e = mDstX - x;
            mDX += (e * mStiffness - mDX * mDamping) * deltaT;
            x += mDX * deltaT;

            e = mDstY - y;
            mDY += (e * mStiffness - mDY * mDamping) * deltaT;
            y += mDY * deltaT;

            e = mDstZ - z;
            mDZ += (e * mStiffness - mDZ * mDamping) * deltaT;
            z += mDZ * deltaT;

            return true;
        }

        /**
         * Set animation speed.
         */
        void setStiffness(float stiffness) {
            mStiffness = stiffness;
            mDamping = 2.0f * (float) Math.sqrt(stiffness);
        }
    }
}
