package de.fabmax.lightgl;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseArray;

/**
 * The TextureManager handles loading and binding of textures.
 * 
 * @author fabmax
 * 
 */
public class TextureManager {
    private static final String TAG = "TextureManager";
    // Default texture properties
    private static final TextureProperties DEFAULT_PROPERTIES = new TextureProperties();

    // Context is needed to load textures from resources
    private Context mContext;

    // List of all loaded textures
    private ArrayList<Texture> mLoadedTextures = new ArrayList<Texture>();
    // Map of loaded texture resources
    private SparseArray<Texture> mResourceMap = new SparseArray<Texture>();
    // Currently bound texture handle
    private int mBoundTextureHandle;
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
     * Is called by {@link GfxEngine} if the GL context was (re-)created. Drops all texture handles.
     */
    protected void newGlContext() {
        // invalidate all old texture
        for (Texture t : mLoadedTextures) {
            t.setGlHandle(0);
        }
        // clear all old texture references
        mLoadedTextures.clear();
        mResourceMap.clear();
    }
    
    /**
     * Removes the specified {@link Texture} from the list of loaded textures.
     * 
     * @param tex 
     */
    protected void removeTexture(Texture tex) {
        if (tex.isValid()) {
            Log.w(TAG, "removeTexture called with undeleted Texture");
            tex.delete();
        }
        mLoadedTextures.remove(tex);
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
        // check used texture unit
        if (mActiveTextureUnit != texUnit) {
            glActiveTexture(texUnit);
            mActiveTextureUnit = texUnit;
        }

        // get texture handle to bind, 0 handle will clear bound texture
        int handle = texture != null ? texture.getGlHandle() : 0;
        if (handle != mBoundTextureHandle) {
            // bind texture
            glBindTexture(GL_TEXTURE_2D, texture.getGlHandle());
            mBoundTextureHandle = handle;
        }
    }

    /**
     * Loads the specified bitmap resource as a texture. If the specified resource was loaded before
     * the corresponding Texture is returned instead of a new one.
     * 
     * @param resource
     *            bitmap resource to load
     * @return the loaded texture
     */
    public Texture createTextureFromResource(int resource) {
        return createTextureFromResource(resource, DEFAULT_PROPERTIES);
    }

    /**
     * Loads the specified bitmap resource as a texture. If the specified resource was loaded before
     * the corresponding Texture is returned instead of a new one. Notice that in this case the
     * specified {@link TextureProperties} are ignored.
     * 
     * @param resource
     *            bitmap resource to load
     * @param texProps
     *            {@link TextureProperties} to set
     * @return the loaded texture
     */
    public Texture createTextureFromResource(int resource, TextureProperties props) {
        Texture tex = mResourceMap.get(resource);
        if (tex != null) {
            // this resource was already loaded, return the corresponding Texture
            return tex;
        }
        
        // load bitmap from resources
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resource);

        // create texture from bitmap
        tex = createEmptyTexture();
        
        // load texture data
        bindTexture(tex, GL_TEXTURE0);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        tex.setTextureProperties(props);
        
        // put Texture to resource map
        mResourceMap.put(resource, tex);
        return tex;
    }

    /**
     * Generates and binds an empty texture handle.
     * 
     * @return the created texture
     */
    public Texture createEmptyTexture() {
        return new Texture(this);
    }
}
