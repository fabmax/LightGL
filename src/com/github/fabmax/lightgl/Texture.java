package com.github.fabmax.lightgl;


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
}
