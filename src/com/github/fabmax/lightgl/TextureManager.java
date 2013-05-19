package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.SparseIntArray;

/**
 * The TextureManager handles loading and binding of textures.
 * 
 * @author fabmax
 * 
 */
public class TextureManager {

    // context is needed to load assets
    private Context mContext;

    // Map of loaded bitmaps / textures
    private SparseIntArray mResourceMap = new SparseIntArray();
    // Currently bound texture
    private Texture mBoundTexture;
    // Active texture unit
    private int mActiveTextureUnit;

    /**
     * Creates a new TextureManager object.
     * 
     * @param context
     *            Application context, is needed to load files from assets directory
     */
    protected TextureManager(Context context) {
        mContext = context;
    }
    
    /**
     * Binds the given texture to the specified texture unit. Use GL_TEXTURE_0 if you only need one
     * texture.
     * 
     * @param texture
     *            texture to be bound
     * @param texUnit
     *            texture unit to be used.
     */
    public void bindTexture(Texture texture, int texUnit) {
        if(mActiveTextureUnit != texUnit) {
            glActiveTexture(texUnit);
            mActiveTextureUnit = texUnit;
        }
        
        if (texture != mBoundTexture) {
            mBoundTexture = texture;
            
            if(texture != null) {
                // bind texture
                glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());
            } else {
                // clear bound texture
                glBindTexture(GL_TEXTURE_2D, 0);
            }
        }
    }

    /**
     * Generates an OpenGL texture object and binds it.
     * 
     * @return handle to the generated texture
     */
    private int genTextureHandle() {
        int[] handle = new int[1];
        glGenTextures(1, handle, 0);
        glBindTexture(GL_TEXTURE_2D, handle[0]);
        return handle[0];
    }

    /**
     * Loads the specified bitmap resource as a texture.
     * 
     * @param resource
     *            bitmap resource to load
     * @param texProps
     *            OpenGL texture properties
     * @return the loaded texture
     */
    public Texture loadTexture(int resource, TextureProperties texProps) {
        int handle = mResourceMap.get(resource);
        if (handle != 0) {
            // this resource was already loaded, return the texture handle
            return new Texture(handle);
        }
        // load bitmap from resources
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resource);
        
        // create texture from bitmap
        Texture tex = createTexture();
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        tex.setTextureProperties(texProps);

        return tex;
    }

    /**
     * Generates and binds an empty texture handle.
     * 
     * @return the created texture
     */
    public Texture createTexture() {
        // generate texture
        Texture tex = new Texture(genTextureHandle());
        bindTexture(tex, GL_TEXTURE0);
        return tex;
    }
}
