package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glClearColor;
import android.opengl.Matrix;

/**
 * Current graphics engine state.
 * 
 * @author fabmax
 * 
 */
public class GfxState {

    public static final int MODEL_MATRIX_STACK_SIZE = 10;

    private final GfxEngine mEngine;
    private final ShaderManager mShaderManager;
    private final TextureManager mTextureManager;

    // projection matrix - holds field of view and mAspect ratio of the camera
    private final float[] mProjMatrix = new float[16];
    // view matrix - holds the camera position
    private final float[] mViewMatrix = new float[16];
    // model matrix stack - holds the geometry transformation
    private final float[][] mModelMatrix = new float[MODEL_MATRIX_STACK_SIZE][16];
    private int mModelMatrixIdx = 0;
    // combined model view projection matrix
    private final float[] mMvpMatrix = new float[16];
    // temp matrix buffer for calculations
    private final float[] mTempMatrix = new float[16];
    
    private final float[] mBackgroundColor;
    
    private boolean mLockShader = false;

    /**
     * Creates a new GfxState object.
     */
    protected GfxState(GfxEngine engine, ShaderManager shaderMgr, TextureManager textureMgr) {
        mEngine = engine;
        mShaderManager = shaderMgr;
        mTextureManager = textureMgr;

        Matrix.setIdentityM(mProjMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix[0], 0);
        Matrix.setIdentityM(mMvpMatrix, 0);
        
        mBackgroundColor = new float[] { 0.0f, 0.0f, 0.0f };
    }

    /**
     * Returns the graphics engine this state belongs to.
     * 
     * @return the graphics engine.
     */
    public GfxEngine getEngine() {
        return mEngine;
    }

    /**
     * Binds the specified texture to texture unit 0.
     * 
     * @param texture
     *            the texture to be bound
     */
    public void bindTexture(Texture texture) {
        mTextureManager.bindTexture(texture, GL_TEXTURE0);
    }

    /**
     * Binds the specified texture to the given texture unit.
     * 
     * @param texture
     *            the texture to be bound
     * @param texUnit
     *            the texture unit to use
     */
    public void bindTexture(Texture texture, int texUnit) {
        mTextureManager.bindTexture(texture, texUnit);
    }
    
    public void setLockShader(boolean enabled) {
        mLockShader = enabled;
    }

    /**
     * Binds the specified shader that is to be used for successive rendering operations.
     * 
     * @param shader
     *            the shader to be used for successive primitive rendering.
     */
    public void bindShader(Shader shader) {
        if (!mLockShader) {
            mShaderManager.bindShader(this, shader);
        }
    }

    /**
     * Returns the currently bound shader.
     * 
     * @return the currently bound shader
     */
    public Shader getBoundShader() {
        return mShaderManager.getBoundShader();
    }

    /**
     * Resets the current engine state. This method is called before a new frame is rendered.
     */
    public void reset() {
        // reset matrices
        mModelMatrixIdx = 0;
        Matrix.setIdentityM(mModelMatrix[0], 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjMatrix, 0);
        Matrix.setIdentityM(mMvpMatrix, 0);
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

    /**
     * Sets the view and projection matrices for the specified camera.
     * 
     * @param camera
     *            the active camera used to compute the camera matrices.
     */
    public void setCamera(Camera camera) {
    	// update camera matrices
        camera.getProjectionMatrix(mProjMatrix);
        camera.getViewMatrix(mViewMatrix);

        matrixUpdate();
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
     * Copies the current model matrix to the specified buffer.
     * 
     * @param modelMBuf
     *            the buffer where the model matrix is copied into.
     */
    public float[] getModelMatrix() {
        return mModelMatrix[mModelMatrixIdx];
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
