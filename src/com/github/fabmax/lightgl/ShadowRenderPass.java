package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.glClearColor;
import android.opengl.Matrix;

/**
 * RenderPass that computes a shadow map for dynamic shadows.
 * 
 * @author fabmax
 * 
 */
public class ShadowRenderPass implements RenderPass {

    private static final int MAP_SIZE = 512;

    private OrthograpicCamera mShadowCamera = new OrthograpicCamera();
    private TextureRenderer mRenderer;
    private Shader mDepthShader;
    private int mTextureUnit = GL_TEXTURE1;

    private BoundingBox mSceneBounds = new BoundingBox(-10, 10, -10, 10, -10, 10);
    private BoundingBox mClipSize = new BoundingBox(-10, 10, -10, 10, -10, 10);
    private float[] mTmpVector = new float[8];
    private float[] mShadowViewMatrix = new float[16];    
    private float[] mShadowProjMatrix = new float[16];

    /**
     * Creates a new ShadowRenderPass. Must be called from the GL thread.
     * 
     * @param engine
     *            the graphics engine
     */
    public ShadowRenderPass(GfxEngine engine) {
        mDepthShader = new DepthShader(engine.getShaderManager());
        mRenderer = new TextureRenderer(engine);
        mRenderer.setTextureSize(MAP_SIZE, MAP_SIZE);
        
        // initialize the depth texture with maximum depth value
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        mRenderer.renderToTexture(engine, null);
        engine.getState().resetBackgroundColor();
        
        // set texture renderer border to 1 to reduce artifacts
        mRenderer.setBorder(1);
    }

    /**
     * Renders a shadow map for a single light.
     * 
     * @see RenderPass#onRender(GfxEngine)
     */
    @Override
    public void onRender(GfxEngine engine) {
        if (engine.getLights().size() == 0) {
            // there is no light to cast a shadow
            return;
        }
        Light l = engine.getLights().get(0);

        // compute view matrix for current light direction
        mShadowCamera.setPosition(l.posX, l.posY, l.posZ);
        mShadowCamera.setLookAt(0, 0, 0);
        mShadowCamera.getViewMatrix(mShadowViewMatrix);
        
        computeCamClipSize();
        mShadowCamera.setClipSize(mClipSize.getMinX(), mClipSize.getMaxX(), mClipSize.getMinY(),
                mClipSize.getMaxY(), mClipSize.getMinZ(), mClipSize.getMaxZ());
        mShadowCamera.getProjectionMatrix(mShadowProjMatrix);

        // setup engine state
        GfxState state = engine.getState();
        state.bindShader(mDepthShader);
        state.setLockShader(true);
        state.setCamera(mShadowCamera);
        
        // set the depth texture clear color values to maximum depth
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // render scene to the texture
        mRenderer.renderToTexture(engine, engine.getScene());

        // cleanup
        state.setLockShader(false);
        engine.getTextureManager().bindTexture(mRenderer.getTexture(), mTextureUnit);
        engine.getState().resetBackgroundColor();
    }
    
    /**
     * Sets the scene bounds. The specified volume will be covered by the schadow map renderer.
     * 
     * @param sceneBounds
     *            Bounds of the scene to be covered
     */
    public void setSceneBounds(BoundingBox sceneBounds) {
        mSceneBounds.set(sceneBounds);
    }
    
    /**
     * Returns the index of the texture unit the depth texture is bound to.
     * 
     * @return the index of the texture unit the depth texture is bound to
     */
    public int getTextureUnit() {
        return mTextureUnit - GL_TEXTURE0;
    }
    
    /**
     * Sets the texture unit the depth texture is bound to. Do not use GL_TEXTUREn but just the
     * index.
     * 
     * @param texUnit the texture unit to use
     */
    public void setTextureUnit(int texUnit) {
        mTextureUnit = texUnit + GL_TEXTURE0;
    }
    
    /**
     * Returns the view matrix from the shadow camera.
     * 
     * @return the view matrix from the shadow camera
     */
    public float[] getShadowViewMatrix() {
        return mShadowViewMatrix;
    }

    /**
     * Returns the projection matrix from the shadow camera.
     * 
     * @return the projection matrix from the shadow camera
     */
    public float[] getShadowProjectionMatrix() {
        return mShadowProjMatrix;
    }
    
    /**
     * Computes the camera clip size for current mShadowViewMatrix and mSceneBounds.
     */
    private void computeCamClipSize() {
        // compute clip size for camera to cover the complete scene
        mTmpVector[3] = 1;
        
        // (minX, minY, minZ)
        mSceneBounds.getMin(mTmpVector);
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.reset(mTmpVector[4], mTmpVector[5], mTmpVector[6]);

        // (minX, minY, maxZ)
        mTmpVector[2] = mSceneBounds.getMaxZ();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);

        // (minX, maxY, maxZ)
        mTmpVector[1] = mSceneBounds.getMaxY();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);

        // (minX, maxY, minZ)
        mTmpVector[2] = mSceneBounds.getMinZ();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);

        // (maxX, maxY, minZ)
        mTmpVector[0] = mSceneBounds.getMaxX();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);

        // (maxX, maxY, maxZ)
        mTmpVector[2] = mSceneBounds.getMaxZ();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);

        // (maxX, minY, maxZ)
        mTmpVector[1] = mSceneBounds.getMinY();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);

        // (maxX, minY, minZ)
        mTmpVector[2] = mSceneBounds.getMinZ();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
    }
}
