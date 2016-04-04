package de.fabmax.lightgl;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import android.util.Log;

public class DepthShader extends Shader {

    private static final String TAG = "DepthShader";
    
    private int muMvpMatrixHandle = 0;
    
    /**
     * Creates a new DepthShader.
     * 
     * @param shaderMgr the {@link ShaderManager}
     */
    public DepthShader(ShaderManager shaderMgr) {
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
            // load depth shader code
            int handle = shaderMgr.loadShader("depth");
            setGlHandle(handle);
            
            // get uniform locations
            muMvpMatrixHandle = glGetUniformLocation(handle, "uMvpMatrix");
            
            // enable attributes
            enableAttribute(ATTRIBUTE_POSITIONS, "aVertexPosition_modelspace");
        } catch (LightGlException e) {
            Log.e(TAG, e.getMessage());
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
        glUniformMatrix4fv(muMvpMatrixHandle, 1, false, glContext.getState().getMvpMatrix(), 0);
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
