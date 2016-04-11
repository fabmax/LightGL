package de.fabmax.lightgl;

import de.fabmax.lightgl.TextureProperties.MinFilterMethod;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;


/**
 * The Texture class represents a loaded OpenGL texture.
 * 
 * @author fabmax
 * 
 */
public class Texture extends GlObject {

    private TextureManager mTexManager;

    private int mWidthPixels;
    private int mHeightPixels;
    
    /**
     * Creates a Texture with a newly generated OpenGL texture object associated to it.
     */
    protected Texture(TextureManager manager) {
        mTexManager = manager;
        glGenTextures(1, mHandle, 0);
    }

    /**
     * Sets the width of this texture in pixels.
     *
     * @param pixels    pixel width of the data buffer associated with this texture
     */
    protected void setWidth(int pixels) {
        mWidthPixels = pixels;
    }

    /**
     * The width of this texture in pixels.
     *
     * @return pixel width of the data buffer associated with this texture
     */
    public int getWidth() {
        return mWidthPixels;
    }

    /**
     * Sets the height of this texture in pixels.
     *
     * @param pixels    pixel height of the data buffer associated with this texture
     */
    protected void setHeight(int pixels) {
        mHeightPixels = pixels;
    }

    /**
     * The height of this texture in pixels.
     *
     * @return pixel height of the data buffer associated with this texture
     */
    public int getHeight() {
        return mHeightPixels;
    }
    
    /**
     * Sets the specified texture properties for this texture. This texture must be bound before
     * calling this method. ATTENTION: calling this method for an empty texture with
     * MinFilterMethod.TRILINEAR will cause a segmentation fault.
     * 
     * @param props
     *            texture properties to set
     */
    public void setTextureProperties(TextureProperties props) {
        mTexManager.bindTexture(this, GL_TEXTURE0);
        
        // set texture properties
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, props.minFilter.getGlMethod());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, props.magFilter.getGlMethod());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, props.xWrapping.getGlMethod());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, props.yWrapping.getGlMethod());

        if (props.minFilter == MinFilterMethod.TRILINEAR) {
            // build mipmaps if trilinear filtering is selected
            glGenerateMipmap(GL_TEXTURE_2D);
        }
    }

    /**
     * Deletes this texture.
     */
    @Override
    public void delete() {
        // handle multiple delete calls gracefully
        if (isValid()) {
            if (mTexManager != null && mTexManager.isBound(this)) {
                // this texture is currently bound, unbind it
                mTexManager.bindTexture(null, mTexManager.getActiveTextureUnit());
            }
            glDeleteTextures(1, mHandle, 0);
            mHandle[0] = 0;
        }
        if (mTexManager != null) {
            mTexManager.removeTexture(this);
            mTexManager = null;
        }
    }
}
