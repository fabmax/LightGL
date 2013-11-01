package de.fabmax.lightgl;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;

/**
 * The TextureManager handles loading and binding of de.fabmax.lightgl.demo.textures.
 * 
 * @author fabmax
 * 
 */
public class TextureManager {
    private static final String TAG = "TextureManager";
    // Default texture properties
    private static final TextureProperties DEFAULT_PROPERTIES = new TextureProperties();

    // Context is needed to load de.fabmax.lightgl.demo.textures from resources
    private final Context mContext;

    // List of all loaded de.fabmax.lightgl.demo.textures
    private final ArrayList<Texture> mLoadedTextures = new ArrayList<>();
    // Map with textures loaded from app assets
    private final SparseArray<Texture> mResourceMap = new SparseArray<>();
    // Currently bound texture handle
    private int mBoundTextureHandle;
    // Active texture unit
    private int mActiveTextureUnit;

//    public ArrayList<Texture> getLoadedTextures() {
//        return mLoadedTextures;
//    }
    
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
        mBoundTextureHandle = 0;
        mActiveTextureUnit = 0;
    }
    
    public boolean isBound(Texture tex) {
        return tex.getGlHandle() == mBoundTextureHandle;
    }
    
    public int getActiveTextureUnit() {
        return mActiveTextureUnit;
    }
    
    /**
     * Removes the specified {@link Texture} from the list of loaded de.fabmax.lightgl.demo.textures.
     * 
     * @param tex    Texture to remove
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
            glBindTexture(GL_TEXTURE_2D, handle);
            mBoundTextureHandle = handle;
        }
    }

    /**
     * Loads the specified file as a texture. If the specified resource was loaded before the
     * corresponding Texture is returned instead of a new one. The specified file is loaded
     * from the App assets by calling {@link AssetManager#open(String)}.
     * 
     * @param assetPath
     *            path in the assets directory
     * @return the loaded texture
     */
    public Texture createTextureFromAsset(String assetPath) {
        return createTextureFromAsset(assetPath, DEFAULT_PROPERTIES);
    }

    /**
     * Loads the specified file as a texture. If the specified file was loaded before the
     * corresponding Texture is returned instead of a new one. Notice that in this case the
     * specified {@link TextureProperties} are ignored. The specified file is loaded from the App
     * assets by calling {@link AssetManager#open(String)}.
     * 
     * @param assetPath
     *            path in the assets directory
     * @param props
     *            {@link TextureProperties} to set
     * @return the loaded texture
     */
    public Texture createTextureFromAsset(String assetPath, TextureProperties props) {
        int key = assetPath.hashCode();
        Texture tex = mResourceMap.get(key);
        if (tex != null) {
            // this resource was already loaded, return the corresponding Texture
            return tex;
        }
        
        try {
            // create texture from bitmap
            tex = createEmptyTexture();
            
            // load bitmap from resources
            InputStream in = mContext.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            
            // load texture data
            bindTexture(tex, GL_TEXTURE0);
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            
            tex.setTextureProperties(props);
            Log.i(TAG, "Successfully loaded texture: \"" + assetPath + "\"");
            
            // put Texture to resource map
            mResourceMap.put(key, tex);
        } catch(IOException e) {
            Log.e(TAG, "Failed loading texture: " + assetPath + " (" + e.getMessage() + ")");
        }
        return tex;
    }

    /**
     * Generates and binds an empty texture handle.
     * 
     * @return the created texture
     */
    public Texture createEmptyTexture() {
        Texture tex = new Texture(this);
        mLoadedTextures.add(tex);
        return tex;
    }
}
