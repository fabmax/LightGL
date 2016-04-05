package de.fabmax.lightgl;

import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import de.fabmax.lightgl.util.BufferHelper;

import static android.opengl.GLES20.*;

/**
 * Current graphics engine state.
 * 
 * @author fth
 * 
 */
public class GfxState {

    public static final int MODEL_MATRIX_STACK_SIZE = 10;

    // viewport dimensions (x, y, width, height)
    private final IntBuffer mViewportBuffer = BufferHelper.createIntBuffer(4);
    private final int[] mViewport = new int[16];
    // projection matrix - holds field of view and mAspect ratio of the camera
    private final FloatBuffer mProjMatrixBuffer = BufferHelper.createFloatBuffer(16);
    private final float[] mProjMatrix = new float[16];
    // view matrix - holds the camera position
    private final FloatBuffer mViewMatrixBuffer = BufferHelper.createFloatBuffer(16);
    private final float[] mViewMatrix = new float[16];
    // model matrix stack - holds the geometry transformation
    private final FloatBuffer mModelMatrixBuffer = BufferHelper.createFloatBuffer(16);
    private final float[][] mModelMatrix = new float[MODEL_MATRIX_STACK_SIZE][16];
    private int mModelMatrixIdx = 0;
    // combined model view projection matrix
    private final float[] mMvpMatrix = new float[16];
    private final FloatBuffer mMvpMatrixBuffer = BufferHelper.createFloatBuffer(16);
    // temp matrix buffer for calculations
    private final float[] mTempMatrix = new float[16];
    
    private final float[] mBackgroundColor;

    private boolean mIsPrePass = false;

    private float mGlobalSaturation = 1.0f;

    private final ShaderManager mShaderManager;

    /**
     * Creates a new GfxState object.
     */
    protected GfxState(ShaderManager shaderManager) {
        mShaderManager = shaderManager;

        Matrix.setIdentityM(mProjMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix[0], 0);
        Matrix.setIdentityM(mMvpMatrix, 0);
        
        mBackgroundColor = new float[] { 0.0f, 0.0f, 0.0f };
    }

    protected void setIsPrePass(boolean isPrePass) {
        mIsPrePass = isPrePass;
    }

    public boolean isPrePass() {
        return mIsPrePass;
    }

    /**
     * Resets the current engine state. This method is called before a new frame is rendered.
     */
    public void reset(LightGlContext context) {
        // reset matrices
        mModelMatrixIdx = 0;
        Matrix.setIdentityM(mModelMatrix[0], 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjMatrix, 0);
        Matrix.setIdentityM(mMvpMatrix, 0);
        
        // unbind shader, is needed so that Shader#onBind() is called on next frame render
        // if only one shader is used
        mShaderManager.bindShader(context, null);
    }

    /**
     * Sets the scene background color.
     */
    public void setBackgroundColor(float red, float green, float blue) {
        mBackgroundColor[0] = red;
        mBackgroundColor[1] = green;
        mBackgroundColor[2] = blue;
        resetBackgroundColor();
    }

    /**
     * Resets the scene background color to the stored value.
     */
    public void resetBackgroundColor() {
        glClearColor(mBackgroundColor[0], mBackgroundColor[1], mBackgroundColor[2], 1.0f);
    }

    public float getGlobalSaturation() {
        return mGlobalSaturation;
    }

    public void setGloabalSaturation(float globalSaturation) {
        mGlobalSaturation = globalSaturation;
    }

    /**
     * Returns the aspect ratio of the current viewport.
     * 
     * @return the aspect ratio of this camera
     */
    public float getAspectRatio() {
        if (mViewport[2] > 0) {
            return (float) mViewport[2] / mViewport[3];
        } else {
            return 1;
        }
    }
    
    /**
     * Returns the current viewport dimensions. The dimensions are returned in a 4-element int array
     * with the format ( x, y, width, height ).
     * 
     * @return the current viewport dimensions 
     */
    public int[] getViewport() {
        return mViewport;
    }

    /**
     * Returns the viewport as an IntBuffer. The returned buffer is not kept in sync with the
     * viewport. Recall this method to update the buffer content.
     * 
     * @see #getViewport()
     * @return the viewport as an IntBuffer
     */
    public IntBuffer getViewportAsBuffer() {
        mViewportBuffer.put(mViewport);
        mViewportBuffer.flip();
        return mViewportBuffer;
    }
    
    /**
     * Updates the viewport dimensions.
     */
    public void setViewport(int x, int y, int width, int height) {
        mViewport[0] = x;
        mViewport[1] = y;
        mViewport[2] = width;
        mViewport[3] = height;

        glViewport(x, y, width, height);
    }

