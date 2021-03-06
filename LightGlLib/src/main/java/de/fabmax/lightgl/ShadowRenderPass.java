package de.fabmax.lightgl;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.glClearColor;

/**
 * RenderPass that computes a shadow map for dynamic shadows.
 * 
 * @author fabmax
 * 
 */
public class ShadowRenderPass implements RenderPass {

    private final OrthograpicCamera mShadowCamera = new OrthograpicCamera();
    private TextureRenderer mRenderer;
    private Shader mDepthShader;
    private int mTextureUnit = GL_TEXTURE1;

    private final BoundingBox mSceneBounds = new BoundingBox(-10, 10, -10, 10, -10, 10);
    private final float[] mShadowViewMatrix = new float[16];
    private final float[] mShadowProjMatrix = new float[16];

    private int mShadowMapSz = 512;

    /**
     * Renders a shadow map for a single light.
     * 
     * @see RenderPass#onRender(LightGlContext)
     */
    @Override
    public void onRender(LightGlContext glContext) {
        checkCreateGlObjects(glContext);
        
        if (glContext.getEngine().getLights().size() == 0) {
            // there is no light to cast a shadow
            return;
        }
        Light l = glContext.getEngine().getLights().get(0);

        // compute view matrix for current light direction
        computeCamClipSize(l);
        mShadowCamera.computeViewMatrix(mShadowViewMatrix);
        mShadowCamera.computeProjectionMatrix(mShadowProjMatrix);

        // setup engine state
        glContext.getShaderManager().bindShader(glContext, mDepthShader);
        glContext.getShaderManager().setLockShader(true);
        mShadowCamera.setup(glContext.getState());
        
        // set the depth texture clear color values to maximum depth
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // render scene to the texture
        mRenderer.renderToTexture(glContext, glContext.getEngine().getScene());

        // cleanup
        glContext.getShaderManager().setLockShader(false);
        glContext.getTextureManager().bindTexture(mRenderer.getTexture(), mTextureUnit);
        glContext.getState().resetBackgroundColor();
    }
    
    /**
     * Checks if the GL objects (texture renderer, shader, etc.) are created and if not creates them.
     */
    private void checkCreateGlObjects(LightGlContext glContext) {
        if (mDepthShader == null || mRenderer == null) {
            mDepthShader = new DepthShader(glContext.getShaderManager());
            mRenderer = new TextureRenderer();
            mRenderer.setTextureSize(mShadowMapSz, mShadowMapSz);
            
            // initialize the depth texture with maximum depth value
            glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            mRenderer.renderToTexture(glContext, null);
            glContext.getState().resetBackgroundColor();
            
            // set texture renderer border to 1 to reduce artifacts
            mRenderer.setBorder(1);
        }
    }
    
    /**
     * Sets the size of the shadow map texture. The actual texture size is sz x sz. Maximum texture
     * size depends on the hardware. Moreover texture size has a heavy impact on performance.
     * Default size is 512 x 512.
     * 
     * @param sz
     *            the shadow map size to use
     */
    public void setShadowMapSize(int sz) {
        mShadowMapSz = sz;
        if(mRenderer != null) {
            mRenderer.setTextureSize(mShadowMapSz, mShadowMapSz);
        }
    }
    
    /**
     * Returns the size of the shadow map.
     * 
     * @return the size of the shadow map
     */
    public int getShadowMapSize() {
        return mShadowMapSz;
    }
    
    /**
     * Sets the scene bounds. The specified volume will be covered by the shadow map renderer.
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
    private void computeCamClipSize(Light l) {
        float lx = (mSceneBounds.maxX - mSceneBounds.minX) / 2;
        float ly = (mSceneBounds.maxY - mSceneBounds.minY) / 2;
        float lz = (mSceneBounds.maxZ - mSceneBounds.minZ) / 2;
        float cx = mSceneBounds.minX + lx;
        float cy = mSceneBounds.minY + ly;
        float cz = mSceneBounds.minZ + lz;
        
        float d = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);

        mShadowCamera.setPosition(cx, cy, cz);
        mShadowCamera.setLookAt(cx - l.position[0], cy - l.position[1], cz - l.position[2]);
        mShadowCamera.setClipSize(cx - d, cx + d, cy - d, cy + d, cz - d, cz + d);
    }
}
