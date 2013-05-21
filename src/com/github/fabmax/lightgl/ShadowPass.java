package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT16;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindRenderbuffer;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glFramebufferRenderbuffer;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenRenderbuffers;
import static android.opengl.GLES20.glRenderbufferStorage;
import static android.opengl.GLES20.glViewport;

import java.nio.ByteBuffer;

import android.opengl.GLES20;

import com.github.fabmax.lightgl.TextureProperties.MagFilterMethod;
import com.github.fabmax.lightgl.TextureProperties.MinFilterMethod;
import com.github.fabmax.lightgl.TextureProperties.WrappingMethod;
import com.github.fabmax.lightgl.util.BufferHelper;

/**
 * RenderPass that computes a shadow map for dynamic shadows.
 * 
 * @author fabmax
 * 
 */
public class ShadowPass implements RenderPass {

    private static final int MAP_SIZE = 512;

    private OrthograpicCamera mShadowCamera = new OrthograpicCamera();
    private Shader mDepthShader;
    private Texture mDepthTexture;
    private int mTextureUnit = GL_TEXTURE1;
    private int mFramebufferHandle;
    private int mRenderbufferHandle;
    
    private float[] mShadowViewMatrix = new float[16];    
    private float[] mShadowProjMatrix = new float[16];

    /**
     * Creates a new ShadowPass. Before calling this constructor the GL context must be initialized.
     * 
     * @param engine
     *            the graphics engine
     */
    public ShadowPass(GfxEngine engine) {
        mDepthShader = new DepthShader(engine.getShaderManager());
        
        // generate the framebuffer that will hold the shadow map
        int[] buffer = new int[1];
        glGenFramebuffers(1, buffer, 0);
        mFramebufferHandle = buffer[0];
        glGenRenderbuffers(1, buffer, 0);
        mRenderbufferHandle = buffer[0];

        // create the shadow map texture
        mDepthTexture = engine.getTextureManager().createTexture();
        TextureProperties props = new TextureProperties();
        props.magFilter = MagFilterMethod.LINEAR;
        props.minFilter = MinFilterMethod.LINEAR;
        props.xWrapping = WrappingMethod.CLAMP;
        props.yWrapping = WrappingMethod.CLAMP;
        mDepthTexture.setTextureProperties(props);
        ByteBuffer texBuffer = BufferHelper.createByteBuffer(512*512*4);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, 512, 512, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, texBuffer);

        // create the render buffer needed for depth testing
        glBindRenderbuffer(GL_RENDERBUFFER, mRenderbufferHandle);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, MAP_SIZE, MAP_SIZE);

        // setup the framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebufferHandle);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mDepthTexture.getTextureHandle(), 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mRenderbufferHandle);
              
        // unbind framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Renders a shadow map for a single light.
     * 
     * @see RenderPass#doRenderPass(GfxEngine)
     */
    @Override
    public void doRenderPass(GfxEngine engine) {
        if (engine.getLights().size() == 0) {
            // there is no light to cast a shadow
            return;
        }
        Light l = engine.getLights().get(0);

        // TODO: consider scene size and compute an appropriate camera position and clip size
        mShadowCamera.setPosition(l.posX, l.posY, l.posZ);
        mShadowCamera.setClipSize(-10, 10, -10, 10, -10, 10);
        mShadowCamera.setLookAt(0, 0, 0);
        mShadowCamera.getViewMatrix(mShadowViewMatrix);
        mShadowCamera.getProjectionMatrix(mShadowProjMatrix);

        // render to the framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebufferHandle);
        
        glViewport(0, 0, MAP_SIZE, MAP_SIZE);
        // front face culling can improve shadow appearance in some cases
        //glCullFace(GL_FRONT);
        // set the depth texture color values to infinite depth
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // draw scene
        GfxState state = engine.getState();
        state.bindShader(mDepthShader);
        state.setLockShader(true);
        state.setCamera(mShadowCamera);
        engine.getScene().render(state);

        // render to the screen
        state.setLockShader(false);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, engine.getViewportWidth(), engine.getViewportHeight());
        //glCullFace(GL_BACK);
        
        engine.getTextureManager().bindTexture(mDepthTexture, mTextureUnit);
        engine.getState().resetBackgroundColor();
    }
    
    /**
     * Returns the index of the texture unit the depth texture is bound to.
     * 
     * @return the index of the texture unit the depth texture is bound to
     */
    public int getTextureUnit() {
        return mTextureUnit - GL_TEXTURE0;
    }
    
    /**
     * Sets the texture unit the depth texture is bound to. Do not use GL_TEXTUREn but just the
     * index.
     * 
     * @param texUnit the texture unit to use
     */
    public void setTextureUnit(int texUnit) {
        mTextureUnit = texUnit + GL_TEXTURE0;
    }
    
    /**
     * Returns the view matrix from the shadow camera.
     * 
     * @return the view matrix from the shadow camera
     */
    public float[] getShadowViewMatrix() {
        return mShadowViewMatrix;
    }

    /**
     * Returns the projection matrix from the shadow camera.
     * 
     * @return the projection matrix from the shadow camera
     */
    public float[] getShadowProjectionMatrix() {
        return mShadowProjMatrix;
    }
}
