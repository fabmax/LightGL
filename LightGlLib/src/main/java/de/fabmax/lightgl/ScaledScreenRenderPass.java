package de.fabmax.lightgl;

import android.util.Log;

import de.fabmax.lightgl.scene.Mesh;
import de.fabmax.lightgl.scene.Node;
import de.fabmax.lightgl.util.MeshFactory;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

public class ScaledScreenRenderPass implements RenderPass {
    
    private static final String TAG = "RenderPass";

    private final TextureRenderer mRenderer;
    private float mViewportScale = 0;
    private int mTexWidth = 0;
    private int mTexHeight = 0;
    private Mesh mTexMesh;

    public ScaledScreenRenderPass(GfxEngine engine) {
        mRenderer = new TextureRenderer();
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
     * @param glContext    graphics engine context
     */
    @Override
    public void onRender(LightGlContext glContext) {
        int[] vp = glContext.getState().getViewport();
        
        // update texture size if a fixed viewport scale is set
        if (mViewportScale > 0) {
            mTexWidth = (int) (vp[2] * mViewportScale + 0.5f);
            mTexHeight = (int) (vp[3] * mViewportScale + 0.5f);
            
            // resizes texture if the size changed, does nothing if the size is the same
            mRenderer.setTextureSize(mTexWidth, mTexHeight);
        }
        
        if (mTexWidth != vp[2] || mTexHeight != vp[3]) {
            // render scene to texture
            mRenderer.renderToTexture(glContext, glContext.getEngine().getScene());
            // draw texture to the screen
            glDisable(GL_DEPTH_TEST);
            glContext.getTextureManager().bindTexture(mRenderer.getTexture());
            mTexMesh.render(glContext);
            glEnable(GL_DEPTH_TEST);
            
        } else {
            // render scene directly to screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            Node scene = glContext.getEngine().getScene();
            if(scene != null) {
                scene.render(glContext);
            }
        }
    }

    /**
     * Creates the mesh the texture is finally drawn to.
     */
    private void createTexMesh(GfxEngine engine) {
        MeshFactory.MeshConstructionInfo info = new MeshFactory.MeshConstructionInfo();
        // vertex positions
        info.positions = new float[] {
               -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                1.0f,  1.0f, 0.0f,
               -1.0f,  1.0f, 0.0f
        };
        // texture coordinates
        info.texCoords = new float[] {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f
        };
        // vertex indices
        info.indices = new int[] {
                0, 1, 2, 0, 2, 3
        };
        mTexMesh = MeshFactory.createStaticMesh(info);
        mTexMesh.setShader(new FramebufferShader(engine.getShaderManager()));
    }
    
    /**
     * The FramebufferShader is used to draw the texture rendered with the framebuffer to the
     * screen.
     */
    private static class FramebufferShader extends Shader {
        private int muTextureSampler = 0;

        /**
         * Creates a new FramebufferShader.
         * 
         * @param shaderMgr the {@link ShaderManager}
         */
        public FramebufferShader(ShaderManager shaderMgr) {
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
                // load framebuffer shader code
                setGlHandle(shaderMgr.loadShader("framebuffer"));

                // get uniform locations
                muTextureSampler = glGetUniformLocation(getGlHandle(), "uTextureSampler");
                
                // enable attributes
                enableAttribute(ATTRIBUTE_POSITIONS, "aVertexPosition_modelspace");
                enableAttribute(ATTRIBUTE_TEXTURE_COORDS, "aVertexTexCoord");
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
