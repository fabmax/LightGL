package de.fabmax.lightgl;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TRUE;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUseProgram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;

/**
 * The ShaderManager handles loading and binding of shaders.
 * 
 * @author fabmax
 * 
 */
public class ShaderManager {
    private static final String TAG = "ShaderManager";

    // context is needed to load assets
    private Context mContext;

    private ArrayList<Shader> mLoadedShaders = new ArrayList<Shader>();
    // map that holds all generated shader handles
    private SparseIntArray mShaderHandles = new SparseIntArray();
    // currently bound shader
    private Shader mBoundShader;

    /**
     * Creates a new ShaderManager object.
     * 
     * @param context
     *            Application context, is needed to load files from assets directory
     */
    protected ShaderManager(Context context) {
        mContext = context;
    }

    /**
     * Is called by {@link GfxEngine} if the GL context was (re-)created. Drops all shader handles.
     */
    public void newGlContext() {
        for (Shader s : mLoadedShaders) {
            s.setGlHandle(0);
        }
        mShaderHandles.clear();
    }
    
    /**
     * Called by the constructor of the {@link Shader} base class to register a newly created shader
     * instance.
     * 
     * @param shader
     *            {@link Shader} to register
     */
    protected void registerShader(Shader shader) {
        mLoadedShaders.add(shader);
    }
    
    /**
     * Deletes the specified {@link Shader}. Same as calling {@link Shader#delete()}.
     * 
     * @param shader the shader to delete
     */
    public void deleteShader(Shader shader) {
        int deleteHandle = shader.getGlHandle();
        
        shader.setGlHandle(0);
        mLoadedShaders.remove(shader);
        
        // check whether handle is used by another shader
        boolean inUse = false;
        for (Shader s : mLoadedShaders) {
            if (s.getGlHandle() == deleteHandle) {
                inUse = true;
                break;
            }
        }
        
        if (!inUse) {
            // shader is not in use anymore - delete it
            glDeleteShader(deleteHandle);
            int index = mShaderHandles.indexOfValue(deleteHandle);
            if (index >= 0) {
                mShaderHandles.removeAt(index);
            }
        }
    }

    /**
     * Binds the specified shader. If the shader is not already bound its onBind() method is called.
     * 
     * @param state
     *            current graphics engine state
     * @param shader
     *            shader to be bound
     */
    public void bindShader(GfxState state, Shader shader) {
        if (shader != null) {
            if (!shader.isValid()) {
                Log.d(TAG, "loading shader");
                shader.loadShader(this);
            }
            if (shader != mBoundShader) {
                // bind shader program
                glUseProgram(shader.getGlHandle());
                mBoundShader = shader;
                // notify shader that it was bound
                shader.onBind(state);
            }
        } else {
            // clear used shader
            glUseProgram(0);
            mBoundShader = null;
        }
    }

    /**
     * Returns the currently bound shader.
     * 
     * @return the currently bound shader
     */
    public Shader getBoundShader() {
        return mBoundShader;
    }

    /**
     * Loads the shader from the App's assets/shaders directory. The shader consists of 2 source
     * files for the vertex and the fragment shader. The source files must be named name +
     * "_vert.glsl" for the vertex shader and name + "_frag.glsl" for the fragment shader.
     * 
     * @param name
     *            Shader name used to load the source files. The source files must be named name +
     *            "_vert.glsl" for the vertex shader and name + "_frag.glsl" for the fragment
     *            shader.
     * @return the GL shader handle; is 0 if there was an error while loading the shader
     * @throws GlException
     *             if shader compilation failed
     */
    public int loadShader(String name) throws GlException {
        try {
            // load vertex shader source code from assets
            String vertShaderSrc = loadSource("shaders/" + name + "_vert.glsl", mContext);
            String fragShaderSrc = loadSource("shaders/" + name + "_frag.glsl", mContext);

            // load shader from sources
            int handle = loadShader(vertShaderSrc, fragShaderSrc);
            Log.i(TAG, "Successfully loaded shader \"" + name + "\", handle: " + handle);
            return handle;

        } catch (IOException e) {
            throw new GlException("Failed loading shader source", e);
        }
    }

    /**
     * Compiles the shader from the specified source code strings.
     * 
     * @param vertexShaderSrc
     *            source code for the vertex shader
     * @param fragmentShaderSrc
     *            source code for the fragment shader
     * @return the GL shader handle; is 0 if there was an error while loading the shader
     * @throws GlException
     *             if shader compilation failed
     */
    public int loadShader(String vertexShaderSrc, String fragmentShaderSrc) throws GlException {
        // check if this shader code was already loaded
        int hashcode = (vertexShaderSrc + fragmentShaderSrc).hashCode();
        int shaderHandle = mShaderHandles.get(hashcode);
        if (shaderHandle != 0) {
            // this shader was already loaded, just return its handle
            return shaderHandle;
        }

        int shaderResult[] = new int[1];

        // create vertex shader object
        int vertShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertShader, vertexShaderSrc);
        glCompileShader(vertShader);

        // check compilation result
        glGetShaderiv(vertShader, GL_COMPILE_STATUS, shaderResult, 0);
        if (shaderResult[0] != GL_TRUE) {
            // vertex shader compilation failed
            String log = glGetShaderInfoLog(vertShader);
            // delete allocated shader objects
            glDeleteShader(vertShader);
            throw new GlException("Vertex shader compilation failed: " + log);
        }

        // create fragment shader object
        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, fragmentShaderSrc);
        glCompileShader(fragShader);

        // check compilation result
        glGetShaderiv(fragShader, GL_COMPILE_STATUS, shaderResult, 0);
        if (shaderResult[0] != GL_TRUE) {
            // fragment shader compilation failed
            String log = glGetShaderInfoLog(fragShader);
            // delete allocated shader objects
            glDeleteShader(vertShader);
            glDeleteShader(fragShader);
            // throw exception with error message
            throw new GlException("Fragment shader compilation failed: " + log);
        }

        // link shader program
        shaderHandle = glCreateProgram();
        glAttachShader(shaderHandle, vertShader);
        glAttachShader(shaderHandle, fragShader);
        glLinkProgram(shaderHandle);
        // after linkage fragment and vertex shader are no longer needed
        glDeleteShader(vertShader);
        glDeleteShader(fragShader);

        // check linker result
        glGetProgramiv(shaderHandle, GL_LINK_STATUS, shaderResult, 0);
        if (shaderResult[0] != GL_TRUE) {
            // shader linkage failed
            String log = glGetProgramInfoLog(shaderHandle);
            // delete allocated shader object
            glDeleteProgram(shaderHandle);
            // throw exception with error message
            throw new GlException("Shader linkage failed failed: " + log);
        }

        // if everything went well put the shader handle into the handle map
        if (shaderHandle != 0) {
            mShaderHandles.put(hashcode, shaderHandle);
        }

        // return shader program handle
        return shaderHandle;
    }

    /*
     * Helper method that reads a asset file with shader source code into a String.
     */
    private static String loadSource(String assetName, Context context) throws IOException {
        StringBuffer sBuf = new StringBuffer();

        // open asset
        InputStream in = context.getAssets().open(assetName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // read shader source line by line
        String line = reader.readLine();
        while (line != null) {
            sBuf.append(line);
            sBuf.append('\n');
            line = reader.readLine();
        }

        // return read source
        return sBuf.toString();
    }
}