    /**
     * Pushes the current model matrix to the matrix stack.
     */
    public void pushModelMatrix() {
        if (mModelMatrixIdx >= MODEL_MATRIX_STACK_SIZE) {
            // model matrix stack size is exceeded
            throw new RuntimeException("Model matrix stack overflow");
        }
        // copy current model matrix to next index
        System.arraycopy(mModelMatrix[mModelMatrixIdx], 0, mModelMatrix[mModelMatrixIdx + 1], 0, 16);
        mModelMatrixIdx++;
    }

    /**
     * Restores the previous model matrix from the matrix stack.
     */
    public void popModelMatrix() {
        if (mModelMatrixIdx <= 0) {
            // no previos matrix to pop
            throw new RuntimeException("Model matrix stack underflow");
        }
        mModelMatrixIdx--;
        matrixUpdate();
    }

    /**
     * Returns the current MVP matrix, that is model-, view- and projection matrix multiplied
     * together.
     * 
     * @return the current MVP matrix
     */
    public float[] getMvpMatrix() {
        return mMvpMatrix;
    }

    /**
     * Returns the current MVP matrix as an FloatBuffer. The returned buffer is not kept in sync with the
     * MVP matrix. Recall this method to update the buffer content.
     * 
     * @see #getMvpMatrix()
     * @return the MVP matrix as an FloatBuffer
     */
    public FloatBuffer getMvpMatrixAsBuffer() {
        mMvpMatrixBuffer.put(mMvpMatrix);
        mMvpMatrixBuffer.flip();
        return mMvpMatrixBuffer;
    }

    /**
     * Returns the current view matrix. The matrix is returned by reference and changes to it will
     * be reflected immediately. However, for the MVP matrix to reflect the changes
     * {@link GfxState#matrixUpdate()} must be called.
     * 
     * @return the current view matrix
     */
    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    /**
     * Returns the current view matrix as an FloatBuffer. The returned buffer is not kept in sync with the
     * view matrix. Recall this method to update the buffer content.
     * 
     * @see #getViewMatrix()
     * @return the view matrix as an FloatBuffer
     */
    public FloatBuffer getViewMatrixAsBuffer() {
        mViewMatrixBuffer.put(mViewMatrix);
        mViewMatrixBuffer.flip();
        return mViewMatrixBuffer;
    }

    /**
     * Returns the current projection matrix. The matrix is returned by reference and changes to it
     * will be reflected immediately. However, for the MVP matrix to reflect the changes
     * {@link GfxState#matrixUpdate()} must be called.
     * 
     * @return the current projection matrix
     */
    public float[] getProjectionMatrix() {
        return mProjMatrix;
    }

    /**
     * Returns the current projection matrix as an FloatBuffer. The returned buffer is not kept in sync with the
     * projection matrix. Recall this method to update the buffer content.
     * 
     * @see #getProjectionMatrix()
     * @return the projection matrix as an FloatBuffer
     */
    public FloatBuffer getProjectionMatrixAsBuffer() {
        mProjMatrixBuffer.put(mProjMatrix);
        mProjMatrixBuffer.flip();
        return mProjMatrixBuffer;
    }

    /**
     * Returns the current model matrix. The matrix is returned by reference and changes to it
     * will be reflected immediately. However, for the MVP matrix to reflect the changes
     * {@link GfxState#matrixUpdate()} must be called.
     */
    public float[] getModelMatrix() {
        return mModelMatrix[mModelMatrixIdx];
    }

    /**
     * Returns the current model matrix as an FloatBuffer. The returned buffer is not kept in sync with the
     * model matrix. Recall this method to update the buffer content.
     * 
     * @see #getModelMatrix()
     * @return the model matrix as an FloatBuffer
     */
    public FloatBuffer getModelMatrixAsBuffer() {
        mModelMatrixBuffer.put(mModelMatrix[mModelMatrixIdx]);
        mModelMatrixBuffer.flip();
        return mModelMatrixBuffer;
    }

    /**
     * Sets the current model matrix to the specified buffer. {@link GfxState#matrixUpdate()} is
     * called automatically in order to update the MVP matrix.
     * 
     * @param modelMBuf
     *            the buffer to set as model matrix.
     */
    public void setModelMatrix(float[] modelMBuf) {
        System.arraycopy(modelMBuf, 0, mModelMatrix[mModelMatrixIdx], 0, 16);
        matrixUpdate();
    }

    /**
     * Computes the MVP matrix from the individual model-, view- and projection matrices. This
     * method must be called after an update of any of these matrices.
     */
    public void matrixUpdate() {
        // Combine projection, model and view matrices
        Matrix.multiplyMM(mTempMatrix, 0, mViewMatrix, 0, mModelMatrix[mModelMatrixIdx], 0);
        Matrix.multiplyMM(mMvpMatrix, 0, mProjMatrix, 0, mTempMatrix, 0);

        // notify current shader about matrix update
        Shader bound = mShaderManager.getBoundShader();
        if (bound != null) {
            bound.onMatrixUpdate(this);
        }
    }
}
