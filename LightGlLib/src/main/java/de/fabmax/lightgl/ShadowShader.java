package de.fabmax.lightgl;

import android.opengl.Matrix;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * ShadowShader is a SimpleShader that supports dynamic shadows. To compute the necessary
 * shadow depth map a {@link ShadowRenderPass} must be set as pre-render pass with
 * {@link GfxEngine#setPreRenderPass(RenderPass)}.
 * 
 * @author fabmax
 * 
 */
public class ShadowShader extends SimpleShader {

    private final ShadowRenderPass mShadowPass;

    private int muShadowSamplerHandle;
    private int muShadowMvpMatrixHandle;
    private int muMapScaleHandle;
    
    private final float[] mTempMatrix = new float[16];
    private final float[] mShadowMvpMatrix = new float[16];
    private final float[] mShadowBiasMatrix;

    /**
     * Creates a ShadowShader with vertex coloring and no further lighting.
     *
     * @param shaderMgr
     *            the shader manager
     * @param shadowPass
     *            the ShadowRenderPass used to compute the depth texture
     */
    public static ShadowShader createNoLightingShadowShader(ShaderManager shaderMgr, ShadowRenderPass shadowPass) {
        return new ShadowShader(shaderMgr, null, false, shadowPass, "color_shadow");
    }

    /**
     * Creates a ShadowShader with Gouraud model. Gouraud shading is faster than Phong shading but
     * not as accurate.
     * 
     * @param shaderMgr
     *            the shader manager
     * @param texture
     *            the texture to map on drawn objects
     * @param shadowPass
     *            the ShadowRenderPass used to compute the depth texture
     */
    public static ShadowShader createGouraudShadowShader(ShaderManager shaderMgr, Texture texture, ShadowRenderPass shadowPass) {
        return new ShadowShader(shaderMgr, texture, true, shadowPass, "gouraud_shadow");
    }

    /**
     * Creates a ShadowShader with Phong model. Phong shading offers a higher quality than Gouraud
     * shading but is slower.
     * 
     * @param shaderMgr
     *            the shader manager
     * @param texture
     *            the texture to map on drawn objects
     * @param shadowPass
     *            the ShadowRenderPass used to compute the depth texture
     */
    public static ShadowShader createPhongShadowShader(ShaderManager shaderMgr, Texture texture, ShadowRenderPass shadowPass) {
        return new ShadowShader(shaderMgr, texture, true, shadowPass, "phong_shadow");
    }
    
    /**
     * Creates a ShadowShader with gouraud or phong light model.
     * 
     * @param shaderMgr
     *            the shader manager
     * @param texture
     *            the texture to map on drawn objects
     * @param shadowPass
     *            the ShadowRenderPass used to compute the depth texture
     * @param shaderFile
     *            the shader filename to load
     */
    private ShadowShader(ShaderManager shaderMgr, Texture texture, boolean useLighting, ShadowRenderPass shadowPass, String shaderFile) {
        super(shaderMgr, texture != null, useLighting, shaderFile);
        setTexture(texture);

        mShadowPass = shadowPass;
        mShadowBiasMatrix = new float[] {
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f
        };
    }

    /**
     * Loads the color shader program. Is called automatically when this shader is
     * bound for the first time and was not called manually before.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     */
    @Override
    public void loadShader(ShaderManager shaderMgr) {
        super.loadShader(shaderMgr);
        
        muShadowSamplerHandle = glGetUniformLocation(mHandle[0], "uShadowSampler");
        muShadowMvpMatrixHandle = glGetUniformLocation(mHandle[0], "uShadowMvpMatrix");
        muMapScaleHandle = glGetUniformLocation(mHandle[0], "uMapScale");
    }

    /**
     * Is called if the MVP matrix has changed.
     * 
     * @see Shader#onMatrixUpdate(GfxState)
     */
    @Override
    public void onMatrixUpdate(GfxState state) {
        super.onMatrixUpdate(state);

        // compute the shadow mvp matrix
        // this matrix is needed to transform vertex coordinates to the corresponding point in the
        // shadow depth map
        float[] shadowView = mShadowPass.getShadowViewMatrix();
        float[] shadowProj = mShadowPass.getShadowProjectionMatrix();
        Matrix.multiplyMM(mTempMatrix, 0, shadowView, 0, state.getModelMatrix(), 0);
        Matrix.multiplyMM(mShadowMvpMatrix, 0, shadowProj, 0, mTempMatrix, 0);
        Matrix.multiplyMM(mTempMatrix, 0, mShadowBiasMatrix, 0, mShadowMvpMatrix, 0);

        glUniformMatrix4fv(muShadowMvpMatrixHandle, 1, false, mTempMatrix, 0);
    }

    /**
     * Is called if this shader is bound.
     * 
     * @see SimpleShader#onBind(LightGlContext)
     */
    @Override
    public void onBind(LightGlContext glContext) {
        super.onBind(glContext);

        glUniform1i(muShadowSamplerHandle, mShadowPass.getTextureUnit());
        //glUniform1f(muMapScaleHandle, 1.4142f / mShadowPass.getShadowMapSize());
        glUniform1f(muMapScaleHandle, 3f / mShadowPass.getShadowMapSize());
    }
}
