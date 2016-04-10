package de.fabmax.lightgl;

import android.util.Log;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * A basic texture shader without any lighting
 * 
 * @author fabmax
 * 
 */
public class TextureShader extends Shader {

    private static final String TAG = "TextureShader";

    private int muMvpMatrixHandle = 0;
    private int muTextureSamplerHandle = 0;
    private int muAlphaHandle = 0;

    private Texture mTexture;
    private float mAlpha = 1;

    /**
     * Creates a new TextureShader.
     *
     * @param shaderMgr the {@link ShaderManager}
     */
    public TextureShader(ShaderManager shaderMgr) {
        super(shaderMgr);
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
        try {
            // load color shader code
            int handle = shaderMgr.loadShader("texture");
            setGlHandle(handle);

            // get uniform locations
            muMvpMatrixHandle = glGetUniformLocation(handle, "uMvpMatrix");
            muTextureSamplerHandle = glGetUniformLocation(handle, "uTextureSampler");
            muAlphaHandle  = glGetUniformLocation(handle, "uAlpha");
            
            // enable attributes
            enableAttribute(ATTRIBUTE_TEXTURE_COORDS, "aVertexTexCoord");
            enableAttribute(ATTRIBUTE_POSITIONS, "aVertexPosition_modelspace");
        } catch (LightGlException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Sets the alpha value that is multiplied with the texture's alpha channel.
     */
    public void setAlpha(LightGlContext glContext, float alpha) {
        mAlpha = alpha;
        if (glContext.getShaderManager().getBoundShader() == this) {
            glUniform1f(muAlphaHandle, mAlpha);
        }
    }

    /**
     * Returns the alpha value that is multiplied with the texture's alpha channel.
     */
    public float getAlpha() {
        return mAlpha;
    }

    /**
     * Returns the texture used by this shader.
     *
     * @return the texture used by this shader
     */
    public Texture getTexture() {
        return mTexture;
    }

    /**
     * Sets the texture to be used by this shader.
     *
     * @param texture
     *            the texture to be used by this shader
     */
    public void setTexture(LightGlContext glContext, Texture texture) {
        mTexture = texture;
        if (glContext.getShaderManager().getBoundShader() == this) {
            glContext.getTextureManager().bindTexture(mTexture);
        }
    }

    /**
     * Is called if this shader is bound.
     * 
     * @see Shader#onBind(LightGlContext)
     */
    @Override
    public void onBind(LightGlContext glContext) {
        // pass current MVP matrix to shader
        onMatrixUpdate(glContext.getState());

        glUniform1f(muAlphaHandle, mAlpha);
        if(mTexture != null) {
            glContext.getTextureManager().bindTexture(mTexture);
            glUniform1i(muTextureSamplerHandle, 0);
        }
    }

    /**
     * Is called if the MVP matrix has changed.
     * 
     * @see Shader#onMatrixUpdate(GfxState)
     */
    @Override
    public void onMatrixUpdate(GfxState state) {
        // pass current MVP matrix to shader
        glUniformMatrix4fv(muMvpMatrixHandle, 1, false, state.getMvpMatrix(), 0);
    }

}
