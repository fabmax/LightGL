package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;

import com.github.fabmax.lightgl.TextureProperties.MinFilterMethod;


/**
 * The Texture class represents a loaded OpenGL texture.
 * 
 * @author fabmax
 * 
 */
public class Texture {

    // handle to GL texture object
    private int mTextureHandle;

    /**
     * Creates a texture with the specified openGL texture handle.
     * 
     * @param handle
     *            OpenGL texture handle
     */
    protected Texture(int handle) {
        mTextureHandle = handle;
    }

    /**
     * Returns the OpenGL texture handle.
     * 
     * @return the texture handle
     */
    public int getTextureHandle() {
        return mTextureHandle;
    }
    
    /**
     * Sets the specified texture properties for this texture. This texture must be bound before
     * calling this method.
     * 
     * @param props
     *            texture properties to set
     */
    public void setTextureProperties(TextureProperties props) {
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
}
