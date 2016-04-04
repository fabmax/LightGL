package de.fabmax.lightgl;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import de.fabmax.lightgl.util.BufferHelper;

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
     * Binds the given texture to the texture unit 0.
     *
     * @param texture    texture to be bound
     */
    public void bindTexture(Texture texture) {
        bindTexture(texture, GL_TEXTURE0);
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
     * Creates a texture from the given Bitmap with the specified properties,
     *
     * @param bitmap    Bitmap to create the texture from
     * @param props     Texture properties to use
     * @return a texture containing the bitmap data
     */
    public Texture createTextureFromBitmap(Bitmap bitmap, TextureProperties props) {
        boolean alpha = bitmap.hasAlpha();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ByteBuffer data = convertImage(bitmap, alpha);
        return createTextureFromBuffer(data, width, height, alpha, props);
    }

    /**
     * Creates a texture from the given data. If hasAlpha is true data format is assumed to be
     * 32-bit RGBA, otherwise it is 24-bit RGB. TextureProperties can be null, in that case default
     * properties are used.
     *
     * @param data      Buffer with per-pixel texture data
     * @param width     width of texture in pixels
     * @param height    height of texture in pixels
     * @param hasAlpha  true if texture data has an alpha channel
     * @param props     {@link TextureProperties} to set, can be null
     * @return the loaded texture
     */
    public Texture createTextureFromBuffer(ByteBuffer data, int width, int height,
                                           boolean hasAlpha, TextureProperties props) {

        if (props == null) {
            // set default texture properties, if nothing else is specfied
            props = DEFAULT_PROPERTIES;
        }

        if (!isPow2(width) || !isPow2(height)) {
            Log.w(TAG, "Tex width / height is not a power of 2, this might cause problems on some " +
                    "platforms... (size is " + width + "x" + height + ")");
        }

        int target = GL_TEXTURE_2D;
        int format = hasAlpha ? GLES20.GL_RGBA : GLES20.GL_RGB;
        //int components = hasAlpha ? 4 : 3;

        // create texture handle
        Texture tex = createEmptyTexture();

        // load texture data
        bindTexture(tex, GL_TEXTURE0);
        GLES20.glTexImage2D(target, 0, format, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, data);
        if (props.minFilter == TextureProperties.MinFilterMethod.TRILINEAR) {
            GLES20.glGenerateMipmap(GL_TEXTURE_2D);
        }

        tex.setTextureProperties(props);
        tex.setWidth(width);
        tex.setHeight(height);

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

    /**
     * Helper method that converts an BufferedImage to a ByteBuffer.
     *
     * @param image
     *            BufferedImage to convert
     * @param alpha
     *            set to true if the specified image has an alpha channel
     * @return a ByteBuffer containing the image data
     */
    private ByteBuffer convertImage(Bitmap image, boolean alpha) {
        int w = image.getWidth();
        int h = image.getHeight();
        int stride = alpha ? 4 : 3;
        byte[] buf = new byte[w * h * stride];

        if (image.getConfig() == Bitmap.Config.ARGB_8888) {
            // this take even more memory but is significantly faster
            buf = new byte[w * h * 4];
            IntBuffer out = IntBuffer.allocate(w*h);
            image.copyPixelsToBuffer(out);
            for (int i = 0, j = 0; i < w * h; i++, j += stride) {
                int px = out.get(i);
                int pa = ((px >> 24) & 0xff);
                if (pa > 0) {
                    float fa = 255.0f / pa;
                    buf[j]     = (byte) (( px        & 0xff) * fa);
                    buf[j + 1] = (byte) (((px >> 8)  & 0xff) * fa);
                    buf[j + 2] = (byte) (((px >> 16) & 0xff) * fa);
                }
                if (alpha) {
                    buf[j + 3] = (byte) pa;
                }
            }
        } else {
            for (int i = 0; i < w * h; i++) {
                int px = image.getPixel(i % w, i / w);
                buf[i * stride]     = (byte) ((px >> 16) & 0xff);
                buf[i * stride + 1] = (byte) ((px >>  8) & 0xff);
                buf[i * stride + 2] = (byte) (px & 0xff);
                if (alpha) {
                    buf[i * stride + 3] = (byte) ((px >> 24) & 0xff);
                }
            }
        }

        return BufferHelper.createByteBuffer(buf);
    }

    private boolean isPow2(int sz) {
        while ((sz & 1) == 0) {
            sz >>= 1;
        }
        return sz == 1;
    }
}
