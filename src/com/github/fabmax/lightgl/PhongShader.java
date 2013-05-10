package com.github.fabmax.lightgl;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;

import java.util.ArrayList;

import android.util.Log;

/**
 * A basic Phong shader. Supports a single directional light source if the list returned by
 * {@link GfxEngine#getLights()} contains multiple lights only the first one is considered. Meshes
 * rendered with this shader must have defined normal and color attributes.
 * 
 * @author fabmax
 * 
 */
public class PhongShader extends Shader {

    private static final String TAG = "PhongShader";

    // shininess coefficient for phong lighting model
    private float mShininess = 20.0f;

    // shader handle
    private int mShaderHandle = 0;

    // uniform handles
    private int muMvpMatrixHandle = 0;
    private int muModelMatrixHandle = 0;
    private int muViewMatrixHandle = 0;
    private int muLightDirectionHandle = 0;
    private int muShininessHandle = 0;
    private int muLightColorHandle = 0;
    private int muTextureSampler = 0;

    // optional texture
    private Texture mTexture;
    
    // buffer for current model matrix
    private float[] mModelMatrix = new float[16];

    /**
     * Creates a new PhongShader object. Rendered objects must provide vertex colors.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     */
    public PhongShader(ShaderManager shaderMgr) {
        this(shaderMgr, null);
    }
    
    /**
     * Creates a new PhongShader object. If a texture is specified, this texture will be mapped onto
     * the shaded object. Otherwise its vertex color information will be used.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     * @param texture
     *            Optional texture that is mapped onto the shaded object
     */
    public PhongShader(ShaderManager shaderMgr, Texture texture) {
        // load color shader code
        try {
            if(texture != null) {
                // load shader with texture mapping
                mShaderHandle = shaderMgr.loadShader("phong_texture");
            } else {
                // load shader with color mapping
                mShaderHandle = shaderMgr.loadShader("phong_color");
            }
        } catch (GlException e) {
            Log.e(TAG, e.getMessage());
        }
        
        mTexture = texture;

        // get uniform locations
        muMvpMatrixHandle = glGetUniformLocation(mShaderHandle, "uMvpMatrix");
        muModelMatrixHandle = glGetUniformLocation(mShaderHandle, "uModelMatrix");
        muViewMatrixHandle = glGetUniformLocation(mShaderHandle, "uViewMatrix");
        muLightDirectionHandle = glGetUniformLocation(mShaderHandle, "uLightDirection_worldspace");
        muShininessHandle = glGetUniformLocation(mShaderHandle, "uShininess");
        muLightColorHandle = glGetUniformLocation(mShaderHandle, "uLightColor");

        if (texture != null) {
            // enable texture mapping
            muTextureSampler = glGetUniformLocation(mShaderHandle, "uTextureSampler");
            enableAttribute(ATTRIBUTE_TEXTURE_COORDS, "aVertexTexCoord");
        } else {
            // enable vertex colors
            enableAttribute(ATTRIBUTE_COLORS, "aVertexColor");
        }
        
        // enable attributes
        enableAttribute(ATTRIBUTE_POSITIONS, "aVertexPosition_modelspace");
        enableAttribute(ATTRIBUTE_NORMALS, "aVertexNormal_modelspace");
    }

    /**
     * Returns the phong lighting shininess coefficient.
     * 
     * @return the shininess
     */
    public float getmShininess() {
        return mShininess;
    }

    /**
     * Sets the phong lighting shininess coefficient.
     * 
     * @param shininess
     *            the shininess to set
     */
    public void setmShininess(float shininess) {
        mShininess = shininess;
    }

    /**
     * @see Shader#getShaderHandle()
     */
    @Override
    public int getShaderHandle() {
        return mShaderHandle;
    }

    /**
     * Is called if this shader is bound.
     * 
     * @see Shader#onBind(GfxState)
     */
    @Override
    public void onBind(GfxState state) {
        // pass current transformation matrices to shader
        onMatrixUpdate(state);

        // set shininess
        glUniform1f(muShininessHandle, mShininess);

        // take first light and interpret it as directional light
        ArrayList<Light> lights = state.getEngine().getLights();
        if (lights.size() > 0) {
            Light l = lights.get(0);
            glUniform3f(muLightDirectionHandle, l.posX, l.posY, l.posZ);
            glUniform3f(muLightColorHandle, l.colorR, l.colorG, l.colorB);
        } else {
            // set some default light properties if no light is defines
            glUniform3f(muLightDirectionHandle, 1, 1, 1);
            glUniform3f(muLightColorHandle, 1, 1, 1);
        }
        
        // bind texture if enabled
        if(mTexture != null) {
            state.bindTexture(mTexture);
            glUniform1i(muTextureSampler, 0);
        }
    }

    /**
     * Is called if the MVP matrix has changed.
     * 
     * @see Shader#onMatrixUpdate(GfxState)
     */
    @Override
    public void onMatrixUpdate(GfxState state) {
        // pass current transformation matrices to shader
        state.getModelMatrix(mModelMatrix);
        glUniformMatrix4fv(muModelMatrixHandle, 1, false, mModelMatrix, 0);
        glUniformMatrix4fv(muViewMatrixHandle, 1, false, state.getViewMatrix(), 0);
        glUniformMatrix4fv(muMvpMatrixHandle, 1, false, state.getMvpMatrix(), 0);
    }

}
