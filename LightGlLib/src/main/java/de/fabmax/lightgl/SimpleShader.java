package de.fabmax.lightgl;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;

import java.util.ArrayList;

import android.util.Log;

/**
 * A standard shader for a single directional light source. If the list returned by
 * {@link GfxEngine#getLights()} contains multiple lights only the first one is considered. Phong
 * and Gouraud light de.fabmax.lightgl.demo.models are available. Phong lighting offers better quality but is slower than
 * Gouraud lighting. Meshes rendered with this shader must define normal attributes.
 * 
 * @author fabmax
 * 
 */
public class SimpleShader extends Shader {

    private static final String TAG = "SimpleShader";

    private final String mShaderName;
    private final boolean mUseTexture;
    
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
     * Creates a new SimpleShader that uses gouraud lighting and a texture.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     * @param texture
     *            Texture that is mapped onto the shaded object
     * 
     * @return a SimpleShader that uses a texture
     */
    public static SimpleShader createGouraudTextureShader(ShaderManager shaderMgr, Texture texture) {
        SimpleShader shader = new SimpleShader(shaderMgr, true, "gouraud_texture");
        shader.setTexture(texture);
        return shader;
    }
    
    /**
     * Creates a new SimpleShader that uses phong lighting and a texture.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     * @param texture
     *            Texture that is mapped onto the shaded object
     * 
     * @return a SimpleShader that uses a texture
     */
    public static SimpleShader createPhongTextureShader(ShaderManager shaderMgr, Texture texture) {
        SimpleShader shader = new SimpleShader(shaderMgr, true, "phong_texture");
        shader.setTexture(texture);
        return shader;
    }

    /**
     * Creates a new SimpleShader that uses phong lighting and vertex colors.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     * 
     * @return a SimpleShader that uses vertex colors
     */
    public static SimpleShader createPhongColorShader(ShaderManager shaderMgr) {
        return new SimpleShader(shaderMgr, false, "phong_color");
    }

    /**
     * Creates a new SimpleShader object with the specified shader file name. Shader implementations
     * can subclass SimpleShader, modify the shader source and use this constructor to load the
     * modified shader version.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     * @param useTexture
     *            Flag that indicates if the shader should support texture mapping
     * @param shaderName
     *            name of the shader to load
     */
    protected SimpleShader(ShaderManager shaderMgr, boolean useTexture, String shaderName) {
        super(shaderMgr);
        mUseTexture = useTexture;
        mShaderName = shaderName;
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
            // load shader
            int handle = shaderMgr.loadShader(mShaderName);
            setGlHandle(handle);

            // get uniform locations
            muMvpMatrixHandle = glGetUniformLocation(handle, "uMvpMatrix");
            muModelMatrixHandle = glGetUniformLocation(handle, "uModelMatrix");
            muViewMatrixHandle = glGetUniformLocation(handle, "uViewMatrix");
            muLightDirectionHandle = glGetUniformLocation(handle, "uLightDirection_worldspace");
            muShininessHandle = glGetUniformLocation(handle, "uShininess");
            muLightColorHandle = glGetUniformLocation(handle, "uLightColor");
    
            if (mUseTexture) {
                // enable texture mapping
                muTextureSamplerHandle = glGetUniformLocation(handle, "uTextureSampler");
                enableAttribute(ATTRIBUTE_TEXTURE_COORDS, "aVertexTexCoord");
            } else {
                // enable vertex colors
                enableAttribute(ATTRIBUTE_COLORS, "aVertexColor");
            }
            
            // enable attributes
            enableAttribute(ATTRIBUTE_POSITIONS, "aVertexPosition_modelspace");
            enableAttribute(ATTRIBUTE_NORMALS, "aVertexNormal_modelspace");

        } catch (LightGlException e) {
            Log.e(TAG, e.getMessage());
        }
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
