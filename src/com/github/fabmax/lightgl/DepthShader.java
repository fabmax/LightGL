package com.github.fabmax.lightgl;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import android.util.Log;

public class DepthShader extends Shader {

    private static final String TAG = "DepthShader";
    
    private int mShaderHandle = 0;
    private int muMvpMatrixHandle = 0;
    
    /**
     * Creates a new DepthShader object.
     * 
     * @param shaderMgr
     *            ShaderManager used to load the shader code
     */
    public DepthShader(ShaderManager shaderMgr) {
        // load color shader code
        try {
            mShaderHandle = shaderMgr.loadShader("depth");
        } catch (GlException e) {
            Log.e(TAG, e.getMessage());
        }

        // get uniform locations
        muMvpMatrixHandle = glGetUniformLocation(mShaderHandle, "uMvpMatrix");
        
        // enable attributes
        enableAttribute(ATTRIBUTE_POSITIONS, "aVertexPosition_modelspace");
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
        // pass current MVP matrix to shader
        glUniformMatrix4fv(muMvpMatrixHandle, 1, false, state.getMvpMatrix(), 0);
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
