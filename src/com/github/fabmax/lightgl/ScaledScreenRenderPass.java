package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import android.util.Log;

import com.github.fabmax.lightgl.scene.Mesh;
import com.github.fabmax.lightgl.scene.Node;
import com.github.fabmax.lightgl.util.MeshFactory;

public class ScaledScreenRenderPass implements RenderPass {
    
    private static final String TAG = "RenderPass";

    private float mViewportScale = 0;
    private TextureRenderer mRenderer;
    private int mTexWidth = 0;
    private int mTexHeight = 0;
    private Mesh mTexMesh;

    public ScaledScreenRenderPass(GfxEngine engine) {
        mRenderer = new TextureRenderer(engine);
        createTexMesh(engine);
    }

    public void setFixedSize(int width, int height) {
        mTexWidth = width;
        mTexHeight = height;
        mRenderer.setTextureSize(width, height);
        mViewportScale = 0;
    }

    public void setViewportScale(float scale) {
        mViewportScale = scale;
    }

    /**
     * Renders the scene. Rendering is done in two steps: At first the scene is rendered into a
     * texture of configurable size. Then the texture is drawn to the screen.
     * 
     * @param engine
     *            graphics engine
     */
    @Override
    public void onRender(GfxEngine engine) {
        int[] vp = engine.getState().getViewport();
        
        // update texture size if a fixed viewport scale is set
        if (mViewportScale > 0) {
            mTexWidth = (int) (vp[2] * mViewportScale + 0.5f);
            mTexHeight = (int) (vp[3] * mViewportScale + 0.5f);
            
            // resizes texture if the size changed, does nothing if the size is the same
            mRenderer.setTextureSize(mTexWidth, mTexHeight);
        }
        
        if (mTexWidth != vp[2] || mTexHeight != vp[3]) {
            // render scene to texture
            mRenderer.renderToTexture(engine, engine.getScene());
            // draw texture to the screen
            glDisable(GL_DEPTH_TEST);
            engine.getState().bindTexture(mRenderer.getTexture());
            mTexMesh.render(engine.getState());
            glEnable(GL_DEPTH_TEST);
            
        } else {
            // render scene directly to screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            Node scene = engine.getScene();
            if(scene != null) {
                scene.render(engine.getState());
            }
        }
    }

    /**
     * Creates the mesh the texture is finally drawn to.
     */
    private void createTexMesh(GfxEngine engine) {
        // vertex positions
        float[] pos = {
               -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                1.0f,  1.0f, 0.0f,
               -1.0f,  1.0f, 0.0f
        };
        // texture coordinates
        float[] uvs = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f
        };
        // vertex indices
        int indcs[] = {
                0, 1, 2, 0, 2, 3
        };
        mTexMesh = MeshFactory.createStaticMesh(pos, null, uvs, null, indcs);
        mTexMesh.setShader(new FramebufferShader(engine.getShaderManager()));
    }
    
    /**
     * The FramebufferShader is used to draw the texture rendered with the framebuffer to the
     * screen.
     */
    private static class FramebufferShader extends Shader {
        private int mShaderHandle = 0;
        private int muTextureSampler = 0;
        
        /**
         * Creates a new DepthShader object.
         * 
         * @param shaderMgr
         *            ShaderManager used to load the shader code
         */
        FramebufferShader(ShaderManager shaderMgr) {
            // load color shader code
            try {
                mShaderHandle = shaderMgr.loadShader("framebuffer");
            } catch (GlException e) {
                Log.e(TAG, e.getMessage());
            }

            // get uniform locations
            muTextureSampler = glGetUniformLocation(mShaderHandle, "uTextureSampler");
            
            // enable attributes
            enableAttribute(ATTRIBUTE_POSITIONS, "aVertexPosition_modelspace");
            enableAttribute(ATTRIBUTE_TEXTURE_COORDS, "aVertexTexCoord");
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
            // set used texture sampler
            glUniform1i(muTextureSampler, 0);
        }

        /**
         * Is called if the MVP matrix has changed.
         * 
         * @see Shader#onMatrixUpdate(GfxState)
         */
        @Override
        public void onMatrixUpdate(GfxState state) {
            // nothing to do here
        }
    }
}
