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

    /** Camera position */
    protected float mEyeX = 0, mEyeY = 0, mEyeZ = 10;

    /** Camera look at position */
    protected float mLookAtX = 0, mLookAtY = 0, mLookAtZ = 0;

    /** Camera up direction */
    protected float mUpX = 0, mUpY = 1, mUpZ = 0;
    
    /** Matrix recalculation flag */
    protected boolean mDirty = true;

    /** Camera transformation matrices */
    protected float[] mViewMatrix = new float[16];
    protected float[] mProjMatrix = new float[16];

    /**
     * Sets the camera position.
     */
    public void setPosition(float x, float y, float z) {
        mEyeX = x;
        mEyeY = y;
        mEyeZ = z;
        mDirty = true;
    }

    /**
     * Sets the position the camera looks at. This is an alternative way to set the camera's llok
     * direction.
     * 
     * @see #setLookDirection(float, float, float)
     */
    public void setLookAt(float x, float y, float z) {
        mLookAtX = x;
        mLookAtY = y;
        mLookAtZ = z;
        mDirty = true;
    }

    /**
     * Sets the look direction of the camera.
     */
    public void setLookDirection(float x, float y, float z) {
        mLookAtX = mEyeX + x;
        mLookAtY = mEyeY + y;
        mLookAtZ = mEyeZ + z;
        mDirty = true;
    }

    /**
     * Sets the camera up direction.
     */
    public void setUpDirection(float x, float y, float z) {
        mUpX = x;
        mUpY = y;
        mUpZ = z;
        mDirty = true;
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
     * {@link MotionEvent} and the computed Ray can be used to pick scene objects. Notice that this
     * function uses the projection and view matrices from {@link GfxState} so these must be valid
     * in order for this function to work. Use {@link GfxState#setCamera(Camera)} to explicitly set
     * the camera matrices.
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
        GlMath.computePickRay(viewport, mViewMatrix, 0, mProjMatrix, 0, x, y, result);
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
        Matrix.setLookAtM(viewMBuf, 0, mEyeX, mEyeY, mEyeZ, mLookAtX, mLookAtY, mLookAtZ, mUpX,
                mUpY, mUpZ);
    }

    /**
     * Computes the projection matrix for this camera. This method is called by GfxEngine for the
     * active camera every time before a frame is rendered.
     * 
     * @param projMBuf
     *            16 element array where the projection matrix is stored in
     */
    public abstract void computeProjectionMatrix(float[] projMBuf);
}
