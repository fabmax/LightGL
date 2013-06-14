package de.fabmax.lightgl;

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

    // shader handle
    protected int mShaderHandle = 0;

    // uniform handles
    private int muMvpMatrixHandle = 0;
    private int muModelMatrixHandle = 0;
    private int muViewMatrixHandle = 0;
    private int muLightDirectionHandle = 0;
    private int muShininessHandle = 0;
    private int muLightColorHandle = 0;
    private int muTextureSamplerHandle = 0;

    // shininess coefficient for phong lighting model
    private float mShininess = 20.0f;
    // optional texture
    protected Texture mTexture;

    /**
     * Creates a new PhongShader object. Rendered objects must provide vertex colors.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     * 
     * @return a PhongShader that uses vertex colors
     */
    public static PhongShader createColorPhongShader(ShaderManager shaderMgr) {
        return new PhongShader(shaderMgr, false, "phong_color");
    }
    
    /**
     * Creates a new PhongShader that uses a texture.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     * @param texture
     *            Texture that is mapped onto the shaded object
     * 
     * @return a PhongShader that uses a texture
     */
    public static PhongShader createTexturePhongShader(ShaderManager shaderMgr, Texture texture) {
        PhongShader shader = new PhongShader(shaderMgr, true, "phong_texture");
        shader.setTexture(texture);
        return shader;
    }

    /**
     * Creates a new PhongShader object with the specified shader file name. Shader implementations
     * can subclass PhongShader, modify the shader source and use this constructor to load the
     * modified shader version.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     * @param texture
     *            Optional texture that is mapped onto the shaded object
     * @param shaderFile
     *            shader file name to load
     */
    protected PhongShader(ShaderManager shaderMgr, boolean useTexture, String shaderFile) {
        // load color shader code
        try {
            // load shader with texture mapping
            mShaderHandle = shaderMgr.loadShader(shaderFile);
        } catch (GlException e) {
            Log.e(TAG, e.getMessage());
        }

        // get uniform locations
        muMvpMatrixHandle = glGetUniformLocation(mShaderHandle, "uMvpMatrix");
        muModelMatrixHandle = glGetUniformLocation(mShaderHandle, "uModelMatrix");
        muViewMatrixHandle = glGetUniformLocation(mShaderHandle, "uViewMatrix");
        muLightDirectionHandle = glGetUniformLocation(mShaderHandle, "uLightDirection_worldspace");
        muShininessHandle = glGetUniformLocation(mShaderHandle, "uShininess");
        muLightColorHandle = glGetUniformLocation(mShaderHandle, "uLightColor");

        if (useTexture) {
            // enable texture mapping
            muTextureSamplerHandle = glGetUniformLocation(mShaderHandle, "uTextureSampler");
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
    public void setTexture(Texture texture) {
        mTexture = texture;
    }

    /**
     * Returns the phong lighting shininess coefficient.
     * 
     * @return the shininess
     */
    public float getShininess() {
        return mShininess;
    }

    /**
     * Sets the phong lighting shininess coefficient.
     * 
     * @param shininess
     *            the shininess to set
     */
    public void setShininess(float shininess) {
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
            glUniform3f(muLightDirectionHandle, l.position[0], l.position[1], l.position[2]);
            glUniform3f(muLightColorHandle, l.color[0], l.color[1], l.color[2]);
        } else {
            // set some default light properties if no light is defines
            glUniform3f(muLightDirectionHandle, 1, 1, 1);
            glUniform3f(muLightColorHandle, 1, 1, 1);
        }
        
        // bind texture if enabled
        if(mTexture != null) {
            state.bindTexture(mTexture);
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
        // pass current transformation matrices to shader
        glUniformMatrix4fv(muModelMatrixHandle, 1, false, state.getModelMatrix(), 0);
        glUniformMatrix4fv(muViewMatrixHandle, 1, false, state.getViewMatrix(), 0);
        glUniformMatrix4fv(muMvpMatrixHandle, 1, false, state.getMvpMatrix(), 0);
    }

}
