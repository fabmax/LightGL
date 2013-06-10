package de.fabmax.lightgl;

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
        mShadowCamera.setPosition(0, 0, 0);
        mShadowCamera.setLookAt(-l.position[0], -l.position[1], -l.position[2]);
//        mShadowCamera.setPosition(l.position[0], l.position[1], l.position[2]);
//        mShadowCamera.setLookAt(0, 0, 0);
        mShadowCamera.computeViewMatrix(mShadowViewMatrix);
        
        computeCamClipSize();
//        mShadowCamera.setClipSize(-20, 20, -20, 20, -20, 20);
        mShadowCamera.setClipSize(mClipSize.getMinX(), mClipSize.getMaxX(), mClipSize.getMinY(),
                mClipSize.getMaxY(), mClipSize.getMinZ(), mClipSize.getMaxZ());
        mShadowCamera.computeProjectionMatrix(mShadowProjMatrix);

        // setup engine state
        GfxState state = engine.getState();
        state.bindShader(mDepthShader);
        state.setLockShader(true);
        mShadowCamera.setup(state);
        
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
//        Log.d("srp", "los");
//
//        Log.d("srp", "[" + mShadowViewMatrix[0] + ", " + mShadowViewMatrix[1] + ", " + mShadowViewMatrix[2] + ", " + mShadowViewMatrix[3] + "]");
//        Log.d("srp", "[" + mShadowViewMatrix[4] + ", " + mShadowViewMatrix[5] + ", " + mShadowViewMatrix[6] + ", " + mShadowViewMatrix[7] + "]");
//        Log.d("srp", "[" + mShadowViewMatrix[8] + ", " + mShadowViewMatrix[9] + ", " + mShadowViewMatrix[10] + ", " + mShadowViewMatrix[11] + "]");
//        Log.d("srp", "[" + mShadowViewMatrix[12] + ", " + mShadowViewMatrix[13] + ", " + mShadowViewMatrix[14] + ", " + mShadowViewMatrix[15] + "]");
        
        // (minX, minY, minZ)
        mSceneBounds.getMin(mTmpVector);
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.reset(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
//        Log.d("srp", "in  x: " + mTmpVector[0] + ", y: " + mTmpVector[1] + ", z: " + mTmpVector[2] + ", w: " + mTmpVector[3]);
//        Log.d("srp", "out x: " + mTmpVector[4] + ", y: " + mTmpVector[5] + ", z: " + mTmpVector[6] + ", w: " + mTmpVector[7]);

        // (minX, minY, maxZ)
        mTmpVector[2] = mSceneBounds.getMaxZ();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
//        Log.d("srp", "in  x: " + mTmpVector[0] + ", y: " + mTmpVector[1] + ", z: " + mTmpVector[2] + ", w: " + mTmpVector[3]);
//        Log.d("srp", "out x: " + mTmpVector[4] + ", y: " + mTmpVector[5] + ", z: " + mTmpVector[6] + ", w: " + mTmpVector[7]);

        // (minX, maxY, maxZ)
        mTmpVector[1] = mSceneBounds.getMaxY();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
//        Log.d("srp", "in  x: " + mTmpVector[0] + ", y: " + mTmpVector[1] + ", z: " + mTmpVector[2] + ", w: " + mTmpVector[3]);
//        Log.d("srp", "out x: " + mTmpVector[4] + ", y: " + mTmpVector[5] + ", z: " + mTmpVector[6] + ", w: " + mTmpVector[7]);

        // (minX, maxY, minZ)
        mTmpVector[2] = mSceneBounds.getMinZ();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
//        Log.d("srp", "in  x: " + mTmpVector[0] + ", y: " + mTmpVector[1] + ", z: " + mTmpVector[2] + ", w: " + mTmpVector[3]);
//        Log.d("srp", "out x: " + mTmpVector[4] + ", y: " + mTmpVector[5] + ", z: " + mTmpVector[6] + ", w: " + mTmpVector[7]);

        // (maxX, maxY, minZ)
        mTmpVector[0] = mSceneBounds.getMaxX();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
//        Log.d("srp", "in  x: " + mTmpVector[0] + ", y: " + mTmpVector[1] + ", z: " + mTmpVector[2] + ", w: " + mTmpVector[3]);
//        Log.d("srp", "out x: " + mTmpVector[4] + ", y: " + mTmpVector[5] + ", z: " + mTmpVector[6] + ", w: " + mTmpVector[7]);

        // (maxX, maxY, maxZ)
        mTmpVector[2] = mSceneBounds.getMaxZ();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
//        Log.d("srp", "in  x: " + mTmpVector[0] + ", y: " + mTmpVector[1] + ", z: " + mTmpVector[2] + ", w: " + mTmpVector[3]);
//        Log.d("srp", "out x: " + mTmpVector[4] + ", y: " + mTmpVector[5] + ", z: " + mTmpVector[6] + ", w: " + mTmpVector[7]);

        // (maxX, minY, maxZ)
        mTmpVector[1] = mSceneBounds.getMinY();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
//        Log.d("srp", "in  x: " + mTmpVector[0] + ", y: " + mTmpVector[1] + ", z: " + mTmpVector[2] + ", w: " + mTmpVector[3]);
//        Log.d("srp", "out x: " + mTmpVector[4] + ", y: " + mTmpVector[5] + ", z: " + mTmpVector[6] + ", w: " + mTmpVector[7]);

        // (maxX, minY, minZ)
        mTmpVector[2] = mSceneBounds.getMinZ();
        Matrix.multiplyMV(mTmpVector, 4, mShadowViewMatrix, 0, mTmpVector, 0);
        mClipSize.addPoint(mTmpVector[4], mTmpVector[5], mTmpVector[6]);
//        Log.d("srp", "in  x: " + mTmpVector[0] + ", y: " + mTmpVector[1] + ", z: " + mTmpVector[2] + ", w: " + mTmpVector[3]);
//        Log.d("srp", "out x: " + mTmpVector[4] + ", y: " + mTmpVector[5] + ", z: " + mTmpVector[6] + ", w: " + mTmpVector[7]);

//        Log.d("srp", "min x: " + mClipSize.getMinX() + ", y: " + mClipSize.getMinY() + ", z: " + mClipSize.getMinZ());
//        Log.d("srp", "max x: " + mClipSize.getMaxX() + ", y: " + mClipSize.getMaxY() + ", z: " + mClipSize.getMaxZ());
        mClipSize.setMinX(mClipSize.getMinX() - 5f);
        mClipSize.setMinY(mClipSize.getMinY() - 5f);
        mClipSize.setMinZ(mClipSize.getMinZ() - 5f);
        mClipSize.setMaxX(mClipSize.getMaxX() + 5f);
        mClipSize.setMaxY(mClipSize.getMaxY() + 5f);
        mClipSize.setMaxZ(mClipSize.getMaxZ() + 5f);
    }
}
