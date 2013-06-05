package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT16;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindRenderbuffer;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glFramebufferRenderbuffer;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenRenderbuffers;
import static android.opengl.GLES20.glRenderbufferStorage;
import static android.opengl.GLES20.glViewport;
import android.opengl.GLES20;

import com.github.fabmax.lightgl.TextureProperties.MagFilterMethod;
import com.github.fabmax.lightgl.TextureProperties.MinFilterMethod;
import com.github.fabmax.lightgl.TextureProperties.WrappingMethod;
import com.github.fabmax.lightgl.scene.Node;

/**
 * TextureRenderer is used to render an arbitrary Node to a texture.
 * 
 * @author fabmax
 * 
 */
public class TextureRenderer {

    private int mWidth = 0;
    private int mNewWidth = 512;
    private int mHeight = 0;
    private int mNewHeight = 512;
    private int mBorder = 0;

    private Texture mTargetTex;

    private int mFramebufferHandle;
    private int mRenderbufferHandle;

    /**
     * Creates a new TextureRenderer. Must be called from the GL thread.
     * 
     * @param engine
     *            graphics engine
     */
    public TextureRenderer(GfxEngine engine) {
        // generate the framebuffer that will hold the shadow map
        int[] buffer = new int[1];
        glGenFramebuffers(1, buffer, 0);
        mFramebufferHandle = buffer[0];
        glGenRenderbuffers(1, buffer, 0);
        mRenderbufferHandle = buffer[0];

        // create the target texture
        mTargetTex = engine.getTextureManager().createTexture();
        TextureProperties props = new TextureProperties();
        props.magFilter = MagFilterMethod.LINEAR;
        props.minFilter = MinFilterMethod.LINEAR;
        props.xWrapping = WrappingMethod.CLAMP;
        props.yWrapping = WrappingMethod.CLAMP;
        mTargetTex.setTextureProperties(props);
    }

    /**
     * Returns the texture this TextureRenderer renders to.
     * 
     * @return the texture this TextureRenderer renders to
     */
    public Texture getTexture() {
        return mTargetTex;
    }
    
    /**
     * Returns the texture width in pixels.
     * 
     * @return the texture width in pixels
     */
    public int getTextureWidth() {
        return mNewWidth;
    }

    /**
     * Returns the texture height in pixels.
     * 
     * @return the texture height in pixels
     */
    public int getTextureHeight() {
        return mNewHeight;
    }

    /**
     * Sets the target texture size in pixels. By default the texture size is set to 512 x 512
     * pixels. The dimensions do not have to be a power of two.
     * 
     * @param width
     *            texture width in pixels
     * @param height
     *            texture height in pixels
     */
    public void setTextureSize(int width, int height) {
        mNewWidth = width;
        mNewHeight = height;
    }
    
    /**
     * Sets the border size. This reduces the viewport size during rendering, so that border pixels
     * are not drawn in the texture.
     * 
     * @param border
     *            border size
     */
    public void setBorder(int border) {
        mBorder = border;
    }

    /**
     * Renders the specified Node to the texture using the current engine state. The image that is
     * rendered into the texture will have the aspect ratio of the current viewport - not the aspect
     * ratio of the texture itself.
     * 
     * @param engine
     *            graphics engine
     * @param nodeToRender
     *            node to be rendered to the texture
     */
    public void renderToTexture(GfxEngine engine, Node nodeToRender) {
        GfxState state = engine.getState();
        bindFramebuffer(engine);

        // set viewport size to the size of our target texture
        // do not use GfxState#setViewport for this as this would affect the camera
        // aspect ratio which is typically not wanted
        glViewport(mBorder, mBorder, mWidth - mBorder * 2, mHeight - mBorder * 2);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // draw scene
        if (nodeToRender != null) {
            nodeToRender.render(state);
        }

        // restore normal state
        int[] vp = state.getViewport();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glViewport(vp[0], vp[1], vp[2], vp[3]);
    }

    /**
     * Checks the size of the target texture and binds the framebuffer
     * 
     * @param engine
     *            graphics engine
     */
    private void bindFramebuffer(GfxEngine engine) {
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebufferHandle);
        glBindRenderbuffer(GL_RENDERBUFFER, mRenderbufferHandle);

        if (mNewWidth != mWidth || mNewHeight != mHeight) {
            mWidth = mNewWidth;
            mHeight = mNewHeight;

            // create / resize target texture
            engine.getState().bindTexture(mTargetTex);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, mWidth, mHeight, 0,
                    GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, null);

            // create / resize the render buffer needed for depth testing
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, mWidth, mHeight);

            // setup the framebuffer
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                    mTargetTex.getTextureHandle(), 0);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,
                    mRenderbufferHandle);
        }
    }

}
