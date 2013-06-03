package com.github.fabmax.lightgl;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import android.opengl.Matrix;

/**
 * ShadowShader is a PhongShader that also supports dynamic shadows. To compute the necessary
 * shadow depth map a {@link ShadowRenderPass} must be set as pre-render pass with
 * {@link GfxEngine#setPreRenderPass(RenderPass)}.
 * 
 * @author fabmax
 * 
 */
public class ShadowShader extends PhongShader {

    private ShadowRenderPass mShadowPass;

    private int muShadowSamplerHandle;
    private int muShadowMvpMatrixHandle;

    private float[] mTempMatrix = new float[16];
    private float[] mShadowMvpMatrix = new float[16];
    private float[] mShadowBiasMatrix;

    /**
     * Creates a ShadowShader.
     * 
     * @param shaderMgr
     *            the shader manager
     * @param texture
     *            the texture to map on drawn objects
     * @param shadowPass
     *            the ShadowRenderPass used to compute the depth texture
     */
    public ShadowShader(ShaderManager shaderMgr, Texture texture, ShadowRenderPass shadowPass) {
        super(shaderMgr, texture, "shadow");

        mShadowPass = shadowPass;
        mShadowBiasMatrix = new float[] {
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f
        };

        muShadowSamplerHandle = glGetUniformLocation(mShaderHandle, "uShadowSampler");
        muShadowMvpMatrixHandle = glGetUniformLocation(mShaderHandle, "uShadowMvpMatrix");
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
     * @see PhongShader#onBind(GfxState)
     */
    @Override
    public void onBind(GfxState state) {
        super.onBind(state);

        glUniform1i(muShadowSamplerHandle, mShadowPass.getTextureUnit());
    }
}
